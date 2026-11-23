package com.capsuleai.personalcontext.application;

import java.util.UUID;

/**
 * One submitted location change: keep (no selection member), clear (null), replace (an object) or malformed.
 * The owner comes from the authenticated session.
 */
public record ReviseLocationCommand(UUID ownerAccountId, ExpectedRevision expectedRevision,
                                    ValueChange<SubmittedLocation> selection) {
}
