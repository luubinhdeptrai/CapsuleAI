package com.capsuleai.personalcontext.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 (S4-CTX-02) — one account's location record: the current selection (empty after clearing) and its
 * own revision, independent of preferences and optional context. A provider refresh never changes it.
 */
public record LocationProfile(UUID ownerAccountId, Optional<LocationSelection> selection, long revision,
                              Instant createdAt, Instant updatedAt) {

    public static final long FIRST_REVISION = 1;

    public LocationProfile {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(selection, "selection must be Optional.empty() when cleared");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A location revision starts at " + FIRST_REVISION);
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A location cannot be updated before it was created");
        }
    }

    public static LocationProfile firstSelection(UUID ownerAccountId, LocationSelection selection, Instant selectedAt) {
        return new LocationProfile(ownerAccountId, Optional.of(selection), FIRST_REVISION, selectedAt, selectedAt);
    }

    /** The accepted change or clear: one more revision; the update time never moves before the creation time. */
    public LocationProfile revisedTo(Optional<LocationSelection> revisedSelection, Instant revisedAt) {
        Instant updated = revisedAt.isBefore(createdAt) ? createdAt : revisedAt;
        return new LocationProfile(ownerAccountId, revisedSelection, Math.addExact(revision, 1), createdAt, updated);
    }
}