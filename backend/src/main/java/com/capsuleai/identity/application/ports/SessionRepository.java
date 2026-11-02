package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import java.util.Optional;
import java.util.UUID;

/** Identity-owned persistence of login sessions and their refresh tokens. */
public interface SessionRepository {

    /**
     * Stores a new session together with the hash of its first refresh token.
     * Call inside {@link IdentityTransactions} so both rows commit together.
     */
    void create(LoginSession session, RefreshTokenHash firstRefreshToken);

    Optional<LoginSession> findById(UUID sessionId);
}