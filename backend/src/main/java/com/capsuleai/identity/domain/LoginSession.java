package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One login session (the SRS "Refresh Session"), created by a successful login.
 * Its maximum lifetime is fixed when it is established and never extended.
 * A revoked session never becomes usable again.
 *
 * <p>{@code revokedAt} and {@code revocationReason} are both null (not revoked)
 * or both set.
 */
public record LoginSession(
        UUID id,
        UUID accountId,
        Instant establishedAt,
        Instant expiresAt,
        Instant revokedAt,
        RevocationReason revocationReason) {

    public LoginSession {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(establishedAt, "establishedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(establishedAt)) {
            throw new IllegalArgumentException("A session must expire after it is established");
        }
        if ((revokedAt == null) != (revocationReason == null)) {
            throw new IllegalArgumentException("revokedAt and revocationReason must be set together");
        }
    }

    public static LoginSession establish(UUID id, UUID accountId, Instant establishedAt) {
        return new LoginSession(id, accountId, establishedAt,
                establishedAt.plus(SessionPolicy.SESSION_MAXIMUM_LIFETIME), null, null);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** Usable = not revoked and strictly before the absolute maximum lifetime ends. */
    public boolean isUsableAt(Instant now) {
        return !isRevoked() && now.isBefore(expiresAt);
    }

    public boolean belongsTo(UUID candidateAccountId) {
        return accountId.equals(candidateAccountId);
    }
}