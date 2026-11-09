package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One page of the current wardrobe, newest first. {@code next} is present exactly when
 * at least one more current garment followed this page when it was read; it is the
 * position to continue after (OPEN-S2-10).
 */
public record WardrobePage(List<Garment> garments, Optional<WardrobePosition> next) {

    public WardrobePage {
        garments = List.copyOf(garments);
        Objects.requireNonNull(next, "next must be Optional.empty() on the last page");
    }
}