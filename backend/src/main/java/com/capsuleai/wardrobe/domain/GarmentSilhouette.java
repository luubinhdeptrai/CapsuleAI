package com.capsuleai.wardrobe.domain;

/**
 * Garment Silhouette: Optional; applies only to Pants (FR-GAR-007).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentSilhouette implements ControlledValue {

    FLARE("Flare"),
    SLIM("Slim"),
    TAPERED("Tapered"),
    BAGGY("Baggy"),
    SKINNY("Skinny"),
    BOOTCUT("Bootcut"),
    WIDE_LEG("Wide-leg");

    private final String label;

    GarmentSilhouette(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}