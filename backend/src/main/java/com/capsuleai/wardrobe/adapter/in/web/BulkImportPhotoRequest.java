package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.LinkBulkImportPhotoCommand;
import java.util.UUID;

/** JSON body of POST …/bulk-imports/{batchId}/items/{itemId}/photo: the staged photo to link to the item. */
public record BulkImportPhotoRequest(String expectedRevision, String photoUploadId) {

    LinkBulkImportPhotoCommand toCommand(UUID ownerAccountId, String batchId, String itemId) {
        return new LinkBulkImportPhotoCommand(ownerAccountId, batchId, itemId, expectedRevision, photoUploadId);
    }
}
