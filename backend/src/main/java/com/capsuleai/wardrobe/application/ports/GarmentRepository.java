package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.Garment;
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
}