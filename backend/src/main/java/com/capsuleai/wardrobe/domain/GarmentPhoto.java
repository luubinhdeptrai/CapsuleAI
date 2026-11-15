package com.capsuleai.wardrobe.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * The private photo attached to a garment at confirmation (US-GAR-003, DATA-GAR-004): media
 * metadata, not one of the 13 garment properties. The bytes live in private object storage
 * and are served only after a current owner check (S3-IMG-05).
 */
public record GarmentPhoto(UUID photoUploadId, String contentType, int width, int height) {

    public GarmentPhoto {
        Objects.requireNonNull(photoUploadId, "photoUploadId");
        Objects.requireNonNull(contentType, "contentType");
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("A stored photo has positive dimensions");
        }
    }
}