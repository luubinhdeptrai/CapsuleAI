package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * One explicitly confirmed edit of an owned garment, exactly as submitted (US-GAR-002).
 * {@code ownerAccountId} comes from the authenticated session, never from the request;
 * {@code garmentId} and {@code expectedRevision} are raw text so that malformed values are
 * reported, not thrown by a parser.
 */
public record ReviseGarmentCommand(
        UUID ownerAccountId,
        String garmentId,
        Boolean confirmed,
        String expectedRevision,
        GarmentChanges changes) {
}