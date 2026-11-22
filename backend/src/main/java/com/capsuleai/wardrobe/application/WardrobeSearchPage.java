package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** US-WAR-003 — one page of current garments matching the conditions, newest first, and what it means. */
public record WardrobeSearchPage(List<Garment> garments, Optional<WardrobePosition> next,
                                 WardrobeResultState resultState) {

    public WardrobeSearchPage {
        garments = List.copyOf(garments);
        Objects.requireNonNull(next, "next must be Optional.empty() on the last page");
        Objects.requireNonNull(resultState, "resultState");
    }
}