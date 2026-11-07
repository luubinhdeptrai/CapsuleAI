package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link PasswordResetRepository}. */
public final class JdbcPasswordResetRepository implements PasswordResetRepository {

    private final JdbcClient jdbc;

    public JdbcPasswordResetRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int replaceOpenTokens(UUID accountId, Instant replacedAt) {
        return jdbc.sql("""
                        UPDATE identity.identity_password_reset
                        SET replaced_at = :replacedAt
                        WHERE account_id = :accountId AND consumed_at IS NULL AND replaced_at IS NULL
                        """)
                .param("replacedAt", JdbcTime.toDatabase(replacedAt))
                .param("accountId", accountId)
                .update();
    }

    @Override
    public void add(PasswordResetToken token, PasswordResetTokenHash hash) {
        jdbc.sql("""
                        INSERT INTO identity.identity_password_reset (id, account_id, token_hash, issued_at, expires_at)
                        VALUES (:id, :accountId, :tokenHash, :issuedAt, :expiresAt)
                        """)
                .param("id", token.id())
                .param("accountId", token.accountId())
                .param("tokenHash", hash.value())
                .param("issuedAt", JdbcTime.toDatabase(token.issuedAt()))
                .param("expiresAt", JdbcTime.toDatabase(token.expiresAt()))
                .update();
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(PasswordResetTokenHash hash) {
        return jdbc.sql("""
                        SELECT id, account_id, issued_at, expires_at, consumed_at, replaced_at
                        FROM identity.identity_password_reset
                        WHERE token_hash = :tokenHash
                        """)
                .param("tokenHash", hash.value())
                .query(JdbcPasswordResetRepository::mapToken)
                .optional();
    }

    @Override
    public Optional<PasswordResetToken> lockByTokenHash(PasswordResetTokenHash hash) {
        return jdbc.sql("""
                        SELECT id, account_id, issued_at, expires_at, consumed_at, replaced_at
                        FROM identity.identity_password_reset
                        WHERE token_hash = :tokenHash
                        FOR UPDATE
                        """)
                .param("tokenHash", hash.value())
                .query(JdbcPasswordResetRepository::mapToken)
                .optional();
    }

    @Override
    public void consume(UUID tokenId, Instant consumedAt) {
        int updated = jdbc.sql("""
                        UPDATE identity.identity_password_reset
                        SET consumed_at = :consumedAt
                        WHERE id = :id AND consumed_at IS NULL AND replaced_at IS NULL
                        """)
                .param("consumedAt", JdbcTime.toDatabase(consumedAt))
                .param("id", tokenId)
                .update();
        if (updated != 1) {
            throw new IllegalStateException(
                    "Password-reset interaction " + tokenId + " was not open; lock it with lockByTokenHash first");
        }
    }

    @Override
    public int deleteEndedBefore(Instant cutoff, int batchSize) {
        // An interaction ends when it is consumed or replaced, otherwise when it expires.
        return jdbc.sql("""
                        DELETE FROM identity.identity_password_reset
                        WHERE id IN (
                            SELECT id FROM identity.identity_password_reset
                            WHERE COALESCE(consumed_at, replaced_at, expires_at) <= :cutoff
                            ORDER BY id
                            LIMIT :batchSize
                            FOR UPDATE SKIP LOCKED)
                        """)
                .param("cutoff", JdbcTime.toDatabase(cutoff))
                .param("batchSize", batchSize)
                .update();
    }

    private static PasswordResetToken mapToken(ResultSet row, int rowNumber) throws SQLException {
        return new PasswordResetToken(
                row.getObject("id", UUID.class),
                row.getObject("account_id", UUID.class),
                JdbcTime.fromDatabase(row, "issued_at"),
                JdbcTime.fromDatabase(row, "expires_at"),
                JdbcTime.fromDatabase(row, "consumed_at"),
                JdbcTime.fromDatabase(row, "replaced_at"));
    }
}