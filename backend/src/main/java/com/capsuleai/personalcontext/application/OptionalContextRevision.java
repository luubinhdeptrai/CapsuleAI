package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of an accepted optional-context change: the record as it now is (empty if the owner still has
 * never declared anything) and whether this change wrote anything (a no-op keeps the revision).
 */
public record OptionalContextRevision(Optional<OptionalContextProfile> profile, boolean changed) {

    public OptionalContextRevision {
        Objects.requireNonNull(profile, "profile");
    }
}