package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * One owned garment with every accepted property. Every field is always present:
 * an optional property that was not supplied, or does not apply to the category
 * (Fit, Neckline, Sleeve Length outside Tops; Length outside Tops and Pants;
 * Silhouette outside Pants), is {@code null}, and no Material or Style is {@code []}.
 * Nothing is filled in to make the profile look complete (DATA-GAR-002, AC-08).
 *
 * <p>{@code recommendationEligibility} is always {@code NOT_EVALUATED}: no
 * eligibility policy is approved, so none is stored or computed (FR-GAR-004).
 * {@code recordedUse} says whether reported-use evidence exists; no Wear
 * capability exists yet, so it is UNAVAILABLE, never zero (US-WAR-002 AC-07).
 *
 * <p>{@code revision} is the opaque version of the accepted profile (US-GAR-002, OPEN-S3-04);
 * an editor sends it back unchanged as {@code expectedRevision}.
 */
public record GarmentResponse(
        UUID id,
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
        PriceResponse price,
        String provenance,
        String recommendationEligibility,
        RecordedUseResponse recordedUse,
        String createdAt,
        String revision) {

    static final String ELIGIBILITY_NOT_EVALUATED = "NOT_EVALUATED";

    static GarmentResponse from(Garment garment) {
        GarmentProfile profile = garment.profile();
        return new GarmentResponse(
                garment.id(),
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
                profile.price().map(PriceResponse::from).orElse(null),
                garment.provenance().name(),
                ELIGIBILITY_NOT_EVALUATED,
                RecordedUseResponse.UNAVAILABLE,
                garment.createdAt().toString(),
                Long.toString(garment.revision()));
    }

    private static String code(Optional<? extends ControlledValue> value) {
        return value.map(ControlledValue::name).orElse(null);
    }
}