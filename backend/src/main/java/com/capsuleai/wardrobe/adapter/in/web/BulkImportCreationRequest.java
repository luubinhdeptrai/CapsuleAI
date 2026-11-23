package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.CreateBulkImportCommand;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of POST /api/v1/wardrobe/bulk-imports (US-BULK-001): how many photos the user selected. The raw
 * {@link JsonNode} keeps a missing value, a whole number and a value of the wrong kind apart, so text or a
 * fraction is reported instead of being coerced or truncated. The Idempotency-Key header names the start.
 */
public record BulkImportCreationRequest(JsonNode itemCount) {

    CreateBulkImportCommand toCommand(UUID ownerAccountId, String idempotencyKey) {
        if (itemCount == null || itemCount.isNull()) {
            return new CreateBulkImportCommand(ownerAccountId, idempotencyKey, null, true);
        }
        if (itemCount.isIntegralNumber() && itemCount.canConvertToInt()) {
            return new CreateBulkImportCommand(ownerAccountId, idempotencyKey, itemCount.intValue(), true);
        }
        return new CreateBulkImportCommand(ownerAccountId, idempotencyKey, null, false);
    }
}
