package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-005 (S4-GAR-02, OPEN-S4-02): the minimal historical meaning of a garment that left the owner's
 * current wardrobe. It deliberately carries only what makes a past reference understandable: the stable
 * id, Category, Color and the original removal time. It never exposes Brand, Price, optional details,
 * Materials, Styles or the photo, which are purged within 30 days of removal.
 *
 * <p>{@link #HISTORICAL_LABEL} is the code clients and future history consumers localize as
 * "Removed from wardrobe" (Vietnamese labels are a client concern).
 */
public record RemovedGarment(
        UUID id,
        UUID ownerAccountId,
        GarmentCategory category,
        GarmentColor color,
        Instant removedAt) {

    /** The label code of a removed garment in any historical reference (BRULE-HIST-002). */
    public static final String HISTORICAL_LABEL = "REMOVED_FROM_WARDROBE";

    public RemovedGarment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(removedAt, "removedAt");
    }

    /** The minimal meaning of a removed garment; a current garment has none. */
    public static RemovedGarment of(Garment garment) {
        Objects.requireNonNull(garment, "garment");
        if (garment.isCurrent()) {
            throw new IllegalArgumentException("A current garment has no removed representation");
        }
        return new RemovedGarment(garment.id(), garment.ownerAccountId(), garment.profile().category(),
                garment.profile().color(), garment.removedAt());
    }
}