package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A registered private account (DATA-AUTH-001, DATA-AUTH-002).
 *
 * <p>{@code displayName} is {@link Optional#empty()} when the user omitted it.
 */
public record Account(
        UUID id,
        EmailAddress email,
        PasswordHash passwordHash,
        Optional<DisplayName> displayName,
        Instant createdAt) {

    public Account {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(displayName, "displayName must be Optional.empty() when absent");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}