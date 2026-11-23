package com.capsuleai.wardrobe.adapter.in.web;

/** JSON body of POST …/bulk-imports/{batchId}/items/{itemId}/cancellation: the item revision the user reviewed. */
public record BulkImportItemCancellationRequest(String expectedRevision) {
}
