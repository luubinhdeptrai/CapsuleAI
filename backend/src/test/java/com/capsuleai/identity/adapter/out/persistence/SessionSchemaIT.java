package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");

    @Autowired
    private Flyway flyway;

    @Test
    void sessionMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("3");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void sessionMustBelongToAnExistingAccount() {
        assertThatThrownBy(() -> insertSession(UUID.randomUUID(), ESTABLISHED, ESTABLISHED.plus(Duration.ofDays(30))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseEnforcesTheThirtyDayMaximum() {
        UUID accountId = insertAccount();

        insertSession(accountId, ESTABLISHED, ESTABLISHED.plus(Duration.ofHours(720)));

        assertThatThrownBy(() -> insertSession(accountId, ESTABLISHED,
                ESTABLISHED.plus(Duration.ofHours(720)).plusSeconds(1)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertSession(accountId, ESTABLISHED, ESTABLISHED))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void refreshTokenHashesAreUniqueAndWellFormed() {
        UUID accountId = insertAccount();
        UUID firstSession = insertSession(accountId, ESTABLISHED, ESTABLISHED.plus(Duration.ofDays(30)));
        UUID secondSession = insertSession(accountId, ESTABLISHED, ESTABLISHED.plus(Duration.ofDays(30)));
        String hash = RefreshTokenHash.of("some-refresh-token").value();

        insertRefreshToken(firstSession, hash);

        assertThatThrownBy(() -> insertRefreshToken(secondSession, hash))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertRefreshToken(secondSession, "NOT-A-SHA-256-HASH"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertAccount() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_account (id, email, password_hash, created_at)
                        VALUES (:id, :email, '{test}hash', now())
                        """)
                .param("id", id)
                .param("email", id + "@example.com")
                .update();
        return id;
    }

    private UUID insertSession(UUID accountId, Instant establishedAt, Instant expiresAt) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", id)
                .param("accountId", accountId)
                .param("establishedAt", establishedAt.atOffset(ZoneOffset.UTC))
                .param("expiresAt", expiresAt.atOffset(ZoneOffset.UTC))
                .update();
        return id;
    }

    private void insertRefreshToken(UUID sessionId, String tokenHash) {
        jdbc.sql("""
                        INSERT INTO identity_refresh_token (id, session_id, token_hash, issued_at)
                        VALUES (:id, :sessionId, :tokenHash, now())
                        """)
                .param("id", UUID.randomUUID())
                .param("sessionId", sessionId)
                .param("tokenHash", tokenHash)
                .update();
    }
}
