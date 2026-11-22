package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * One submitted attachment of a staged photo to an existing garment (provisional US-GAR-006). The owner
 * comes from the authenticated session. Ids are raw request text: a malformed garment id is "not found";
 * a malformed photo id is a validation error, because the client supplied it in the body.
 */
public record AttachGarmentPhotoCommand(UUID ownerAccountId, String garmentId, Boolean confirmed,
                                        String expectedRevision, String photoUploadId) {
}
