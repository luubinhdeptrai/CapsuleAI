package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.Garment;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A place in the newest-first order of the current wardrobe: the creation time and id
 * of the last garment a page returned (keyset pagination, OPEN-S2-10). Ids are unique,
 * so the order (createdAt descending, id descending) is total and a position is exact.
 */
public record WardrobePosition(Instant createdAt, UUID garmentId) {

    public WardrobePosition {
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(garmentId, "garmentId");
    }

    public static WardrobePosition of(Garment garment) {
        return new WardrobePosition(garment.createdAt(), garment.id());
    }
}