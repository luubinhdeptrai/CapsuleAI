package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.PurchasePrice;
import java.util.List;
import java.util.Optional;

/**
 * The 13 garment properties as submitted text, before validation: codes, Brand text and the
 * Price pair. Creation (US-GAR-001) and revision (US-GAR-002) both turn these values into an
 * accepted profile through the same validator, so there is one definition of a valid garment
 * (S3-GAR-05). A missing value is null; a missing Material or Style list is null or empty.
 */
public record GarmentValues(
        String category,
        String subtype,
        String color,
        List<String> materials,
        String pattern,
        List<String> styles,
        String fit,
        String neckline,
        String sleeveLength,
        String length,
        String silhouette,
        String brand,
        String priceAmount,
        String priceCurrency) {

    /**
     * An accepted profile expressed as submitted values. Validating the result again yields an
     * equal profile: codes are enum names, Brand is the exact accepted text and the amount is
     * the exact accepted decimal text.
     */
    static GarmentValues of(GarmentProfile profile) {
        return new GarmentValues(
                profile.category().name(),
                code(profile.subtype()),
                profile.color().name(),
                profile.materials().stream().map(Enum::name).toList(),
                code(profile.pattern()),
                profile.styles().stream().map(Enum::name).toList(),
                code(profile.fit()),
                code(profile.neckline()),
                code(profile.sleeveLength()),
                code(profile.length()),
                code(profile.silhouette()),
                profile.brand().map(Brand::value).orElse(null),
                profile.price().map(PurchasePrice::amountText).orElse(null),
                profile.price().map(PurchasePrice::currencyCode).orElse(null));
    }

    private static String code(Optional<? extends ControlledValue> value) {
        return value.map(ControlledValue::name).orElse(null);
    }
}