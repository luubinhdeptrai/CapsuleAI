package com.capsuleai.identity.application.ports;

import java.time.Instant;
import java.util.UUID;

/** Creates signed access tokens. */
public interface AccessTokenIssuer {

    /** @return a signed token naming the account and session, valid until {@code expiresAt} */
    String issue(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt);
}