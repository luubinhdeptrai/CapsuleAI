package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.api.DeclaredPersonalContext;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Implements the published {@link CurrentPersonalContext} from the owner's own persistence. Read-only. */
public final class CurrentPersonalContextService implements CurrentPersonalContext {

    private final PreferenceProfileRepository profiles;

    public CurrentPersonalContextService(PreferenceProfileRepository profiles) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
    }

    @Override
    public Optional<DeclaredPersonalContext> find(UUID accountId) {
        return profiles.find(Objects.requireNonNull(accountId, "accountId")).map(CurrentPersonalContextService::publish);
    }

    private static DeclaredPersonalContext publish(PreferenceProfile profile) {
        Set<String> styles = new LinkedHashSet<>();
        profile.preferences().styles().forEach(style -> styles.add(style.name()));
        Map<String, Integer> needs = new LinkedHashMap<>();
        profile.preferences().commonNeeds().forEach((need, priority) -> needs.put(need.name(), priority.weight()));
        return new DeclaredPersonalContext(Long.toString(profile.revision()), styles, needs);
    }
}