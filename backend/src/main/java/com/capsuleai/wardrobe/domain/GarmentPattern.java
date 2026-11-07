package com.capsuleai.wardrobe.domain;

/**
 * Garment Pattern: One optional value per garment (FR-GAR-009).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentPattern implements ControlledValue {

    SOLID("Solid"),
    STRIPED("Striped"),
    GRAPHIC("Graphic"),
    DOTTED("Dotted"),
    ANIMAL("Animal"),
    FLORAL("Floral"),
    TROPICAL("Tropical"),
    PAISLEY("Paisley"),
    ARGYLE("Argyle"),
    CAMO("Camo"),
    COLOR_BLOCK("Color-Block"),
    REPEATED("Repeated"),
    CHECKERBOARD("Checkerboard"),
    PLAID("Plaid"),
    GINGHAM("Gingham"),
    HOUNDSTOOTH("Houndstooth"),
    HERRINGBONE("Herringbone"),
    CHEVRON("Chevron"),
    TWEED("Tweed"),
    ABSTRACT("Abstract"),
    TIE_DYE("Tie Dye"),
    GEOMETRIC("Geometric"),
    LACE("Lace"),
    OTHER_PATTERNS("Other Patterns");

    private final String label;

    GarmentPattern(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}