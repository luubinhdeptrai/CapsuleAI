package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link SessionRepository} for application tests. */
final class InMemorySessionRepository implements SessionRepository {

    private final Map<UUID, LoginSession> sessionsById = new LinkedHashMap<>();
    private final Map<RefreshTokenHash, UUID> sessionIdsByRefreshToken = new LinkedHashMap<>();

    @Override
    public synchronized void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
        sessionsById.put(session.id(), session);
        sessionIdsByRefreshToken.put(firstRefreshToken, session.id());
    }

    @Override
    public synchronized Optional<LoginSession> findById(UUID sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    synchronized List<LoginSession> all() {
        return List.copyOf(sessionsById.values());
    }

    synchronized Optional<UUID> sessionIdForRefreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(sessionIdsByRefreshToken.get(hash));
    }
}
