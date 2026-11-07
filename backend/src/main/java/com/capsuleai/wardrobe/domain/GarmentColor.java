package com.capsuleai.wardrobe.domain;

/**
 * Garment Color: Exactly one dominant Color per garment; Colorful is the listed value for multicolored garments (FR-GAR-008).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentColor implements ControlledValue {

    WHITE("White"),
    IVORY("Ivory"),
    BEIGE("Beige"),
    LIGHT_GRAY("Light-Gray"),
    DARK_GRAY("Dark-Gray"),
    BLACK("Black"),
    LIGHT_YELLOW("Light-Yellow"),
    YELLOW("Yellow"),
    TURMERIC("Turmeric"),
    ORANGE("Orange"),
    CORAL("Coral"),
    RED("Red"),
    PINK("Pink"),
    HOT_PINK("Hot-Pink"),
    LIGHT_GREEN("Light-Green"),
    GREEN("Green"),
    OLIVE("Olive"),
    DARK_OLIVE("Dark-Olive"),
    TEAL("Teal"),
    KHAKI("Khaki"),
    CYAN("Cyan"),
    SKY_BLUE("Sky-Blue"),
    BLUE("Blue"),
    NAVY("Navy"),
    LAVENDER("Lavender"),
    PURPLE("Purple"),
    BURGUNDY("Burgundy"),
    CAMEL("Camel"),
    BROWN("Brown"),
    DARK_BROWN("Dark-Brown"),
    MAGENTA("Magenta"),
    GOLD("Gold"),
    SILVER("Silver"),
    COLORFUL("Colorful");

    private final String label;

    GarmentColor(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}