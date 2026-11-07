package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One issued password-reset interaction (DATA-AUTH-005, BRULE-AUTH-004).
 * Only the hash of its secret is ever stored.
 *
 * <p>An interaction ends in at most one way: consumed by an accepted reset, or
 * replaced by a newer issuance. An expired interaction simply stops being usable.
 */
public record PasswordResetToken(
        UUID id,
        UUID accountId,
        Instant issuedAt,
        Instant expiresAt,
        Instant consumedAt,
        Instant replacedAt) {

    public PasswordResetToken {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("A reset interaction must expire after it is issued");
        }
        if (consumedAt != null && replacedAt != null) {
            throw new IllegalArgumentException("A reset interaction cannot be both consumed and replaced");
        }
    }

    public static PasswordResetToken issue(UUID id, UUID accountId, Instant issuedAt) {
        return new PasswordResetToken(id, accountId, issuedAt, PasswordResetPolicy.expiresAt(issuedAt), null, null);
    }

    /** Usable = not consumed, not replaced, and strictly before its 30-minute expiry. */
    public boolean isUsableAt(Instant now) {
        return consumedAt == null && replacedAt == null && now.isBefore(expiresAt);
    }
}