package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * One photo staging request as submitted (US-GAR-003). The owner comes from the authenticated
 * session; {@code uploadKey} is the raw Idempotency-Key header; {@code photo} is null when the
 * request carried no photo part.
 */
public record StagePhotoCommand(UUID ownerAccountId, String uploadKey, ReceivedPhoto photo) {
}