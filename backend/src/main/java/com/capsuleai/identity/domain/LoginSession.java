package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One login session (the SRS "Refresh Session"), created by a successful login.
 * Its maximum lifetime is fixed when it is established and never extended.
 */
public record LoginSession(UUID id, UUID accountId, Instant establishedAt, Instant expiresAt) {

    public LoginSession {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(establishedAt, "establishedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(establishedAt)) {
            throw new IllegalArgumentException("A session must expire after it is established");
        }
    }

    public static LoginSession establish(UUID id, UUID accountId, Instant establishedAt) {
        return new LoginSession(id, accountId, establishedAt,
                establishedAt.plus(SessionPolicy.SESSION_MAXIMUM_LIFETIME));
    }

    /** Usable strictly before the absolute maximum lifetime ends. */
    public boolean isUsableAt(Instant now) {
        return now.isBefore(expiresAt);
    }

    public boolean belongsTo(UUID candidateAccountId) {
        return accountId.equals(candidateAccountId);
    }
}