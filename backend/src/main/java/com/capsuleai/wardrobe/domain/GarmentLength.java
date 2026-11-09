package com.capsuleai.wardrobe.domain;

/**
 * Optional Length with separate Tops and Pants vocabularies (FR-GAR-007). Each
 * value belongs to one category.
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentLength implements ControlledValue {

    // Tops
    CROPPED(GarmentCategory.TOPS, "Cropped"),
    WAIST(GarmentCategory.TOPS, "Waist"),
    HIP(GarmentCategory.TOPS, "Hip"),
    KNEE(GarmentCategory.TOPS, "Knee"),

    // Pants
    SHORT(GarmentCategory.PANTS, "Short"),
    LONG(GarmentCategory.PANTS, "Long");

    private final GarmentCategory category;
    private final String label;

    GarmentLength(GarmentCategory category, String label) {
        this.category = category;
        this.label = label;
    }

    public GarmentCategory category() {
        return category;
    }

    @Override
    public String label() {
        return label;
    }
}