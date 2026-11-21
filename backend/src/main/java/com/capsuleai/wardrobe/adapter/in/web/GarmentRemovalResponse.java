package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.GarmentRemoval;
import com.capsuleai.wardrobe.domain.RemovedGarment;
import java.util.UUID;

/**
 * The outcome of a confirmed removal (US-GAR-005, OPEN-S4-01). {@code outcome} is REMOVED when this request
 * removed the garment and ALREADY_REMOVED when it had been removed before (for example by an earlier
 * delivery of the same request); {@code removedAt} is always the original removal time. Only the minimal
 * historical meaning is returned: never Brand, Price, optional details or the photo.
 */
public record GarmentRemovalResponse(
        UUID id,
        String outcome,
        String state,
        String category,
        String color,
        String removedAt) {

    static final String REMOVED = "REMOVED";
    static final String ALREADY_REMOVED = "ALREADY_REMOVED";

    static GarmentRemovalResponse from(GarmentRemoval removal) {
        RemovedGarment removedGarment = removal.removedGarment();
        return new GarmentRemovalResponse(
                removedGarment.id(),
                removal.newlyRemoved() ? REMOVED : ALREADY_REMOVED,
                RemovedGarment.HISTORICAL_LABEL,
                removedGarment.category().name(),
                removedGarment.color().name(),
                removedGarment.removedAt().toString());
    }
}