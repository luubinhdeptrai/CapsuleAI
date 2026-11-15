package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** US-PROF-001 AC-01 — the owner's current or unset declaration. Read-only. */
public final class ViewPreferencesService {

    private final PreferenceProfileRepository profiles;

    public ViewPreferencesService(PreferenceProfileRepository profiles) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
    }

    public Optional<PreferenceProfile> view(UUID ownerAccountId) {
        return profiles.find(Objects.requireNonNull(ownerAccountId, "ownerAccountId"));
    }
}