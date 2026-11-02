package com.capsuleai.identity.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Published Identity contract: the account and login session a protected
 * request was verified against.
 *
 * <p>Other modules obtain the current account only through this type
 * (as {@code @AuthenticationPrincipal AuthenticatedAccount account} in their
 * controllers), never through Identity's private domain or persistence classes.
 */
public record AuthenticatedAccount(UUID accountId, UUID sessionId) {

    public AuthenticatedAccount {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(sessionId, "sessionId");
    }
}