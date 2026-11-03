package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshToken;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link SessionRepository} for application tests (no real locking). */
final class InMemorySessionRepository implements SessionRepository {

    private final Map<UUID, LoginSession> sessionsById = new LinkedHashMap<>();
    private final Map<RefreshTokenHash, RefreshToken> tokensByHash = new LinkedHashMap<>();

    @Override
    public synchronized void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
        sessionsById.put(session.id(), session);
        tokensByHash.put(firstRefreshToken,
                new RefreshToken(UUID.randomUUID(), session.id(), session.establishedAt(), null));
    }

    @Override
    public synchronized Optional<LoginSession> findById(UUID sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    @Override
    public synchronized boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt) {
        LoginSession session = sessionsById.get(sessionId);
        if (session == null || !session.belongsTo(accountId) || session.isRevoked()) {
            return false;
        }
        sessionsById.put(sessionId, new LoginSession(session.id(), session.accountId(),
                session.establishedAt(), session.expiresAt(), revokedAt, reason));
        return true;
    }

    @Override
    public synchronized Optional<LockedRefreshCredential> lockByRefreshToken(RefreshTokenHash hash) {
        RefreshToken token = tokensByHash.get(hash);
        if (token == null) {
            return Optional.empty();
        }
        return Optional.of(new LockedRefreshCredential(sessionsById.get(token.sessionId()), token));
    }

    @Override
    public synchronized void consumeRefreshToken(UUID refreshTokenId, Instant consumedAt) {
        for (Map.Entry<RefreshTokenHash, RefreshToken> entry : tokensByHash.entrySet()) {
            RefreshToken token = entry.getValue();
            if (token.id().equals(refreshTokenId)) {
                if (token.isConsumed()) {
                    throw new IllegalStateException("Refresh token already consumed");
                }
                entry.setValue(new RefreshToken(token.id(), token.sessionId(), token.issuedAt(), consumedAt));
                return;
            }
        }
        throw new IllegalStateException("Unknown refresh token");
    }

    @Override
    public synchronized void addRefreshToken(UUID sessionId, RefreshTokenHash hash, Instant issuedAt) {
        tokensByHash.put(hash, new RefreshToken(UUID.randomUUID(), sessionId, issuedAt, null));
    }

    synchronized List<LoginSession> all() {
        return List.copyOf(sessionsById.values());
    }

    synchronized Optional<UUID> sessionIdForRefreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(tokensByHash.get(hash)).map(RefreshToken::sessionId);
    }

    synchronized Optional<RefreshToken> refreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(tokensByHash.get(hash));
    }
}