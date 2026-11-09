package com.capsuleai.wardrobe.domain;

/**
 * The approved properties that apply only to some categories (FR-GAR-007);
 * see {@link GarmentCategory#accepts(CategorySpecificProperty)}.
 */
public enum CategorySpecificProperty {
    FIT,
    NECKLINE,
    SLEEVE_LENGTH,
    LENGTH,
    SILHOUETTE
}