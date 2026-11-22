package com.capsuleai.personalcontext.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * US-PROF-002 — one account's optional context record (S4-PROF-02): its current values and its own revision,
 * independent of the preferences revision. {@code revision} is concurrency metadata, not a preference. A
 * record whose values were all cleared still exists, so the revision line continues.
 */
public record OptionalContextProfile(
        UUID ownerAccountId,
        OptionalContext values,
        long revision,
        Instant createdAt,
        Instant updatedAt) {

    public static final long FIRST_REVISION = 1;

    public OptionalContextProfile {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("An optional-context revision starts at " + FIRST_REVISION);
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Optional context cannot be updated before it was created");
        }
    }

    public static OptionalContextProfile firstDeclaration(UUID ownerAccountId, OptionalContext values,
                                                          Instant declaredAt) {
        return new OptionalContextProfile(ownerAccountId, values, FIRST_REVISION, declaredAt, declaredAt);
    }

    /** The accepted change: one more revision; the update time never moves before the creation time. */
    public OptionalContextProfile revisedTo(OptionalContext revisedValues, Instant revisedAt) {
        Instant updated = revisedAt.isBefore(createdAt) ? createdAt : revisedAt;
        return new OptionalContextProfile(ownerAccountId, revisedValues, Math.addExact(revision, 1), createdAt,
                updated);
    }
}