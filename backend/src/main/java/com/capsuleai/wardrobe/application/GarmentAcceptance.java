package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-BULK-001 (S4-BULK-01/04, OPEN-S4-29) — the one rule set that turns a validated, explicitly confirmed
 * garment entry into exactly one accepted garment. Single additions ({@link AddGarmentService}) and bulk item
 * acceptance ({@link AcceptBulkImportItemService}) both use it, so there is one definition of acceptance.
 *
 * <p>It never opens a transaction: the caller runs it inside {@code WardrobeTransactions}, so the garment, its
 * sets, its creation key and (with a photo) the photo's READY → ATTACHED transition commit together with
 * whatever else the caller writes in that transaction, such as a bulk item becoming ACCEPTED. With a photo it
 * locks the stage first (after any batch and item locks the caller holds, S4-INT-01), then recognizes an
 * earlier acceptance of the same key, and otherwise requires a usable photo.
 */
final class GarmentAcceptance {

    private final GarmentRepository garmentRepository;
    private final PhotoUploadRepository photoUploadRepository;

    GarmentAcceptance(GarmentRepository garmentRepository, PhotoUploadRepository photoUploadRepository) {
        this.garmentRepository = Objects.requireNonNull(garmentRepository, "garmentRepository");
        this.photoUploadRepository = Objects.requireNonNull(photoUploadRepository, "photoUploadRepository");
    }

    /**
     * Must run inside the caller's Wardrobe transaction.
     *
     * @throws IdempotencyKeyReusedException   if the key was used for different values or another photo
     * @throws PhotoUploadNotFoundException    if the named staged photo is not the owner's
     * @throws PhotoUploadNotUsableException   if the staged photo is not READY, has expired or is attached
     * @throws GarmentNoLongerCurrentException if this key was accepted earlier and its garment has been removed
     */
    GarmentCreation acceptInCurrentTransaction(UUID ownerAccountId, ValidatedGarmentEntry entry,
                                               String creationFingerprint, Instant now) {
        if (entry.photoUploadId().isPresent()) {
            return acceptWithPhoto(ownerAccountId, entry, entry.photoUploadId().get(), creationFingerprint, now);
        }
        Garment candidate = Garment.confirmManualEntry(UUID.randomUUID(), ownerAccountId, entry.profile(), now);
        if (garmentRepository.addIfCreationKeyUnused(candidate, entry.creationKey(), creationFingerprint)) {
            return new GarmentCreation(candidate, true);
        }
        return replay(ownerAccountId, entry.creationKey(), creationFingerprint);
    }

    private GarmentCreation acceptWithPhoto(UUID ownerAccountId, ValidatedGarmentEntry entry, UUID photoUploadId,
                                            String creationFingerprint, Instant now) {
        // Lock first (when the row still exists), then look for an earlier acceptance: after a removal and its
        // cleanup the photo row is gone, but the replay must still be recognized (OPEN-S4-03).
        Optional<PhotoUpload> lockedUpload = photoUploadRepository.lockOwned(ownerAccountId, photoUploadId);
        if (garmentRepository.findCreation(ownerAccountId, entry.creationKey()).isPresent()) {
            return replay(ownerAccountId, entry.creationKey(), creationFingerprint);
        }
        PhotoUpload upload = lockedUpload.orElseThrow(PhotoUploadNotFoundException::new);
        if (!upload.isUsableAt(now)) {
            throw new PhotoUploadNotUsableException();
        }
        Garment candidate = Garment.confirmWithPhoto(UUID.randomUUID(), ownerAccountId, entry.profile(), now,
                upload.asGarmentPhoto());
        if (!garmentRepository.addIfCreationKeyUnused(candidate, entry.creationKey(), creationFingerprint)) {
            return replay(ownerAccountId, entry.creationKey(), creationFingerprint);
        }
        if (!photoUploadRepository.attach(upload.id(), candidate.id(), now)) {
            throw new IllegalStateException("A locked READY photo could not be attached");
        }
        return new GarmentCreation(candidate, true);
    }

    private GarmentCreation replay(UUID ownerAccountId, UUID creationKey, String creationFingerprint) {
        RecordedGarmentCreation earlier = garmentRepository.findCreation(ownerAccountId, creationKey)
                .orElseThrow(() -> new IllegalStateException("A used creation key has no recorded garment"));
        if (!earlier.creationFingerprint().equals(creationFingerprint)) {
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
