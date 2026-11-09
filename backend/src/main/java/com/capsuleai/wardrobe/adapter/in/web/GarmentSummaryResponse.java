package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import java.util.UUID;

/**
 * One garment in the wardrobe overview: enough to recognize and select it
 * (US-WAR-001 AC-01/AC-02). Category and Color are always present, so a
 * minimum-only, imageless garment is still identifiable; the client can draw a
 * category/color placeholder. Subtype and Brand are null when not supplied.
 * Detail (US-WAR-002) returns every accepted property.
 */
public record GarmentSummaryResponse(
        UUID id,
        String category,
        String subtype,
        String color,
        String brand,
        String recommendationEligibility,
        String createdAt) {

    static GarmentSummaryResponse from(Garment garment) {
        return new GarmentSummaryResponse(
                garment.id(),
                garment.profile().category().name(),
                garment.profile().subtype().map(GarmentSubtype::name).orElse(null),
                garment.profile().color().name(),
                garment.profile().brand().map(Brand::value).orElse(null),
                GarmentResponse.ELIGIBILITY_NOT_EVALUATED,
                garment.createdAt().toString());
    }
}