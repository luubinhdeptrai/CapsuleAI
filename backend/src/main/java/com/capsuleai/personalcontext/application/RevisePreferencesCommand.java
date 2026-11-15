package com.capsuleai.personalcontext.application;

import java.util.UUID;

/** One submitted preference change. The owner comes from the authenticated session, never from the request. */
public record RevisePreferencesCommand(UUID ownerAccountId, ExpectedRevision expectedRevision,
                                       PreferenceChanges changes) {
}