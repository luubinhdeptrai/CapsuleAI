package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The requested change of each of the 13 garment properties in one edit (US-GAR-002,
 * S3-GAR-01). Category and Color can be replaced but not cleared; Price is one value made of
 * amount and currency and is kept, cleared or replaced as a whole (BRULE-GAR-010).
 */
public record GarmentChanges(
        FieldChange<String> category,
        FieldChange<String> subtype,
        FieldChange<String> color,
        FieldChange<List<String>> materials,
        FieldChange<String> pattern,
        FieldChange<List<String>> styles,
        FieldChange<String> fit,
        FieldChange<String> neckline,
        FieldChange<String> sleeveLength,
        FieldChange<String> length,
        FieldChange<String> silhouette,
        FieldChange<String> brand,
        FieldChange<PriceInput> price) {

    /** A submitted Price: both parts as text, either possibly missing (then reported as incomplete). */
    public record PriceInput(String amount, String currency) {
    }

    public GarmentChanges {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(subtype, "subtype");
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(materials, "materials");
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(styles, "styles");
        Objects.requireNonNull(fit, "fit");
        Objects.requireNonNull(neckline, "neckline");
        Objects.requireNonNull(sleeveLength, "sleeveLength");
        Objects.requireNonNull(length, "length");
        Objects.requireNonNull(silhouette, "silhouette");
        Objects.requireNonNull(brand, "brand");
        Objects.requireNonNull(price, "price");
    }

    /** An edit that keeps every property. */
    public static GarmentChanges none() {
        return new GarmentChanges(FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep());
    }

    /**
     * Problems visible without the accepted profile: a cleared required property, or a value of
     * the wrong kind. Reported before any lookup, so nothing is read for an unusable request.
     */
    List<GarmentFieldViolation> shapeViolations() {
        List<GarmentFieldViolation> violations = new ArrayList<>();
        required("category", category, violations);
        malformed("subtype", subtype, violations);
        required("color", color, violations);
        malformed("materials", materials, violations);
        malformed("pattern", pattern, violations);
        malformed("styles", styles, violations);
        malformed("fit", fit, violations);
        malformed("neckline", neckline, violations);
        malformed("sleeveLength", sleeveLength, violations);
        malformed("length", length, violations);
        malformed("silhouette", silhouette, violations);
        malformed("brand", brand, violations);
        malformed("price", price, violations);
        return violations;
    }

    /** The garment values that result from applying this edit to {@code current}. */
    GarmentValues applyTo(GarmentValues current) {
        PriceInput currentPrice = current.priceAmount() == null && current.priceCurrency() == null
                ? null
                : new PriceInput(current.priceAmount(), current.priceCurrency());
        PriceInput resultingPrice = price.applyTo(currentPrice);
        return new GarmentValues(
                category.applyTo(current.category()),
                subtype.applyTo(current.subtype()),
                color.applyTo(current.color()),
                materials.applyTo(current.materials()),
                pattern.applyTo(current.pattern()),
                styles.applyTo(current.styles()),
                fit.applyTo(current.fit()),
                neckline.applyTo(current.neckline()),
                sleeveLength.applyTo(current.sleeveLength()),
                length.applyTo(current.length()),
                silhouette.applyTo(current.silhouette()),
                brand.applyTo(current.brand()),
                resultingPrice == null ? null : resultingPrice.amount(),
                resultingPrice == null ? null : resultingPrice.currency());
    }

    private static void required(String field, FieldChange<String> change, List<GarmentFieldViolation> violations) {
        if (change.isClear()) {
            violations.add(new GarmentFieldViolation(field, GarmentValueProblem.REQUIRED));
        } else {
            malformed(field, change, violations);
        }
    }

    private static void malformed(String field, FieldChange<?> change, List<GarmentFieldViolation> violations) {
        if (change.isMalformed()) {
            violations.add(new GarmentFieldViolation(field, GarmentValueProblem.UNSUPPORTED_VALUE));
        }
    }
}