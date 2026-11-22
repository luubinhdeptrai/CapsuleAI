package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * US-WAR-003 — the search the repository executes (OPEN-S4-11/13): for the text condition, the controlled
 * values whose label or code contains the text plus the literal Brand text; and the exact category and
 * color filters. An empty filter set means no restriction; an empty code set inside a text match means no
 * value of that vocabulary matched (the garment may still match through another field).
 */
public record WardrobeSearchQuery(Optional<TextMatch> textMatch, Set<GarmentCategory> categories,
                                  Set<GarmentColor> colors) {

    /** What the text condition matches; a garment matches when any of these fields does (OR). */
    public record TextMatch(Set<GarmentCategory> categories, Set<GarmentSubtype> subtypes, Set<GarmentColor> colors,
                            Set<GarmentMaterial> materials, Set<GarmentPattern> patterns, Set<GarmentStyle> styles,
                            String brandText) {

        public TextMatch {
            categories = Set.copyOf(categories);
            subtypes = Set.copyOf(subtypes);
            colors = Set.copyOf(colors);
            materials = Set.copyOf(materials);
            patterns = Set.copyOf(patterns);
            styles = Set.copyOf(styles);
            Objects.requireNonNull(brandText, "brandText");
        }
    }

    public WardrobeSearchQuery {
        Objects.requireNonNull(textMatch, "textMatch must be Optional.empty() without a text condition");
        categories = Set.copyOf(categories);
        colors = Set.copyOf(colors);
    }

    public boolean restrictsText() {
        return textMatch.isPresent();
    }

    public boolean restrictsCategories() {
        return !categories.isEmpty();
    }

    public boolean restrictsColors() {
        return !colors.isEmpty();
    }
}