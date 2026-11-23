package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * Explicitly confirm one reviewable item with its reviewed values (the 13 garment properties). The photo is
 * the one linked to the item, never one named by the request (OPEN-S4-29: no cross-item association).
 */
public record AcceptBulkImportItemCommand(UUID ownerAccountId, String batchId, String itemId,
                                          String expectedRevision, Boolean confirmed, GarmentValues values) {
}
