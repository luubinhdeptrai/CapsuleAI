package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake with the same guarded creation and conditional revision as the SQL. */
class InMemoryPreferenceProfileRepository implements PreferenceProfileRepository {

    private final Map<UUID, PreferenceProfile> profiles = new HashMap<>();
    private int writes;

    @Override
    public synchronized Optional<PreferenceProfile> find(UUID ownerAccountId) {
        return Optional.ofNullable(profiles.get(ownerAccountId));
    }

    @Override
    public synchronized boolean createIfAbsent(PreferenceProfile profile) {
        if (profiles.putIfAbsent(profile.ownerAccountId(), profile) != null) {
            return false;
        }
        writes++;
        return true;
    }

    @Override
    public synchronized boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision,
                                                  DeclaredPreferences preferences, Instant updatedAt) {
        PreferenceProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        profiles.put(ownerAccountId, current.revisedTo(preferences, updatedAt));
        writes++;
        return true;
    }

    synchronized int writes() {
        return writes;
    }
}
