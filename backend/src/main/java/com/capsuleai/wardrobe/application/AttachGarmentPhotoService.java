package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentPhotoAttachmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Provisional US-GAR-006 — attaches the owner's reviewed READY photo to the owner's current garment that has
 * no photo (S4-GAR-04/05/06, S4-INT-01/02, OPEN-S4-07/08/10).
 *
 * <p>One Wardrobe transaction locks the garment, then the photo (S4-INT-01), and checks in this order:
 * an identical pair already attached is a replay and changes nothing; a garment that already has a photo
 * is refused; a garment that changed after review is a conflict; a photo that is not READY or has expired
 * is not usable. Otherwise the photo becomes ATTACHED to the garment and the garment's revision advances by
 * one, together. No object is read or written: READY already proves validated, privately stored bytes.
 */
public final class AttachGarmentPhotoService {

    static final String CONFIRMED_FIELD = "confirmed";
    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String PHOTO_UPLOAD_ID_FIELD = "photoUploadId";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /** A positive decimal that fits in a long; clients treat it as opaque (OPEN-S3-04). */
    private static final Pattern REVISION_TOKEN = Pattern.compile("[1-9][0-9]{0,17}");

    /** The validated body of a request. */
    private record ReviewedPair(long expectedRevision, UUID photoUploadId) {
    }

    private final GarmentPhotoAttachmentRepository garmentPhotoAttachmentRepository;
    private final PhotoUploadRepository photoUploadRepository;
    private final WardrobeTransactions wardrobeTransactions;
    private final Clock clock;

    public AttachGarmentPhotoService(GarmentPhotoAttachmentRepository garmentPhotoAttachmentRepository,
                                     PhotoUploadRepository photoUploadRepository,
                                     WardrobeTransactions wardrobeTransactions, Clock clock) {
        this.garmentPhotoAttachmentRepository =
                Objects.requireNonNull(garmentPhotoAttachmentRepository, "garmentPhotoAttachmentRepository");
        this.photoUploadRepository = Objects.requireNonNull(photoUploadRepository, "photoUploadRepository");
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws GarmentValidationException      if a field is missing or malformed (checked before any read)
     * @throws GarmentNotFoundException        if the owner has no garment with this id
     * @throws GarmentNoLongerCurrentException if the owner's garment was removed
     * @throws PhotoUploadNotFoundException    if the photo is not among the owner's staged photos
     * @throws GarmentAlreadyHasPhotoException if the garment already has another photo
     * @throws GarmentRevisionConflictException if the garment changed after the owner reviewed it
     * @throws PhotoUploadNotUsableException   if the photo is not READY, has expired or belongs to another garment
     */
    public GarmentPhotoAttachment attach(AttachGarmentPhotoCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ReviewedPair reviewedPair = checkRequest(command);
        UUID garmentId = garmentId(command.garmentId());
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);

        return wardrobeTransactions.inTransaction(() -> {
            Garment garment = garmentPhotoAttachmentRepository.lockOwnedGarment(ownerAccountId, garmentId)
                    .orElseThrow(GarmentNotFoundException::new);
            if (!garment.isCurrent()) {
                throw new GarmentNoLongerCurrentException();
            }
            PhotoUpload photoUpload = photoUploadRepository.lockOwned(ownerAccountId, reviewedPair.photoUploadId())
                    .orElseThrow(PhotoUploadNotFoundException::new);
            if (photoUpload.status() == PhotoUploadStatus.ATTACHED && garmentId.equals(photoUpload.garmentId())) {
                return new GarmentPhotoAttachment(garment, false);
            }
            if (garment.photo().isPresent()) {
                throw new GarmentAlreadyHasPhotoException();
            }
            if (garment.revision() != reviewedPair.expectedRevision()) {
                throw new GarmentRevisionConflictException();
            }
            if (!photoUpload.isUsableAt(now)) {
                throw new PhotoUploadNotUsableException();
            }
            if (!photoUploadRepository.attach(photoUpload.id(), garmentId, now)) {
                throw new IllegalStateException("A locked READY photo could not be attached");
            }
            if (!garmentPhotoAttachmentRepository.advanceRevisionIfCurrentAt(ownerAccountId, garmentId,
                    reviewedPair.expectedRevision())) {
                throw new IllegalStateException("A locked current garment could not be revised");
            }
            return new GarmentPhotoAttachment(garment.withAttachedPhoto(photoUpload.asGarmentPhoto()), true);
        });
    }

    /** Problems visible without reading anything. */
    private static ReviewedPair checkRequest(AttachGarmentPhotoCommand command) {
        List<GarmentFieldViolation> violations = new ArrayList<>();
        if (command.confirmed() == null) {
            violations.add(new GarmentFieldViolation(CONFIRMED_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!command.confirmed()) {
            violations.add(new GarmentFieldViolation(CONFIRMED_FIELD, GarmentValueProblem.NOT_CONFIRMED));
        }
        long expectedRevision = 0;
        String revisionToken = command.expectedRevision();
        if (revisionToken == null || revisionToken.isBlank()) {
            violations.add(new GarmentFieldViolation(EXPECTED_REVISION_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!REVISION_TOKEN.matcher(revisionToken).matches()) {
            violations.add(new GarmentFieldViolation(EXPECTED_REVISION_FIELD, GarmentValueProblem.INVALID_FORMAT));
        } else {
            expectedRevision = Long.parseLong(revisionToken);
        }
        UUID photoUploadId = null;
        String photoText = command.photoUploadId();
        if (photoText == null || photoText.isBlank()) {
            violations.add(new GarmentFieldViolation(PHOTO_UPLOAD_ID_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!CANONICAL_UUID.matcher(photoText).matches()) {
            violations.add(new GarmentFieldViolation(PHOTO_UPLOAD_ID_FIELD, GarmentValueProblem.INVALID_FORMAT));
        } else {
            photoUploadId = UUID.fromString(photoText);
        }
        if (!violations.isEmpty()) {
            throw new GarmentValidationException(violations);
        }
        return new ReviewedPair(expectedRevision, photoUploadId);
    }

    private static UUID garmentId(String garmentId) {
        if (garmentId == null || !CANONICAL_UUID.matcher(garmentId).matches()) {
            throw new GarmentNotFoundException();
        }
        return UUID.fromString(garmentId);
    }
}
