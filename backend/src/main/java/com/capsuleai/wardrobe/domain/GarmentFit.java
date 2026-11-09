package com.capsuleai.wardrobe.domain;

/**
 * Garment Fit: Optional; applies only to Tops (FR-GAR-007).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentFit implements ControlledValue {

    SLIM("Slim"),
    REGULAR("Regular"),
    LOOSE("Loose"),
    OVERSIZED("Oversized");

    private final String label;

    GarmentFit(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}