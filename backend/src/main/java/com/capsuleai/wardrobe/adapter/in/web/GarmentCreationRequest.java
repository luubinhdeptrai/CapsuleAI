package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.AddGarmentCommand;
import java.util.List;
import java.util.UUID;

/**
 * JSON body of POST /api/v1/wardrobe/garments: the 13 approved properties plus
 * the explicit confirmation. Controlled values are codes; Materials and Styles are
 * arrays of codes; missing fields arrive as null. There is no owner field: the owner is the authenticated account.
 */
public record GarmentCreationRequest(
        Boolean confirmed,
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
        PriceRequest price) {

    AddGarmentCommand toCommand(UUID ownerAccountId, String idempotencyKey) {
        return new AddGarmentCommand(ownerAccountId, idempotencyKey, confirmed, category, subtype, color,
                materials, pattern, styles, fit, neckline, sleeveLength, length, silhouette, brand,
                price == null ? null : price.amount(),
                price == null ? null : price.currency());
    }
}