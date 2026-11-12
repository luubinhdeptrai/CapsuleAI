package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Garment;
import java.util.Objects;

/**
 * Outcome of an accepted edit: the garment as it now is, and whether this edit changed its
 * profile ({@code false}: the confirmed values equalled the accepted ones, so nothing was
 * written and the revision is unchanged; OPEN-S3-06).
 */
public record GarmentRevision(Garment garment, boolean changed) {

    public GarmentRevision {
        Objects.requireNonNull(garment, "garment");
    }
}