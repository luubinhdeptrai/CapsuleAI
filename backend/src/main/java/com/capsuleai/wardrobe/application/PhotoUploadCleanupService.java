package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * US-GAR-003 — the recoverable pending-photo lifecycle (S3-IMG-04/08): expires unconfirmed photos
 * whose pending TTL has passed and deletes the objects of discarded photos, retrying failed deletions
 * on later runs. Attached photos are never selected. Object deletion runs outside any transaction;
 * a crash between deletion and its record only repeats an idempotent deletion.
 */
public final class PhotoUploadCleanupService {

    /** What one run did. */
    public record CleanupResult(int expired, int deleted, int failed) {
    }

    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;
    private final WardrobeTransactions transactions;
    private final Clock clock;
    private final PhotoUploadPolicy policy;

    public PhotoUploadCleanupService(PhotoUploadRepository uploads, PhotoStorage storage,
                                     WardrobeTransactions transactions, Clock clock, PhotoUploadPolicy policy) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public CleanupResult run() {
        Instant now = now();
        int expired = transactions.inTransaction(() ->
                uploads.expireDue(now, policy.inFlightFence(), policy.cleanupBatchSize()));
        List<PhotoCleanupTarget> due = transactions.inTransaction(() ->
                uploads.claimDueCleanup(now, now.plus(policy.cleanupRetryDelay()), policy.cleanupBatchSize()));
        int deleted = 0;
        int failed = 0;
        for (PhotoCleanupTarget target : due) {
            try {
                storage.delete(target.objectKey());
                transactions.inTransaction(() -> uploads.markObjectDeleted(target.photoUploadId(), now()));
                deleted++;
            } catch (PhotoStorageUnavailableException unavailable) {
                failed++; // The lease expires and a later run retries this deletion.
            }
        }
        return new CleanupResult(expired, deleted, failed);
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}