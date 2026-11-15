package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-GAR-003 — stages one private garment photo for later explicit confirmation (S3-IMG-02/03/04,
 * FR-AI-001/002/012). Staging never creates a garment.
 *
 * <p>Order of work: the size rule, then the genuine decode and normalization (no durable state
 * yet, so a rejected photo leaves nothing behind), then a durable RECEIVING intent, then the
 * object store outside any transaction, then a conditional RECEIVING → READY. A retry with the
 * same Idempotency-Key and the same bytes returns the same staged photo; different bytes under the
 * same key are refused.
 */
public final class StagePhotoUploadService {

    static final String PHOTO_FIELD = "photo";
    static final String UPLOAD_KEY_FIELD = "Idempotency-Key";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;
    private final PhotoInspector inspector;
    private final WardrobeTransactions transactions;
    private final Clock clock;
    private final PhotoUploadPolicy policy;

    public StagePhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage, PhotoInspector inspector,
                                   WardrobeTransactions transactions, Clock clock, PhotoUploadPolicy policy) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.inspector = Objects.requireNonNull(inspector, "inspector");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * @throws GarmentValidationException    if the photo part or the Idempotency-Key is missing or malformed
     * @throws PhotoRejectedException        if the photo is not accepted (nothing stored)
     * @throws PhotoUploadKeyReusedException if the key was used for different bytes
     * @throws PhotoUploadNotUsableException if the photo was cancelled or expired while it was being stored
     */
    public StagedPhoto stage(StagePhotoCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        UUID uploadKey = validate(command);
        ReceivedPhoto photo = command.photo();
        if (photo.sizeBytes() > policy.maximumBytes()) {
            throw new PhotoRejectedException(PhotoRejectionReason.TOO_LARGE);
        }
        String contentSha256 = sha256(photo);

        Optional<PhotoUpload> earlier = uploads.findByUploadKey(owner, uploadKey);
        if (earlier.isPresent()) {
            return replay(earlier.get(), contentSha256);
        }

        InspectedPhoto inspected = inspector.inspectAndNormalize(photo);
        if (Math.min(inspected.originalWidth(), inspected.originalHeight()) < policy.minimumShortestSide()) {
            throw new PhotoRejectedException(PhotoRejectionReason.TOO_SMALL);
        }

        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), owner, uploadKey, contentSha256, now(),
                policy.pendingTtl());
        if (!transactions.inTransaction(() -> uploads.addIfUploadKeyUnused(intent))) {
            PhotoUpload concurrent = uploads.findByUploadKey(owner, uploadKey)
                    .orElseThrow(() -> new IllegalStateException("A used upload key has no staged photo"));
            return replay(concurrent, contentSha256);
        }

        try {
            storage.store(intent.objectKey(), inspected.jpeg(), PhotoUpload.STORED_CONTENT_TYPE);
        } catch (RuntimeException storeFailed) {
            transactions.inTransaction(() -> uploads.discardReceiving(intent.id(), now(), policy.inFlightFence()));
            throw storeFailed;
        }

        boolean ready = transactions.inTransaction(() -> uploads.markReady(intent.id(),
                PhotoUpload.STORED_CONTENT_TYPE, inspected.width(), inspected.height(), now()));
        if (!ready) {
            // Cancelled or expired while storing: make sure the object just written is deleted (S3-IMG-04).
            transactions.inTransaction(() -> {
                uploads.requeueCleanup(intent.id(), now());
                return null;
            });
            throw new PhotoUploadNotUsableException();
        }
        PhotoUpload staged = uploads.findOwned(owner, intent.id())
                .orElseThrow(() -> new IllegalStateException("A staged photo disappeared"));
        return new StagedPhoto(staged, true);
    }

    private UUID validate(StagePhotoCommand command) {
        List<GarmentFieldViolation> violations = new ArrayList<>();
        UUID uploadKey = null;
        String rawKey = command.uploadKey();
        if (rawKey == null || rawKey.isBlank()) {
            violations.add(new GarmentFieldViolation(UPLOAD_KEY_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!CANONICAL_UUID.matcher(rawKey).matches()) {
            violations.add(new GarmentFieldViolation(UPLOAD_KEY_FIELD, GarmentValueProblem.INVALID_FORMAT));
        } else {
            uploadKey = UUID.fromString(rawKey);
        }
        if (command.photo() == null || command.photo().sizeBytes() == 0) {
            violations.add(new GarmentFieldViolation(PHOTO_FIELD, GarmentValueProblem.REQUIRED));
        }
        if (!violations.isEmpty()) {
            throw new GarmentValidationException(violations);
        }
        return uploadKey;
    }

    private static StagedPhoto replay(PhotoUpload earlier, String contentSha256) {
        if (!earlier.contentSha256().equals(contentSha256)) {
            throw new PhotoUploadKeyReusedException();
        }
        return new StagedPhoto(earlier, false);
    }

    /** SHA-256 of the received bytes, counting them too, so a misreported size cannot slip past the limit. */
    private String sha256(ReceivedPhoto photo) {
        try (InputStream content = photo.openStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            long total = 0;
            for (int read = content.read(buffer); read != -1; read = content.read(buffer)) {
                total += read;
                if (total > policy.maximumBytes()) {
                    throw new PhotoRejectedException(PhotoRejectionReason.TOO_LARGE);
                }
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException unreadable) {
            throw new UncheckedIOException("The received photo could not be read", unreadable);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", impossible);
        }
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}