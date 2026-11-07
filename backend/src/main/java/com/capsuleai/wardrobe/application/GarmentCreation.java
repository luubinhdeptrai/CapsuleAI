package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Garment;
import java.util.Objects;

/**
 * Outcome of an accepted addition: the owned garment, and whether this call
 * created it ({@code true}) or found it created by an earlier delivery of the
 * same logical addition ({@code false}). Either way there is exactly one garment.
 */
public record GarmentCreation(Garment garment, boolean newlyCreated) {

    public GarmentCreation {
        Objects.requireNonNull(garment, "garment");
    }
}