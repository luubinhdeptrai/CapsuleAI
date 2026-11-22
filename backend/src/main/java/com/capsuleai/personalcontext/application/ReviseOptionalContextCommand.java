package com.capsuleai.personalcontext.application;

import java.util.UUID;

/** One submitted optional-context change. The owner comes from the authenticated session, never the request. */
public record ReviseOptionalContextCommand(UUID ownerAccountId, ExpectedRevision expectedRevision,
                                           OptionalContextChanges changes) {
}