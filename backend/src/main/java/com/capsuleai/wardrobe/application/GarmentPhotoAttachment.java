package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Garment;
import java.util.Objects;

/**
 * Outcome of an accepted attachment (provisional US-GAR-006, OPEN-S4-07): the garment as it now is, and
 * whether this request attached the photo ({@code false}: the same pair was already attached, for example
 * by an earlier delivery of the same request; nothing changed and the revision is the same).
 */
public record GarmentPhotoAttachment(Garment garment, boolean newlyAttached) {

    public GarmentPhotoAttachment {
        Objects.requireNonNull(garment, "garment");
    }
}