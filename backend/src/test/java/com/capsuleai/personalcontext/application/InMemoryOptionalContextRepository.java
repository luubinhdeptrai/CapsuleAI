package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.OptionalContextRepository;
import com.capsuleai.personalcontext.domain.OptionalContext;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link OptionalContextRepository} with the same guarded-write semantics. */
final class InMemoryOptionalContextRepository implements OptionalContextRepository {

    private final Map<UUID, OptionalContextProfile> profiles = new HashMap<>();
    private int writes;

    @Override
    public synchronized Optional<OptionalContextProfile> find(UUID ownerAccountId) {
        return Optional.ofNullable(profiles.get(ownerAccountId));
    }

    @Override
    public synchronized boolean createIfAbsent(OptionalContextProfile profile) {
        if (profiles.putIfAbsent(profile.ownerAccountId(), profile) != null) {
            return false;
        }
        writes++;
        return true;
    }

    @Override
    public synchronized boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, OptionalContext values,
                                                  Instant updatedAt) {
        OptionalContextProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        profiles.put(ownerAccountId, current.revisedTo(values, updatedAt));
        writes++;
        return true;
    }

    synchronized int writes() {
        return writes;
    }
}
