package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenRotationSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final String TRANSITION_SCHEMA = "identity_transition_v5";

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    void rotationMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("5");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void aSessionHasAtMostOneCurrentRefreshToken() {
        UUID sessionId = insertSession();
        UUID first = insertRefreshToken(sessionId, RefreshTokenHash.of("first").value());

        assertThatThrownBy(() -> insertRefreshToken(sessionId, RefreshTokenHash.of("second").value()))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.sql("UPDATE identity_refresh_token SET consumed_at = issued_at WHERE id = :id")
                .param("id", first).update();
        insertRefreshToken(sessionId, RefreshTokenHash.of("second").value());

        assertThat(jdbc.sql("SELECT count(*) FROM identity_refresh_token WHERE session_id = :id")
                .param("id", sessionId).query(Long.class).single()).isEqualTo(2);
    }

    @Test
    void consumptionCannotPrecedeIssue() {
        UUID token = insertRefreshToken(insertSession(), RefreshTokenHash.of("early").value());

        assertThatThrownBy(() -> jdbc.sql("""
                        UPDATE identity_refresh_token
                        SET consumed_at = issued_at - INTERVAL '1 second'
                        WHERE id = :id
                        """)
                .param("id", token).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void reuseRevocationReasonIsAcceptedAndUnknownReasonsAreNot() {
        UUID sessionId = insertSession();

        assertThatThrownBy(() -> revoke(sessionId, "SOMETHING_ELSE"))
                .isInstanceOf(DataIntegrityViolationException.class);
        revoke(sessionId, "REFRESH_TOKEN_REUSE");

        assertThat(jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId).query(String.class).single()).isEqualTo("REFRESH_TOKEN_REUSE");
    }

    @Test
    void existingCurrentTokensRemainCurrentAfterTheMigration() {
        try {
            migrateTransitionSchema("4");
            UUID accountId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_account (id, email, password_hash, created_at) "
                            + "VALUES (:id, 'existing@example.com', '{test}hash', now())")
                    .param("id", accountId).update();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_session (id, account_id, established_at, expires_at) "
                            + "VALUES (:id, :accountId, now(), now() + INTERVAL '1 day')")
                    .param("id", sessionId).param("accountId", accountId).update();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_refresh_token (id, session_id, token_hash, issued_at) "
                            + "VALUES (:id, :sessionId, :hash, now())")
                    .param("id", UUID.randomUUID()).param("sessionId", sessionId)
                    .param("hash", RefreshTokenHash.of("existing").value()).update();

            migrateTransitionSchema(null);

            assertThat(jdbc.sql("SELECT consumed_at IS NULL FROM " + TRANSITION_SCHEMA
                            + ".identity_refresh_token WHERE session_id = :id")
                    .param("id", sessionId).query(Boolean.class).single()).isTrue();
            assertThatThrownBy(() -> jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA
                            + ".identity_refresh_token (id, session_id, token_hash, issued_at) "
                            + "VALUES (:id, :sessionId, :hash, now())")
                    .param("id", UUID.randomUUID()).param("sessionId", sessionId)
                    .param("hash", RefreshTokenHash.of("second-current").value()).update())
                    .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbc.sql("DROP SCHEMA IF EXISTS " + TRANSITION_SCHEMA + " CASCADE").update();
        }
    }

    /** Runs the real migration files into a scratch schema, up to {@code target} (null = latest). */
    private void migrateTransitionSchema(String target) {
        var configuration = Flyway.configure()
                .dataSource(dataSource)
                .schemas(TRANSITION_SCHEMA)
                .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        configuration.load().migrate();
    }

    private UUID insertSession() {
        UUID accountId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_account (id, email, password_hash, created_at)
                        VALUES (:id, :email, '{test}hash', now())
                        """)
                .param("id", accountId)
                .param("email", accountId + "@example.com")
                .update();
        UUID sessionId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", sessionId)
                .param("accountId", accountId)
                .param("establishedAt", ESTABLISHED.atOffset(ZoneOffset.UTC))
                .param("expiresAt", ESTABLISHED.plus(Duration.ofDays(30)).atOffset(ZoneOffset.UTC))
                .update();
        return sessionId;
    }

    private UUID insertRefreshToken(UUID sessionId, String tokenHash) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_refresh_token (id, session_id, token_hash, issued_at)
                        VALUES (:id, :sessionId, :tokenHash, :issuedAt)
                        """)
                .param("id", id)
                .param("sessionId", sessionId)
                .param("tokenHash", tokenHash)
                .param("issuedAt", ESTABLISHED.atOffset(ZoneOffset.UTC))
                .update();
        return id;
    }

    private void revoke(UUID sessionId, String reason) {
        jdbc.sql("UPDATE identity_session SET revoked_at = :at, revocation_reason = :reason WHERE id = :id")
                .param("at", ESTABLISHED.plusSeconds(60).atOffset(ZoneOffset.UTC))
                .param("reason", reason)
                .param("id", sessionId)
                .update();
    }
}