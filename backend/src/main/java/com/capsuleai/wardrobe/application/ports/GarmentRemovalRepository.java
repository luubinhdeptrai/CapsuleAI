package com.capsuleai.wardrobe.application.ports;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * US-GAR-005 (S4-GAR-01/03): Wardrobe-owned persistence of confirmed removal and of the durable removal
 * cleanup obligation. Every state change is a conditional statement, so concurrent removals, edits,
 * attachments and cleanup runs each see the others' committed outcome.
 *
 * <p>Lock order (S4-INT-01): garment, then photo, then cleanup work. Call every method except
 * {@link #countOverdue} inside {@link WardrobeTransactions}.
 */
public interface GarmentRemovalRepository {

    /**
     * Removes the owner's garment from the current wardrobe only while it is current and at
     * {@code expectedRevision}; the revision advances by one. The garment row stays locked until the
     * surrounding transaction ends.
     *
     * @return {@code false} if the garment is unknown, another owner's, already removed or at another
     *         revision; nothing changed
     */
    boolean removeIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision, Instant removedAt);

    /** The photo attached to the garment, if any. Read while the caller holds the garment row lock. */
    Optional<PhotoCleanupTarget> findAttachedPhoto(UUID garmentId);

    /**
     * Records the cleanup obligation of a just-removed garment. Recorded at most once per garment: a
     * second call changes nothing, so the retention clock never restarts.
     */
    void recordCleanup(UUID garmentId, UUID ownerAccountId, Instant removedAt, Instant purgeDeadline,
                       Optional<PhotoCleanupTarget> attachedPhoto);

    /**
     * Claims up to {@code limit} incomplete obligations that are due, leasing each until
     * {@code leaseUntil} so that a concurrent or later run does not repeat it before then.
     */
    List<GarmentRemovalCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit);

    /**
     * Purges the removed garment's rich data: every optional property, Brand and Price, its Materials
     * and Styles, and its attached photo row. Category, Color, identity and creation identity remain.
     *
     * @return {@code true} if this call recorded the minimization; {@code false} if it was already recorded
     */
    boolean minimizeRemovedGarment(UUID garmentId, Instant minimizedAt);

    /** Records that the obligation's photo object is deleted. @return false if already recorded or no photo */
    boolean markObjectDeleted(UUID garmentId, Instant deletedAt);

    /** Completes the obligation if every step is done. @return true if this call completed it */
    boolean completeIfFinished(UUID garmentId, Instant completedAt);

    /** How many obligations are still incomplete although their garment was removed at or before {@code removedBefore}. */
    int countOverdue(Instant removedBefore);
}