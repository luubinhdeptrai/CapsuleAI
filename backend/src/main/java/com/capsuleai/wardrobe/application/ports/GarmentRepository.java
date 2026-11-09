package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.Garment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Wardrobe-owned persistence of confirmed garments (DATA-GAR-001/002, ADA-016). */
public interface GarmentRepository {

    /**
     * Stores the garment, its materials and its logical-action key only if the
     * owner has not used that key before (ADA-009, DATA-INT-004).
     *
     * <p>Must be atomic under concurrency: of several additions with the same owner
     * and key running at the same time, exactly one may return {@code true}. Call
     * inside {@link WardrobeTransactions} so the garment and its materials commit together.
     *
     * @return {@code true} if stored; {@code false} if the key was already used (nothing changed)
     */
    boolean addIfCreationKeyUnused(Garment garment, UUID creationKey, String creationFingerprint);

    /** @return the garment the owner's earlier addition with this key created, if any */
    Optional<RecordedGarmentCreation> findCreation(UUID ownerAccountId, UUID creationKey);

    /**
     * One page of the owner's current garments (not removed), newest first with ties broken
     * by id, descending (US-WAR-001, OPEN-S2-10): at most {@code maximumCount} garments that
     * come strictly after {@code after} in that order, or from the newest when {@code after}
     * is empty. Read-only; one statement, so a page is one consistent snapshot.
     *
     * @return an empty list when no current garment follows the position
     */
    List<Garment> findCurrentByOwner(UUID ownerAccountId, Optional<WardrobePosition> after, int maximumCount);

    /**
     * One garment of this owner, current or removed (US-WAR-002). Another owner's
     * garment is never returned, so a guessed id reveals nothing. Read-only.
     */
    Optional<Garment> findOwned(UUID ownerAccountId, UUID garmentId);
}