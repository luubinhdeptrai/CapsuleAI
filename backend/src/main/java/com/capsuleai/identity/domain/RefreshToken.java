package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One issued refresh token of a login session. Only its hash is ever stored. */
public record RefreshToken(UUID id, UUID sessionId, Instant issuedAt, Instant consumedAt) {

    public RefreshToken {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(issuedAt, "issuedAt");
    }

    /** Consumed by an earlier successful rotation; presenting it again is reuse. */
    public boolean isConsumed() {
        return consumedAt != null;
    }
}