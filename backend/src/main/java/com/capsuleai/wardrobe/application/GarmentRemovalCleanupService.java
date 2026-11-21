package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRemovalCleanupTarget;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * US-GAR-005 — the durable removal cleanup (S4-GAR-03, OPEN-S4-04). Each run claims due obligations,
 * minimizes each removed garment's data in a short transaction, deletes its private photo object outside
 * any transaction, records each step and completes the obligation when everything is done.
 *
 * <p>Separate from the pending-photo lifecycle: it never touches RECEIVING, READY or DISCARDED uploads,
 * and pending expiry never touches the garments it cleans. A storage failure leaves the lease to expire,
 * so a later run retries the deletion; a crash between deleting the object and recording it only repeats
 * an idempotent delete. The deadline was fixed at removal and is never extended by a retry.
 */
public final class GarmentRemovalCleanupService {

    /** What one run did. {@code overdue} counts incomplete obligations older than the warning age. */
    public record CleanupResult(int minimized, int objectsDeleted, int completed, int failed, int overdue) {
    }

    private final GarmentRemovalRepository garmentRemovalRepository;
    private final PhotoStorage photoStorage;
    private final WardrobeTransactions wardrobeTransactions;
    private final Clock clock;
    private final GarmentRemovalPolicy removalPolicy;

    public GarmentRemovalCleanupService(GarmentRemovalRepository garmentRemovalRepository, PhotoStorage photoStorage,
                                        WardrobeTransactions wardrobeTransactions, Clock clock,
                                        GarmentRemovalPolicy removalPolicy) {
        this.garmentRemovalRepository = Objects.requireNonNull(garmentRemovalRepository, "garmentRemovalRepository");
        this.photoStorage = Objects.requireNonNull(photoStorage, "photoStorage");
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.removalPolicy = Objects.requireNonNull(removalPolicy, "removalPolicy");
    }

    public CleanupResult run() {
        Instant runStart = now();
        List<GarmentRemovalCleanupTarget> dueObligations = wardrobeTransactions.inTransaction(() ->
                garmentRemovalRepository.claimDueCleanup(runStart, runStart.plus(removalPolicy.cleanupRetryDelay()),
                        removalPolicy.cleanupBatchSize()));
        int minimized = 0;
        int objectsDeleted = 0;
        int completed = 0;
        int failed = 0;
        for (GarmentRemovalCleanupTarget obligation : dueObligations) {
            if (!obligation.dataMinimized() && wardrobeTransactions.inTransaction(() ->
                    garmentRemovalRepository.minimizeRemovedGarment(obligation.garmentId(), now()))) {
                minimized++;
            }
            if (obligation.needsObjectDeletion()) {
                try {
                    photoStorage.delete(obligation.photoObjectKey().orElseThrow());
                } catch (PhotoStorageUnavailableException unavailable) {
                    failed++; // The lease expires and a later run retries this deletion.
                    continue;
                }
                if (wardrobeTransactions.inTransaction(() ->
                        garmentRemovalRepository.markObjectDeleted(obligation.garmentId(), now()))) {
                    objectsDeleted++;
                }
            }
            if (wardrobeTransactions.inTransaction(() ->
                    garmentRemovalRepository.completeIfFinished(obligation.garmentId(), now()))) {
                completed++;
            }
        }
        int overdue = wardrobeTransactions.inTransaction(() ->
                garmentRemovalRepository.countOverdue(runStart.minus(removalPolicy.overdueWarningAge())));
        return new CleanupResult(minimized, objectsDeleted, completed, failed, overdue);
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}