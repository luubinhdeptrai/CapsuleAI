package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.LocationProfile;
import java.util.Objects;
import java.util.Optional;

/**
 * US-CTX-001 AC-02 — the accepted selection (empty profile: never selected) and, separately, the environmental
 * context of that selection. A selected city alone is never presented as known weather.
 */
public record LocationContextView(Optional<LocationProfile> profile, EnvironmentalContext environment) {

    public LocationContextView {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(environment, "environment");
    }
}