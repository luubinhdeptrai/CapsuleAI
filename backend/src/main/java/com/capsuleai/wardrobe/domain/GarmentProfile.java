package com.capsuleai.wardrobe.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The accepted, user-confirmed values of exactly the 13 approved garment
 * properties (DATA-GAR-002, BRULE-GAR-005). Category and Color are required;
 * every other property is optional, and an absent value is {@code Optional.empty()}
 * or an empty Material or Style set, never an invented UNKNOWN.
 *
 * <p>The constructor enforces category applicability; user input is validated
 * before construction so that invalid entries can be reported field by field.
 * Materials and Styles are sets in catalogue order, so repeated selections collapse
 * (FR-GAR-009/010; multiple Styles approved by the Product Owner, 2026-11-07).
 */
public record GarmentProfile(
        GarmentCategory category,
        Optional<GarmentSubtype> subtype,
        GarmentColor color,
        Set<GarmentMaterial> materials,
        Optional<GarmentPattern> pattern,
        Set<GarmentStyle> styles,
        Optional<GarmentFit> fit,
        Optional<GarmentNeckline> neckline,
        Optional<GarmentSleeveLength> sleeveLength,
        Optional<GarmentLength> length,
        Optional<GarmentSilhouette> silhouette,
        Optional<Brand> brand,
        Optional<PurchasePrice> price) {

    public GarmentProfile {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(subtype, "subtype must be Optional.empty() when absent");
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(materials, "materials must be empty when absent");
        Objects.requireNonNull(pattern, "pattern must be Optional.empty() when absent");
        Objects.requireNonNull(styles, "styles must be empty when absent");
        Objects.requireNonNull(fit, "fit must be Optional.empty() when absent");
        Objects.requireNonNull(neckline, "neckline must be Optional.empty() when absent");
        Objects.requireNonNull(sleeveLength, "sleeveLength must be Optional.empty() when absent");
        Objects.requireNonNull(length, "length must be Optional.empty() when absent");
        Objects.requireNonNull(silhouette, "silhouette must be Optional.empty() when absent");
        Objects.requireNonNull(brand, "brand must be Optional.empty() when absent");
        Objects.requireNonNull(price, "price must be Optional.empty() when absent");

        if (subtype.isPresent() && subtype.get().category() != category) {
            throw new IllegalArgumentException("The subtype belongs to another category");
        }
        requireApplicable(category, CategorySpecificProperty.FIT, fit);
        requireApplicable(category, CategorySpecificProperty.NECKLINE, neckline);
        requireApplicable(category, CategorySpecificProperty.SLEEVE_LENGTH, sleeveLength);
        requireApplicable(category, CategorySpecificProperty.LENGTH, length);
        requireApplicable(category, CategorySpecificProperty.SILHOUETTE, silhouette);
        if (length.isPresent() && length.get().category() != category) {
            throw new IllegalArgumentException("The length belongs to another category's list");
        }
        materials = distinctInCatalogueOrder(GarmentMaterial.class, materials);
        styles = distinctInCatalogueOrder(GarmentStyle.class, styles);
    }

    /** A profile with only the Saveable minimum (BRULE-GAR-002). */
    public static GarmentProfile minimum(GarmentCategory category, GarmentColor color) {
        return new GarmentProfile(category, Optional.empty(), color, Set.of(), Optional.empty(), Set.of(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    private static void requireApplicable(
            GarmentCategory category, CategorySpecificProperty property, Optional<?> value) {
        if (value.isPresent() && !category.accepts(property)) {
            throw new IllegalArgumentException(property + " does not apply to " + category);
        }
    }

    private static <E extends Enum<E>> Set<E> distinctInCatalogueOrder(Class<E> vocabulary, Collection<E> values) {
        EnumSet<E> distinct = EnumSet.noneOf(vocabulary);
        distinct.addAll(values);
        return Collections.unmodifiableSet(distinct);
    }
}