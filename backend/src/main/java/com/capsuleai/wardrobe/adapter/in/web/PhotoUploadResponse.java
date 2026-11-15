package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.UUID;

/**
 * A staged photo (US-GAR-003). Every field is always present: contentType, width and height are null
 * until the photo is READY; garmentId is set only when ATTACHED. A staged photo is never a garment.
 */
public record PhotoUploadResponse(
        UUID id,
        String status,
        String contentType,
        Integer width,
        Integer height,
        String expiresAt,
        UUID garmentId) {

    static PhotoUploadResponse from(PhotoUpload upload) {
        return new PhotoUploadResponse(upload.id(), upload.status().name(), upload.contentType(), upload.width(),
                upload.height(), upload.expiresAt().toString(), upload.garmentId());
    }
}