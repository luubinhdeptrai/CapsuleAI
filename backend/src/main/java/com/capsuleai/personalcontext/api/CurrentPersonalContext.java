package com.capsuleai.personalcontext.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Published Personal Context query (S3-PROF-05, OPEN-S3-16): the account's current declared styles and
 * common-need priorities, read at request time from the owner, never from a copy. Future advice and
 * Coverage modules use this instead of Personal Context tables (AR-03). No consumer exists in Sprint 3.
 */
public interface CurrentPersonalContext {

    /** @return the current declaration, or empty if the account has declared nothing (absent context, not a persona) */
    Optional<DeclaredPersonalContext> find(UUID accountId);
}