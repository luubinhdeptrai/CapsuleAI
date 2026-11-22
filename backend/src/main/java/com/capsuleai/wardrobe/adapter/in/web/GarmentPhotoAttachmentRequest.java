package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.AttachGarmentPhotoCommand;
import java.util.UUID;

/**
 * JSON body of POST /api/v1/wardrobe/garments/{garmentId}/photo-attachment (provisional US-GAR-006): the
 * explicit confirmation, the garment revision the user reviewed and the reviewed staged photo. There is no
 * owner field.
 */
public record GarmentPhotoAttachmentRequest(Boolean confirmed, String expectedRevision, String photoUploadId) {

    AttachGarmentPhotoCommand toCommand(UUID ownerAccountId, String garmentId) {
        return new AttachGarmentPhotoCommand(ownerAccountId, garmentId, confirmed, expectedRevision, photoUploadId);
    }
}