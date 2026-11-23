package com.capsuleai.wardrobe.application;

import java.util.UUID;

/** Link a staged photo to one item (or replace its photo). Path ids and body values arrive as text. */
public record LinkBulkImportPhotoCommand(UUID ownerAccountId, String batchId, String itemId,
                                         String expectedRevision, String photoUploadId) {
}
