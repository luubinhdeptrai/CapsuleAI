package com.capsuleai.identity.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Published Identity contract: "may this account use this login session now?"
 * (FR-AUTH-009, ADA-008).
 *
 * <p>Token signature and expiry are checked before this is asked; this contract
 * adds the persistent session authority that a token alone cannot prove.
 */
public interface SessionAuthority {

    /**
     * @return the authenticated account if the session exists, belongs to the
     *         account and is currently usable; otherwise empty
     */
    Optional<AuthenticatedAccount> findUsableSession(UUID accountId, UUID sessionId);
}