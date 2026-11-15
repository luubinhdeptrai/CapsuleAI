package com.capsuleai.wardrobe.application;

import java.util.List;
import java.util.UUID;

/**
 * One manual garment addition exactly as submitted. Missing values arrive as null.
 * {@code ownerAccountId} comes from the authenticated session, never from the body;
 * {@code idempotencyKey} identifies the user's logical addition across retries;
 * {@code photoUploadId} optionally names the reviewed staged photo to attach (US-GAR-003).
 */
public record AddGarmentCommand(
        UUID ownerAccountId,
        String idempotencyKey,
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
        String priceAmount,
        String priceCurrency,
        String photoUploadId) {

    /** An imageless manual addition (US-GAR-001). */
    public AddGarmentCommand(UUID ownerAccountId, String idempotencyKey, Boolean confirmed, String category,
                             String subtype, String color, List<String> materials, String pattern,
                             List<String> styles, String fit, String neckline, String sleeveLength, String length,
                             String silhouette, String brand, String priceAmount, String priceCurrency) {
        this(ownerAccountId, idempotencyKey, confirmed, category, subtype, color, materials, pattern, styles, fit,
                neckline, sleeveLength, length, silhouette, brand, priceAmount, priceCurrency, null);
    }

    /** The submitted garment properties, for the shared profile validation (S3-GAR-05). */
    GarmentValues values() {
        return new GarmentValues(category, subtype, color, materials, pattern, styles, fit, neckline,
                sleeveLength, length, silhouette, brand, priceAmount, priceCurrency);
    }
}