package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ISSUED = Instant.parse("2026-01-05T08:00:00Z");
    private static final String PASSWORD_RESET_VERSION = "7";

    @Autowired
    private Flyway flyway;

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void passwordResetMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains(PASSWORD_RESET_VERSION);
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void onlyAHashOfTheSecretCanBeStored() {
        UUID accountId = insertAccount();

        assertThatThrownBy(() -> insertReset(accountId, "raw-reset-secret", ISSUED, ISSUED.plus(Duration.ofMinutes(30))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void anInteractionLivesAtMostThirtyMinutes() {
        UUID accountId = insertAccount();

        assertThatThrownBy(() -> insertReset(accountId, hash("too-long"), ISSUED,
                ISSUED.plus(Duration.ofMinutes(30)).plusSeconds(1)))
                .isInstanceOf(DataIntegrityViolationException.class);
        insertReset(accountId, hash("exactly-thirty"), ISSUED, ISSUED.plus(Duration.ofMinutes(30)));
    }

    @Test
    void anInteractionCannotBeBothConsumedAndReplaced() {
        UUID resetId = insertReset(insertAccount(), hash("one-outcome"), ISSUED, ISSUED.plus(Duration.ofMinutes(30)));

        assertThatThrownBy(() -> jdbc.sql("""
                        UPDATE identity.identity_password_reset SET consumed_at = :at, replaced_at = :at WHERE id = :id
                        """)
                .param("at", ISSUED.plusSeconds(60).atOffset(ZoneOffset.UTC))
                .param("id", resetId)
                .update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void anAccountHasAtMostOneOpenInteraction() {
        UUID accountId = insertAccount();
        UUID first = insertReset(accountId, hash("first"), ISSUED, ISSUED.plus(Duration.ofMinutes(30)));

        assertThatThrownBy(() -> insertReset(accountId, hash("second"), ISSUED, ISSUED.plus(Duration.ofMinutes(30))))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.sql("UPDATE identity.identity_password_reset SET replaced_at = :at WHERE id = :id")
                .param("at", ISSUED.plusSeconds(1).atOffset(ZoneOffset.UTC))
                .param("id", first)
                .update();
        insertReset(accountId, hash("second"), ISSUED, ISSUED.plus(Duration.ofMinutes(30)));
    }

    @Test
    void passwordResetIsAnAcceptedSessionRevocationReason() {
        UUID sessionId = insertSession(insertAccount());

        jdbc.sql("UPDATE identity.identity_session SET revoked_at = :at, revocation_reason = 'PASSWORD_RESET' WHERE id = :id")
                .param("at", ISSUED.plusSeconds(60).atOffset(ZoneOffset.UTC))
                .param("id", sessionId)
                .update();

        assertThat(jdbc.sql("SELECT revocation_reason FROM identity.identity_session WHERE id = :id")
                .param("id", sessionId).query(String.class).single()).isEqualTo("PASSWORD_RESET");
    }

    /**
     * A database in the state before this story (Sprint 1 data, already moved into the
     * identity schema by V6) is upgraded by exactly the password-reset migration.
     */
    @Test
    void existingAccountsAndSessionsSurviveTheUpgradeFromV6() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("6");
            JdbcClient existing = database.jdbc();
            UUID accountId = UUID.randomUUID();
            UUID activeSessionId = UUID.randomUUID();
            UUID loggedOutSessionId = UUID.randomUUID();
            existing.sql("INSERT INTO identity.identity_account (id, email, password_hash, created_at) "
                            + "VALUES (:id, 'existing@example.com', '{test}hash', now())")
                    .param("id", accountId).update();
            for (UUID sessionId : new UUID[] {activeSessionId, loggedOutSessionId}) {
                existing.sql("INSERT INTO identity.identity_session (id, account_id, established_at, expires_at) "
                                + "VALUES (:id, :accountId, now(), now() + INTERVAL '1 day')")
                        .param("id", sessionId).param("accountId", accountId).update();
            }
            existing.sql("UPDATE identity.identity_session "
                            + "SET revoked_at = now(), revocation_reason = 'LOGOUT' WHERE id = :id")
                    .param("id", loggedOutSessionId).update();

            assertThat(database.migrateTo(PASSWORD_RESET_VERSION).migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT password_hash FROM identity.identity_account WHERE id = :id")
                    .param("id", accountId).query(String.class).single()).isEqualTo("{test}hash");
            assertThat(existing.sql("SELECT revocation_reason FROM identity.identity_session WHERE id = :id")
                    .param("id", loggedOutSessionId).query(String.class).single()).isEqualTo("LOGOUT");
            existing.sql("UPDATE identity.identity_session "
                            + "SET revoked_at = now(), revocation_reason = 'PASSWORD_RESET' WHERE id = :id")
                    .param("id", activeSessionId).update();
            assertThat(existing.sql("SELECT count(*) FROM identity.identity_password_reset")
                    .query(Long.class).single()).isZero();
            assertThat(existing.sql("SELECT to_regclass('public.identity_password_reset') IS NULL")
                    .query(Boolean.class).single()).as("nothing created in Flyway's default schema").isTrue();
        }
    }

    private static String hash(String rawSecret) {
        return PasswordResetTokenHash.of(rawSecret).value();
    }

    private UUID insertAccount() {
        UUID accountId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity.identity_account (id, email, password_hash, created_at)
                        VALUES (:id, :email, '{test}hash', now())
                        """)
                .param("id", accountId)
                .param("email", accountId + "@example.com")
                .update();
        return accountId;
    }

    private UUID insertSession(UUID accountId) {
        UUID sessionId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity.identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", sessionId)
                .param("accountId", accountId)
                .param("establishedAt", ISSUED.atOffset(ZoneOffset.UTC))
                .param("expiresAt", ISSUED.plus(Duration.ofDays(30)).atOffset(ZoneOffset.UTC))
                .update();
        return sessionId;
    }

    private UUID insertReset(UUID accountId, String tokenHash, Instant issuedAt, Instant expiresAt) {
        UUID resetId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity.identity_password_reset (id, account_id, token_hash, issued_at, expires_at)
                        VALUES (:id, :accountId, :tokenHash, :issuedAt, :expiresAt)
                        """)
                .param("id", resetId)
                .param("accountId", accountId)
                .param("tokenHash", tokenHash)
                .param("issuedAt", issuedAt.atOffset(ZoneOffset.UTC))
                .param("expiresAt", expiresAt.atOffset(ZoneOffset.UTC))
                .update();
        return resetId;
    }
}
