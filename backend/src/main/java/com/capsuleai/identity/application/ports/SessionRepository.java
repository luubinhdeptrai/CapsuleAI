package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Instant;
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

    /**
     * Revokes the session if it belongs to the account and is not yet revoked.
     * A single conditional update, so concurrent revocations cannot both succeed.
     *
     * @return {@code true} if this call revoked it; {@code false} if it was
     *         already revoked or is not that account's session
     */
    boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt);

    /**
     * Revokes every active (not revoked, not yet expired) session of the account in
     * one statement (FR-AUTH-004, NFR-SEC-002). Other accounts are untouched.
     * Call inside {@link IdentityTransactions} after the account lock.
     *
     * @return how many sessions this call revoked
     */
    int revokeAllActiveForAccount(UUID accountId, RevocationReason reason, Instant revokedAt);

    /**
     * Finds a refresh token by hash together with its session and LOCKS both rows
     * until the surrounding transaction ends, so competing renewals and logouts
     * of the same session run one after another.
     * Must be called inside {@link IdentityTransactions}.
     */
    Optional<LockedRefreshCredential> lockByRefreshToken(RefreshTokenHash hash);

    /** Marks a locked, current refresh token as consumed. */
    void consumeRefreshToken(UUID refreshTokenId, Instant consumedAt);

    /** Stores the session's new current refresh token. */
    void addRefreshToken(UUID sessionId, RefreshTokenHash hash, Instant issuedAt);
}