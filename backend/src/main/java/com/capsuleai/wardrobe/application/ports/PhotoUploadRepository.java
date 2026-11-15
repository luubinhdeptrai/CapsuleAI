package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Wardrobe-owned persistence of staged garment photos and their lifecycle (S3-IMG-04/08). Every
 * status change is a conditional statement, so concurrent confirmation, cancellation, expiry and
 * cleanup each see the outcome of the others and never act on a row in the wrong state.
 */
public interface PhotoUploadRepository {

    /** Records a RECEIVING intent unless the owner already used its upload key. @return true if recorded */
    boolean addIfUploadKeyUnused(PhotoUpload upload);

    Optional<PhotoUpload> findByUploadKey(UUID ownerAccountId, UUID uploadKey);

    /** The owner's staged photo in any status; another owner's is never returned. */
    Optional<PhotoUpload> findOwned(UUID ownerAccountId, UUID photoUploadId);

    /**
     * Like {@link #findOwned} but locks the row until the surrounding transaction ends, serializing
     * confirmation with other confirmations, cancellation and expiry. Call inside {@link WardrobeTransactions}.
     */
    Optional<PhotoUpload> lockOwned(UUID ownerAccountId, UUID photoUploadId);

    /** The photo attached to the owner's garment, if any. */
    Optional<PhotoUpload> findAttachedTo(UUID ownerAccountId, UUID garmentId);

    /** RECEIVING → READY with the stored photo's metadata. @return false if it is no longer RECEIVING */
    boolean markReady(UUID photoUploadId, String contentType, int width, int height, Instant readyAt);

    /** RECEIVING → DISCARDED after a failed store; deletion waits for the in-flight fence. */
    boolean discardReceiving(UUID photoUploadId, Instant now, Duration inFlightFence);

    /** The owner's RECEIVING or READY photo → DISCARDED (cancel). @return false if it was in another status */
    boolean cancel(UUID ownerAccountId, UUID photoUploadId, Instant now, Duration inFlightFence);

    /** A store finished after its photo was discarded: delete the object again (late-orphan fence). */
    void requeueCleanup(UUID photoUploadId, Instant now);

    /** READY → ATTACHED to the new garment. Call inside the creating transaction. @return false if not READY */
    boolean attach(UUID photoUploadId, UUID garmentId, Instant attachedAt);

    /** Discards up to {@code limit} pending photos whose expiry has passed. @return how many */
    int expireDue(Instant now, Duration inFlightFence, int limit);

    /**
     * Claims up to {@code limit} discarded photos whose deletion is due, leasing each until
     * {@code leaseUntil} so a concurrent or later run does not repeat it before then.
     */
    List<PhotoCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit);

    /** Records that the object of a discarded photo is deleted. @return false if already recorded */
    boolean markObjectDeleted(UUID photoUploadId, Instant deletedAt);
}