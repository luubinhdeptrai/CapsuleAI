package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRemovalCleanupTarget;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.domain.Garment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory fake of {@link GarmentRemovalRepository} for application tests. Removal is recorded through
 * {@link InMemoryGarmentRepository}'s existing test helper, which keeps the revision unchanged; the real
 * revision increment is verified against PostgreSQL (GarmentRemovalApiIT, ConcurrentGarmentRemovalIT).
 */
final class InMemoryGarmentRemovalRepository implements GarmentRemovalRepository {

    /** One recorded obligation and its progress. */
    static final class Obligation {
        final UUID garmentId;
        final UUID ownerAccountId;
        final Instant removedAt;
        final Instant purgeDeadline;
        final Optional<PhotoCleanupTarget> attachedPhoto;
        Instant nextAttemptAt;
        int attempts;
        boolean dataMinimized;
        boolean objectDeleted;
        boolean completed;

        Obligation(UUID garmentId, UUID ownerAccountId, Instant removedAt, Instant purgeDeadline,
                   Optional<PhotoCleanupTarget> attachedPhoto) {
            this.garmentId = garmentId;
            this.ownerAccountId = ownerAccountId;
            this.removedAt = removedAt;
            this.purgeDeadline = purgeDeadline;
            this.attachedPhoto = attachedPhoto;
            this.nextAttemptAt = removedAt;
        }
    }

    private final InMemoryGarmentRepository garmentRepository;
    private final Map<UUID, PhotoCleanupTarget> attachedPhotos = new HashMap<>();
    private final Map<UUID, Obligation> obligations = new LinkedHashMap<>();
    private Instant competingRemovalTime;
    private boolean crashOnNextObjectDeletionRecord;
    private final Set<UUID> garmentsWhoseMinimizationFails = new HashSet<>();

    InMemoryGarmentRemovalRepository(InMemoryGarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
    }

    @Override
    public synchronized boolean removeIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision,
                                                  Instant removedAt) {
        if (competingRemovalTime != null) {
            garmentRepository.remove(garmentId, competingRemovalTime);
            competingRemovalTime = null;
            return false;
        }
        Optional<Garment> garment = garmentRepository.findOwned(ownerAccountId, garmentId);
        if (garment.isEmpty() || !garment.get().isCurrent() || garment.get().revision() != expectedRevision) {
            return false;
        }
        garmentRepository.remove(garmentId, removedAt);
        return true;
    }

    @Override
    public synchronized Optional<PhotoCleanupTarget> findAttachedPhoto(UUID garmentId) {
        return Optional.ofNullable(attachedPhotos.get(garmentId));
    }

    @Override
    public synchronized void recordCleanup(UUID garmentId, UUID ownerAccountId, Instant removedAt,
                                           Instant purgeDeadline, Optional<PhotoCleanupTarget> attachedPhoto) {
        obligations.putIfAbsent(garmentId,
                new Obligation(garmentId, ownerAccountId, removedAt, purgeDeadline, attachedPhoto));
    }

    @Override
    public synchronized List<GarmentRemovalCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
        List<Obligation> due = obligations.values().stream()
                .filter(obligation -> !obligation.completed && !obligation.nextAttemptAt.isAfter(now))
                .sorted(Comparator.comparing(obligation -> obligation.nextAttemptAt))
                .limit(limit)
                .toList();
        List<GarmentRemovalCleanupTarget> claimed = new ArrayList<>();
        for (Obligation obligation : due) {
            obligation.nextAttemptAt = leaseUntil;
            obligation.attempts++;
            claimed.add(new GarmentRemovalCleanupTarget(obligation.garmentId, obligation.removedAt,
                    obligation.purgeDeadline, obligation.attachedPhoto.map(PhotoCleanupTarget::objectKey),
                    obligation.dataMinimized, obligation.objectDeleted));
        }
        return claimed;
    }

    @Override
    public synchronized boolean minimizeRemovedGarment(UUID garmentId, Instant minimizedAt) {
        if (garmentsWhoseMinimizationFails.contains(garmentId)) {
            throw new IllegalStateException("simulated permanent failure while minimizing one garment");
        }
        Obligation obligation = obligations.get(garmentId);
        if (obligation == null || obligation.dataMinimized) {
            return false;
        }
        obligation.dataMinimized = true;
        return true;
    }

    @Override
    public synchronized boolean markObjectDeleted(UUID garmentId, Instant deletedAt) {
        if (crashOnNextObjectDeletionRecord) {
            crashOnNextObjectDeletionRecord = false;
            throw new IllegalStateException("simulated crash after the object was deleted");
        }
        Obligation obligation = obligations.get(garmentId);
        if (obligation == null || obligation.attachedPhoto.isEmpty() || obligation.objectDeleted) {
            return false;
        }
        obligation.objectDeleted = true;
        return true;
    }

    @Override
    public synchronized boolean completeIfFinished(UUID garmentId, Instant completedAt) {
        Obligation obligation = obligations.get(garmentId);
        if (obligation == null || obligation.completed || !obligation.dataMinimized
                || (obligation.attachedPhoto.isPresent() && !obligation.objectDeleted)) {
            return false;
        }
        obligation.completed = true;
        return true;
    }

    @Override
    public synchronized int countOverdue(Instant removedBefore) {
        return (int) obligations.values().stream()
                .filter(obligation -> !obligation.completed && !obligation.removedAt.isAfter(removedBefore))
                .count();
    }

    /** TEST HELPER: the garment has this attached photo when it is removed. */
    synchronized void attachPhoto(UUID garmentId, PhotoCleanupTarget photo) {
        attachedPhotos.put(garmentId, photo);
    }

    /** TEST HELPER: a competing removal at {@code removalTime} commits just before the next conditional removal. */
    synchronized void removeConcurrentlyBeforeNextAttempt(Instant removalTime) {
        competingRemovalTime = removalTime;
    }

    /**
     * TEST HELPER: the process stops after the next photo object is deleted but before its deletion is
     * recorded, as a crash or a lost database connection would.
     */
    synchronized void crashOnNextObjectDeletionRecord() {
        crashOnNextObjectDeletionRecord = true;
    }

    /** TEST HELPER: minimizing this garment always fails, like data the minimization cannot process. */
    synchronized void failMinimizationOf(UUID garmentId) {
        garmentsWhoseMinimizationFails.add(garmentId);
    }

    synchronized Optional<Obligation> obligation(UUID garmentId) {
        return Optional.ofNullable(obligations.get(garmentId));
    }

    synchronized int obligationCount() {
        return obligations.size();
    }
}
