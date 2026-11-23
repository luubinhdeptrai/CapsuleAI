package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository.BatchOverview;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.BulkImportBatch;
import com.capsuleai.wardrobe.domain.BulkImportItem;
import com.capsuleai.wardrobe.domain.BulkImportItemStatus;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-BULK-001 — explicit confirmation of one reviewable import item (FR-BULK-003/004/006; S4-BULK-01/04/05,
 * OPEN-S4-27/29). The reviewed values go through the same validation as a single addition: all 13 properties,
 * distinct Materials and Styles, category applicability, Category and one Color required.
 *
 * <p>In one Wardrobe transaction: the batch is held in share mode, the item exclusively, then the item's own
 * staged photo; the shared {@link GarmentAcceptance} creates the garment under the item's acceptance key and
 * attaches the photo; the item becomes ACCEPTED and no longer names the stage. All or nothing: a failure
 * leaves the item as it was. Each item is independent; other items are never touched.
 *
 * <p>An already accepted item answers with its current state when the same values are confirmed again (a
 * retry after a lost response), even after its garment was removed (garmentCurrent false); nothing is created
 * again. Other values for an accepted item are refused as closed.
 */
public final class AcceptBulkImportItemService {

    private final BulkImportRepository bulkImportRepository;
    private final GarmentRepository garmentRepository;
    private final PhotoUploadRepository photoUploadRepository;
    private final GarmentAcceptance garmentAcceptance;
    private final WardrobeTransactions wardrobeTransactions;
    private final Clock clock;

    public AcceptBulkImportItemService(BulkImportRepository bulkImportRepository,
                                       GarmentRepository garmentRepository,
                                       PhotoUploadRepository photoUploadRepository,
                                       WardrobeTransactions wardrobeTransactions, Clock clock) {
        this.bulkImportRepository = Objects.requireNonNull(bulkImportRepository, "bulkImportRepository");
        this.garmentRepository = Objects.requireNonNull(garmentRepository, "garmentRepository");
        this.photoUploadRepository = Objects.requireNonNull(photoUploadRepository, "photoUploadRepository");
        this.garmentAcceptance = new GarmentAcceptance(garmentRepository, photoUploadRepository);
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws GarmentValidationException       if the confirmation, the revision or a value is missing or invalid
     *                                          (checked before anything is read; nothing changed)
     * @throws BulkImportNotFoundException      if the owner has no such batch or item
     * @throws BulkImportClosedException        if the batch is stopped or expired, the item was cancelled, or the
     *                                          item was accepted with other values
     * @throws BulkImportItemConflictException  if the item changed after the user read it
     * @throws BulkImportItemNotReadyException  if no photo is linked yet
     * @throws PhotoUploadNotUsableException    if the linked photo can no longer be used (select it again)
     */
    public BatchOverview accept(AcceptBulkImportItemCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        List<GarmentFieldViolation> violations = new ArrayList<>();
        long expectedRevision = BulkImportService.expectedRevision(command.expectedRevision(), violations);
        BulkImportService.requireConfirmation(command.confirmed(), violations);
        Optional<GarmentProfile> reviewedProfile = reviewedProfile(command.values(), violations);
        BulkImportService.rejectIfAny(violations);
        UUID batchId = BulkImportService.pathId(command.batchId());
        UUID itemId = BulkImportService.pathId(command.itemId());
        GarmentProfile profile = reviewedProfile.orElseThrow();
        // The item fixes the photo, never the request, so the reviewed values alone identify the confirmation.
        String creationFingerprint = GarmentCreationFingerprint.of(profile);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);

        return wardrobeTransactions.inTransaction(() -> {
            BulkImportBatch batch = bulkImportRepository.lockForItemWork(ownerAccountId, batchId)
                    .orElseThrow(BulkImportNotFoundException::new);
            BulkImportItem item = bulkImportRepository.lockItem(batchId, itemId)
                    .orElseThrow(BulkImportNotFoundException::new);
            if (item.status() == BulkImportItemStatus.ACCEPTED) {
                return replay(ownerAccountId, batchId, item, creationFingerprint, now);
            }
            BulkImportService.requireItemWork(batch, now);
            BulkImportService.requireRevision(item, expectedRevision);
            if (item.status() == BulkImportItemStatus.CANCELED) {
                throw new BulkImportClosedException();
            }
            if (item.status() == BulkImportItemStatus.AWAITING_PHOTO) {
                throw new BulkImportItemNotReadyException();
            }
            UUID linkedPhotoId = item.photoUploadId().orElseThrow();
            Optional<PhotoUpload> linkedPhoto = photoUploadRepository.lockOwned(ownerAccountId, linkedPhotoId);
            if (linkedPhoto.isEmpty() || !linkedPhoto.get().isUsableAt(now)) {
                throw new PhotoUploadNotUsableException();
            }
            ValidatedGarmentEntry entry = new ValidatedGarmentEntry(item.acceptanceKey(), profile,
                    Optional.of(linkedPhotoId));
            GarmentCreation creation = garmentAcceptance.acceptInCurrentTransaction(ownerAccountId, entry,
                    creationFingerprint, now);
            if (!bulkImportRepository.update(item.accepted(creation.garment().id(), now), item.revision())) {
                throw new IllegalStateException("A locked import item could not be accepted");
            }
            return overview(ownerAccountId, batchId, now);
        });
    }

    /** All violations of the reviewed values, merged with the request's own; empty when any was found. */
    private static Optional<GarmentProfile> reviewedProfile(GarmentValues values,
                                                            List<GarmentFieldViolation> violations) {
        try {
            return Optional.of(GarmentEntryValidator.validateProfile(values));
        } catch (GarmentValidationException rejected) {
            violations.addAll(rejected.violations());
            return Optional.empty();
        }
    }

    private BatchOverview replay(UUID ownerAccountId, UUID batchId, BulkImportItem acceptedItem,
                                 String creationFingerprint, Instant now) {
        RecordedGarmentCreation earlier = garmentRepository.findCreation(ownerAccountId, acceptedItem.acceptanceKey())
                .orElseThrow(() -> new IllegalStateException("An accepted import item has no recorded garment"));
        if (!earlier.creationFingerprint().equals(creationFingerprint)) {
            throw new BulkImportClosedException();
        }
        return overview(ownerAccountId, batchId, now);
    }

    private BatchOverview overview(UUID ownerAccountId, UUID batchId, Instant now) {
        return bulkImportRepository.findOverview(ownerAccountId, batchId, now)
                .orElseThrow(() -> new IllegalStateException("A locked import batch could not be read"));
    }
}
