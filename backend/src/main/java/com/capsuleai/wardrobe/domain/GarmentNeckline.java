package com.capsuleai.wardrobe.domain;

/**
 * Garment Neckline: Optional; applies only to Tops (FR-GAR-007).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentNeckline implements ControlledValue {

    ROUND("Round"),
    SCOOP_U("Scoop (U)"),
    BOAT("Boat"),
    V_NECK("V-Neck"),
    DEEP_V("Deep-V"),
    SQUARE("Square"),
    SURPLICE("Surplice"),
    SHIRT_COLLAR("Shirt Collar"),
    STANDUP_COLLAR("Standup-Collar"),
    WIDE_COLLAR("Wide-Collar"),
    MOCKNECK("Mockneck"),
    TURTLENECK("Turtleneck"),
    STRAPLESS("Strapless"),
    THICK_STRAP("Thick-Strap"),
    THIN_STRAP("Thin-Strap"),
    SWEETHEART("Sweetheart"),
    OFF_THE_SHOULDER("Off-The-Shoulder"),
    ASYMMETRIC("Asymmetric"),
    HALTER("Halter"),
    ILLUSION("Illusion"),
    KEYHOLE("Keyhole"),
    SUIT_COLLAR("Suit-Collar"),
    OTHER("Other");

    private final String label;

    GarmentNeckline(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}