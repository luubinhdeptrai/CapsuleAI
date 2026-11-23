package com.capsuleai.wardrobe.application;

import java.time.Duration;
import java.util.Objects;

/**
 * US-BULK-001 admission and lifecycle policy (S4-BULK-06, approved 2026-11-16). The values are OPEN-S4-25
 * delegated defaults supplied by configuration; Product Owner review is advised.
 *
 * @param maximumItemsPerBatch items one batch may queue (default 20)
 * @param maximumActiveBatches open, unexpired batches one owner may have at a time (default 3)
 * @param batchLifetime        after this, a batch takes no new item work (default P7D)
 */
public record BulkImportPolicy(int maximumItemsPerBatch, int maximumActiveBatches, Duration batchLifetime) {

    /** The V15 database ceiling for items per batch; configuration may not exceed it. */
    public static final int ITEM_CEILING = 100;

    public BulkImportPolicy {
        Objects.requireNonNull(batchLifetime, "batchLifetime");
        if (maximumItemsPerBatch < 1 || maximumItemsPerBatch > ITEM_CEILING) {
            throw new IllegalArgumentException("Items per batch must be between 1 and " + ITEM_CEILING);
        }
        if (maximumActiveBatches < 1) {
            throw new IllegalArgumentException("At least one active batch must be allowed");
        }
        if (batchLifetime.isNegative() || batchLifetime.isZero()) {
            throw new IllegalArgumentException("The batch lifetime must be positive");
        }
    }

    public boolean admitsItemCount(int itemCount) {
        return itemCount >= 1 && itemCount <= maximumItemsPerBatch;
    }

    public boolean admitsAnotherBatch(int activeBatches) {
        return activeBatches < maximumActiveBatches;
    }

    /**
     * The original bytes one batch can bring in at most: the item limit times the unchanged per-photo limit
     * (20 × 15,000,000 = 300,000,000 by default). It is enforced by those two rules together; no separate byte
     * counter exists (OPEN-S4-25).
     */
    public long maximumOriginalBytesPerBatch(long maximumOriginalBytesPerPhoto) {
        return Math.multiplyExact((long) maximumItemsPerBatch, maximumOriginalBytesPerPhoto);
    }
}
