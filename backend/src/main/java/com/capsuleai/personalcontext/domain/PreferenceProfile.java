package com.capsuleai.personalcontext.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One account's accepted declaration (US-PROF-001). One per owner (S3-PROF-04). {@code revision} is
 * concurrency metadata (1 at the first declaration, one more per accepted change), not a preference.
 */
public record PreferenceProfile(
        UUID ownerAccountId,
        DeclaredPreferences preferences,
        long revision,
        Instant createdAt,
        Instant updatedAt) {

    public static final long FIRST_REVISION = 1;

    public PreferenceProfile {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(preferences, "preferences");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A preference revision starts at " + FIRST_REVISION);
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A profile cannot be updated before it was created");
        }
    }

    public static PreferenceProfile firstDeclaration(UUID ownerAccountId, DeclaredPreferences preferences,
                                                     Instant declaredAt) {
        return new PreferenceProfile(ownerAccountId, preferences, FIRST_REVISION, declaredAt, declaredAt);
    }

    /** The accepted change: one more revision; the update time never moves before the creation time. */
    public PreferenceProfile revisedTo(DeclaredPreferences revised, Instant revisedAt) {
        Instant updated = revisedAt.isBefore(createdAt) ? createdAt : revisedAt;
        return new PreferenceProfile(ownerAccountId, revised, Math.addExact(revision, 1), createdAt, updated);
    }
}