package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link SessionRepository}. */
public final class JdbcSessionRepository implements SessionRepository {

    private final JdbcClient jdbc;

    public JdbcSessionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
        jdbc.sql("""
                        INSERT INTO identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", session.id())
                .param("accountId", session.accountId())
                .param("establishedAt", JdbcTime.toDatabase(session.establishedAt()))
                .param("expiresAt", JdbcTime.toDatabase(session.expiresAt()))
                .update();
        insertRefreshToken(session.id(), firstRefreshToken, session.establishedAt());
    }

    @Override
    public Optional<LoginSession> findById(UUID sessionId) {
        return jdbc.sql("""
                        SELECT id, account_id, established_at, expires_at, revoked_at, revocation_reason
                        FROM identity_session
                        WHERE id = :id
                        """)
                .param("id", sessionId)
                .query(JdbcSessionRepository::mapSession)
                .optional();
    }

    @Override
    public boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt) {
        int updated = jdbc.sql("""
                        UPDATE identity_session
                        SET revoked_at = :revokedAt, revocation_reason = :reason
                        WHERE id = :id AND account_id = :accountId AND revoked_at IS NULL
                        """)
                .param("revokedAt", JdbcTime.toDatabase(revokedAt))
                .param("reason", reason.name())
                .param("id", sessionId)
                .param("accountId", accountId)
                .update();
        return updated == 1;
    }

    private void insertRefreshToken(UUID sessionId, RefreshTokenHash tokenHash, Instant issuedAt) {
        jdbc.sql("""
                        INSERT INTO identity_refresh_token (id, session_id, token_hash, issued_at)
                        VALUES (:id, :sessionId, :tokenHash, :issuedAt)
                        """)
                .param("id", UUID.randomUUID())
                .param("sessionId", sessionId)
                .param("tokenHash", tokenHash.value())
                .param("issuedAt", JdbcTime.toDatabase(issuedAt))
                .update();
    }

    private static LoginSession mapSession(ResultSet row, int rowNumber) throws SQLException {
        String reason = row.getString("revocation_reason");
        return new LoginSession(
                row.getObject("id", UUID.class),
                row.getObject("account_id", UUID.class),
                JdbcTime.fromDatabase(row, "established_at"),
                JdbcTime.fromDatabase(row, "expires_at"),
                JdbcTime.fromDatabase(row, "revoked_at"),
                reason == null ? null : RevocationReason.valueOf(reason));
    }
}