package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.application.WardrobeSearchQuery;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * US-WAR-003 (S4-WAR-02/03): Wardrobe-owned read access for searches with at least one condition. Every
 * result is the owner's current garment; removed and other owners' garments never match. The order is
 * creation time, then id, both descending, as in the overview.
 */
public interface WardrobeSearchRepository {

    /**
     * Up to {@code maximumCount} matching current garments, strictly after {@code after} when present.
     *
     * @param query a query with at least one condition (an unrestricted overview uses {@link GarmentRepository})
     */
    List<Garment> findCurrentMatching(UUID ownerAccountId, WardrobeSearchQuery query,
                                      Optional<WardrobePosition> after, int maximumCount);

    /** Whether the owner has at least one current garment (no match versus empty wardrobe). */
    boolean hasCurrentGarment(UUID ownerAccountId);
}