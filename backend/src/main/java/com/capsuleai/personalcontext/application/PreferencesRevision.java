package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of an accepted change: the declaration as it now is (empty if the owner still has declared
 * nothing) and whether this change wrote anything (OPEN-S3-06).
 */
public record PreferencesRevision(Optional<PreferenceProfile> profile, boolean changed) {

    public PreferencesRevision {
        Objects.requireNonNull(profile, "profile");
    }
}