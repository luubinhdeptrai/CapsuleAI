package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * One submitted removal (US-GAR-005). The owner comes from the authenticated session, never from the
 * body. {@code garmentId} is the raw path text: a malformed id is "not found", like any id the owner
 * does not have. {@code expectedRevision} is the revision the user reviewed before confirming.
 */
public record RemoveGarmentCommand(UUID ownerAccountId, String garmentId, Boolean confirmed,
                                   String expectedRevision) {
}
