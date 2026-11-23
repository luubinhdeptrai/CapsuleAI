package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.AcceptBulkImportItemCommand;
import com.capsuleai.wardrobe.application.GarmentValues;
import java.util.List;
import java.util.UUID;

/**
 * JSON body of POST …/bulk-imports/{batchId}/items/{itemId}/acceptance (US-BULK-001 AC-03/AC-04): the explicit
 * confirmation, the item revision the user reviewed and the reviewed 13 garment properties, in the same shape
 * as a single garment creation. There is no photo field: the item's linked photo is used.
 */
public record BulkImportItemAcceptanceRequest(
        String expectedRevision,
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

    AcceptBulkImportItemCommand toCommand(UUID ownerAccountId, String batchId, String itemId) {
        GarmentValues values = new GarmentValues(category, subtype, color, materials, pattern, styles, fit,
                neckline, sleeveLength, length, silhouette, brand,
                price == null ? null : price.amount(),
                price == null ? null : price.currency());
        return new AcceptBulkImportItemCommand(ownerAccountId, batchId, itemId, expectedRevision, confirmed, values);
    }
}
