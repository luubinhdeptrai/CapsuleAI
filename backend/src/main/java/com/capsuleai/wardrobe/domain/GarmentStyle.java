package com.capsuleai.wardrobe.domain;

/**
 * Garment Style: zero, one or several distinct named Styles for every category; Etc is
 * explanatory, never a value (FR-GAR-009, Product Owner decision 2026-11-07).
 * Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3).
 */
public enum GarmentStyle implements ControlledValue {

    COMFORTABLE("Comfortable"),
    CASUAL("Casual"),
    BUSINESS_CASUAL("Business Casual"),
    FORMAL("Formal"),
    MODERN("Modern"),
    CLASSIC("Classic"),
    MINIMALIST("Minimalist"),
    BOHEMIAN("Bohemian"),
    LUXURY("Luxury"),
    SPORTY("Sporty"),
    ATHLEISURE("Athleisure"),
    TRENDY("Trendy"),
    KIDCORE("Kidcore"),
    ARTISTIC("Artistic"),
    DRESSY("Dressy"),
    FEMININE("Feminine"),
    STREET("Street");

    private final String label;

    GarmentStyle(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }
}