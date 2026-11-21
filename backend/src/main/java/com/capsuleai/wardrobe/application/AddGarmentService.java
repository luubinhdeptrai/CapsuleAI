package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-GAR-001 / US-GAR-003 — accepts one explicitly confirmed manual garment for the
 * authenticated owner, optionally with the reviewed staged photo attached
 * (FR-AI-003/008/010, FR-GAR-003, DATA-GAR-004, DATA-INT-004, ADA-009, S3-IMG-03/04).
 *
 * <p>The client's Idempotency-Key names the logical addition. The garment, its sets, the key
 * and (with a photo) the photo's READY → ATTACHED transition commit together, so repeating the
 * same addition returns the garment already accepted instead of creating a second one. A photo
 * that cannot be used makes the addition fail; it never falls back to an imageless garment.
 */
public final class AddGarmentService {

    private final GarmentRepository garments;
    private final PhotoUploadRepository photoUploads;
    private final WardrobeTransactions transactions;
    private final Clock clock;

    public AddGarmentService(GarmentRepository garments, PhotoUploadRepository photoUploads,
                             WardrobeTransactions transactions, Clock clock) {
        this.garments = Objects.requireNonNull(garments, "garments");
        this.photoUploads = Objects.requireNonNull(photoUploads, "photoUploads");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws GarmentValidationException    if the entry is unconfirmed, incomplete or invalid (nothing stored)
     * @throws IdempotencyKeyReusedException if the key was used for different values or another photo
     * @throws PhotoUploadNotFoundException  if the named staged photo is not the owner's
     * @throws PhotoUploadNotUsableException if the staged photo is not READY, has expired or is attached
     * @throws GarmentNoLongerCurrentException if this logical addition was accepted earlier and its garment
     *                                         has since been removed (US-GAR-005, OPEN-S4-03)
     */
    public GarmentCreation add(AddGarmentCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ValidatedGarmentEntry entry = GarmentEntryValidator.validate(command);
        String fingerprint = GarmentCreationFingerprint.of(entry.profile(), entry.photoUploadId());
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);

        if (entry.photoUploadId().isPresent()) {
            return addWithPhoto(owner, entry, entry.photoUploadId().get(), fingerprint, now);
        }
        Garment candidate = Garment.confirmManualEntry(UUID.randomUUID(), owner, entry.profile(), now);
        return transactions.inTransaction(() -> {
            if (garments.addIfCreationKeyUnused(candidate, entry.creationKey(), fingerprint)) {
                return new GarmentCreation(candidate, true);
            }
            return replay(owner, entry.creationKey(), fingerprint);
        });
    }

    /**
     * One transaction: lock the staged photo (serializing confirmations, cancellation and expiry),
     * replay an earlier acceptance of the same key, otherwise require a usable photo, create the
     * garment and attach the photo.
     */
    private GarmentCreation addWithPhoto(UUID owner, ValidatedGarmentEntry entry, UUID photoUploadId,
                                         String fingerprint, Instant now) {
        return transactions.inTransaction(() -> {
            // Lock first (when the row still exists), then look for an earlier acceptance: after a removal
            // and its cleanup the photo row is gone, but the replay must still be recognized (OPEN-S4-03).
            Optional<PhotoUpload> lockedUpload = photoUploads.lockOwned(owner, photoUploadId);
            if (garments.findCreation(owner, entry.creationKey()).isPresent()) {
                return replay(owner, entry.creationKey(), fingerprint);
            }
            PhotoUpload upload = lockedUpload.orElseThrow(PhotoUploadNotFoundException::new);
            if (!upload.isUsableAt(now)) {
                throw new PhotoUploadNotUsableException();
            }
            Garment candidate = Garment.confirmWithPhoto(UUID.randomUUID(), owner, entry.profile(), now,
                    upload.asGarmentPhoto());
            if (!garments.addIfCreationKeyUnused(candidate, entry.creationKey(), fingerprint)) {
                return replay(owner, entry.creationKey(), fingerprint);
            }
            if (!photoUploads.attach(upload.id(), candidate.id(), now)) {
                throw new IllegalStateException("A locked READY photo could not be attached");
            }
            return new GarmentCreation(candidate, true);
        });
    }

    private GarmentCreation replay(UUID owner, UUID creationKey, String fingerprint) {
        RecordedGarmentCreation earlier = garments.findCreation(owner, creationKey)
                .orElseThrow(() -> new IllegalStateException("A used creation key has no recorded garment"));
        if (!earlier.creationFingerprint().equals(fingerprint)) {
            throw new IdempotencyKeyReusedException();
        }
        if (!earlier.garment().isCurrent()) {
            // US-GAR-005 (OPEN-S4-03): accepted earlier and removed since. A retry never recreates the
            // garment and never presents a removed garment as the current result of the addition.
            throw new GarmentNoLongerCurrentException();
        }
        return new GarmentCreation(earlier.garment(), false);
    }
}