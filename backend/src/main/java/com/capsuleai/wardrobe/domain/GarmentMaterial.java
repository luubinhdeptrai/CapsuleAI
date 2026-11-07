package com.capsuleai.wardrobe.domain;

/**
 * Garment Material: Zero, one or several distinct values per garment, without percentages (FR-GAR-010).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentMaterial implements ControlledValue {

    COTTON("Cotton"),
    POLYESTER("Polyester"),
    NYLON("Nylon"),
    DENIM("Denim"),
    LEATHER("Leather"),
    WOOL("Wool"),
    SPANDEX_ELASTANE("Spandex (elastane)"),
    LINEN("Linen"),
    ACRYLIC("Acrylic"),
    SILK("Silk"),
    RAYON("Rayon"),
    VISCOSE("Viscose"),
    TWEED("Tweed"),
    VELVET("Velvet"),
    CHIFFON("Chiffon"),
    SUEDE("Suede"),
    CORDUROY("Corduroy"),
    LACE("Lace"),
    FUR("Fur"),
    DOWN("Down"),
    LYOCELL("Lyocell"),
    METAL("Metal"),
    CUPRO("Cupro"),
    CASHMERE("Cashmere"),
    MOHAIR("Mohair"),
    ANGORA("Angora"),
    ALPACA("Alpaca"),
    OTHER_MATERIALS("Other Materials");

    private final String label;

    GarmentMaterial(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}