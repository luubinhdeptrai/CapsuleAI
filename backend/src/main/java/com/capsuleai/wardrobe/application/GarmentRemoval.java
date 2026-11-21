package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.RemovedGarment;
import java.util.Objects;

/**
 * Outcome of a confirmed removal (US-GAR-005, OPEN-S4-01): the garment's minimal removed meaning and
 * whether this request removed it ({@code true}) or found it already removed ({@code false}, for example
 * when the response of an earlier identical request was lost). Either way the garment is not current.
 */
public record GarmentRemoval(RemovedGarment removedGarment, boolean newlyRemoved) {

    public GarmentRemoval {
        Objects.requireNonNull(removedGarment, "removedGarment");
    }
}
