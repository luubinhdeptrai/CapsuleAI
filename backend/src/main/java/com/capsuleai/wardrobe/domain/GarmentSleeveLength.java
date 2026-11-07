package com.capsuleai.wardrobe.domain;

/**
 * Garment Sleeve Length: Optional; applies only to Tops (FR-GAR-007).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentSleeveLength implements ControlledValue {

    SLEEVELESS("Sleeveless"),
    CAP_SLEEVE("Cap Sleeve"),
    SHORT_SLEEVE("Short Sleeve"),
    THREE_QUARTER_SLEEVE("3/4 Sleeve"),
    LONG_SLEEVE("Long Sleeve");

    private final String label;

    GarmentSleeveLength(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}