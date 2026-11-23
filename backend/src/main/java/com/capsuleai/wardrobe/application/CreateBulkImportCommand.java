package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * Start one import batch. {@code itemCount} is null when it was not sent; {@code itemCountWellFormed} is false
 * when a value of the wrong JSON kind was sent (text, a fraction or a number beyond the integer range), which
 * is reported without echoing it.
 */
public record CreateBulkImportCommand(UUID ownerAccountId, String idempotencyKey, Integer itemCount,
                                      boolean itemCountWellFormed) {
}
