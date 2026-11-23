package com.capsuleai.wardrobe.adapter.in.web;

/** JSON body of POST …/bulk-imports/{batchId}/stop: the explicit confirmation to stop the remaining work. */
public record BulkImportStopRequest(Boolean confirmed) {
}
