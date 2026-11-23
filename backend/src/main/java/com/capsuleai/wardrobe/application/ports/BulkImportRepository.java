package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.BulkImportBatch;
import com.capsuleai.wardrobe.domain.BulkImportBatchStatus;
import com.capsuleai.wardrobe.domain.BulkImportItem;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Wardrobe-owned persistence of import batches and items (US-BULK-001, V15). Lock methods read one table each
 * and must be called inside {@link WardrobeTransactions}; the canonical order is batch, then item, then staged
 * photo, then garment (S4-INT-01, OPEN-S4-28). Overviews are fresh reads joined with the staged photo and the
 * garment, never used for locking.
 */
public interface BulkImportRepository {

    /** How a linked staged photo can be used now. Derived when read, never stored (OPEN-S4-26). */
    enum LinkedPhotoState {

        /** No staged photo is linked. */
        NONE,

        /** The linked photo is READY and unexpired: the item can be confirmed. */
        READY,

        /** The linked photo can no longer be used for this item (expired, cancelled or used elsewhere): select a photo again. */
        EXPIRED
    }

    /** One item as a reader sees it: its state, its photo usability and, once accepted, whether its garment is current. */
    record ItemOverview(BulkImportItem item, LinkedPhotoState photoState, Optional<Boolean> garmentCurrent) {

        public ItemOverview {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(photoState, "photoState");
            Objects.requireNonNull(garmentCurrent, "garmentCurrent");
        }
    }

    /** One batch as a reader sees it at a given time, with every item in queue order. */
    record BatchOverview(BulkImportBatch batch, BulkImportBatchStatus currentStatus, List<ItemOverview> items) {

        public BatchOverview {
            Objects.requireNonNull(batch, "batch");
            Objects.requireNonNull(currentStatus, "currentStatus");
            items = List.copyOf(items);
        }
    }

    /** Serializes the batch admissions of one owner until the transaction ends (limit check plus insert). */
    void lockAdmission(UUID ownerAccountId);

    Optional<BulkImportBatch> findByCreationKey(UUID ownerAccountId, UUID creationKey);

    /** The owner's OPEN batches whose lifetime has not ended at {@code now}. */
    int countActive(UUID ownerAccountId, Instant now);

    /** Stores a new batch and all its items. Call inside {@link WardrobeTransactions}. */
    void add(BulkImportBatch batch, List<BulkImportItem> items);

    /** The owner's batch, held in share mode: item commands run in parallel, a stop waits for them. */
    Optional<BulkImportBatch> lockForItemWork(UUID ownerAccountId, UUID batchId);

    /** The owner's batch, held exclusively: item commands wait until the stop commits. */
    Optional<BulkImportBatch> lockForStop(UUID ownerAccountId, UUID batchId);

    /** One item of the batch, held exclusively. */
    Optional<BulkImportItem> lockItem(UUID batchId, UUID itemId);

    /** The batch's AWAITING_PHOTO and REVIEWABLE items, held exclusively, in queue order. */
    List<BulkImportItem> lockUnfinishedItems(UUID batchId);

    /** Whether another item already names this staged photo. */
    boolean isPhotoLinkedToAnotherItem(UUID photoUploadId, UUID itemId);

    /** Writes the item's next state only while its stored revision is {@code expectedRevision}. @return false if not */
    boolean update(BulkImportItem item, long expectedRevision);

    /** OPEN → STOPPED. @return false if the batch was not OPEN */
    boolean markStopped(UUID batchId, Instant stoppedAt);

    Optional<BatchOverview> findOverview(UUID ownerAccountId, UUID batchId, Instant now);

    /** The owner's active batches (resume list), newest first. */
    List<BatchOverview> findActiveOverviews(UUID ownerAccountId, Instant now);
}
