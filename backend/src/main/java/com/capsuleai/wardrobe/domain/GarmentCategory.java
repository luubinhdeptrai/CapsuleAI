package com.capsuleai.wardrobe.domain;

/**
 * Primary garment classification: exactly eight categories (FR-GAR-001,
 * BRULE-GAR-004). The constant name is the approved category identifier.
 */
public enum GarmentCategory implements ControlledValue {

    TOPS("Tops"),
    DRESSES_JUMPSUITS("Dresses/Jumpsuits"),
    PANTS("Pants"),
    SKIRTS("Skirts"),
    OUTERWEAR("Outerwear"),
    SHOES("Shoes"),
    BAGS("Bags"),
    HEADWEAR("Headwear");

    private final String label;

    GarmentCategory(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    /**
     * Whether a category-specific property may be supplied for this category
     * (FR-GAR-007): Fit, Neckline and Sleeve Length for Tops, Length for Tops and
     * Pants, Silhouette for Pants. Global properties apply to every category.
     */
    public boolean accepts(CategorySpecificProperty property) {
        return switch (property) {
            case FIT, NECKLINE, SLEEVE_LENGTH -> this == TOPS;
            case LENGTH -> this == TOPS || this == PANTS;
            case SILHOUETTE -> this == PANTS;
        };
    }
}