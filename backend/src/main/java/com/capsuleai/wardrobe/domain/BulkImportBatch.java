package com.capsuleai.wardrobe.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-BULK-001 (S4-BULK-02/06/07, OPEN-S4-25/26/28) — one explicitly started import batch of an owner: how many
 * items it queues, when its lifetime ends and whether the user stopped it. Only OPEN and STOPPED are stored;
 * EXPIRED is derived from {@code expiresAt}. A stopped or expired batch takes no new item work, but its
 * accepted items and every outcome stay readable.
 */
public record BulkImportBatch(
        UUID id,
        UUID ownerAccountId,
        UUID creationKey,
        int itemCount,
        BulkImportBatchStatus status,
        Instant createdAt,
        Instant expiresAt,
        Optional<Instant> stoppedAt) {

    public BulkImportBatch {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(creationKey, "creationKey");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(stoppedAt, "stoppedAt must be Optional.empty() while not stopped");
        if (status == BulkImportBatchStatus.EXPIRED) {
            throw new IllegalArgumentException("EXPIRED is derived from expiresAt and never stored");
        }
        if (itemCount < 1) {
            throw new IllegalArgumentException("A batch queues at least one item");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("A batch expires after it started");
        }
        if ((status == BulkImportBatchStatus.STOPPED) != stoppedAt.isPresent()) {
            throw new IllegalArgumentException("Exactly a stopped batch has a stop time");
        }
    }

    /** A new batch whose lifetime starts now. */
    public static BulkImportBatch open(UUID id, UUID ownerAccountId, UUID creationKey, int itemCount,
                                       Instant createdAt, Duration lifetime) {
        return new BulkImportBatch(id, ownerAccountId, creationKey, itemCount, BulkImportBatchStatus.OPEN,
                createdAt, createdAt.plus(lifetime), Optional.empty());
    }

    /** The status a reader sees at {@code now}: STOPPED, EXPIRED once the lifetime has ended, otherwise OPEN. */
    public BulkImportBatchStatus statusAt(Instant now) {
        if (status == BulkImportBatchStatus.STOPPED) {
            return BulkImportBatchStatus.STOPPED;
        }
        return now.isBefore(expiresAt) ? BulkImportBatchStatus.OPEN : BulkImportBatchStatus.EXPIRED;
    }

    public boolean acceptsItemWorkAt(Instant now) {
        return statusAt(now) == BulkImportBatchStatus.OPEN;
    }

    /** The stopped batch; stopping again keeps the first stop time. */
    public BulkImportBatch stop(Instant requestedAt) {
        if (status == BulkImportBatchStatus.STOPPED) {
            return this;
        }
        Instant stopTime = requestedAt.isBefore(createdAt) ? createdAt : requestedAt;
        return new BulkImportBatch(id, ownerAccountId, creationKey, itemCount, BulkImportBatchStatus.STOPPED,
                createdAt, expiresAt, Optional.of(stopTime));
    }
}
