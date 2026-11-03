package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionRevocationSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final String TRANSITION_SCHEMA = "identity_transition_v4";

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    void revocationMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("4");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void revocationTimeAndReasonMustBeRecordedTogether() {
        UUID sessionId = insertSession();

        assertThatThrownBy(() -> jdbc.sql("UPDATE identity_session SET revoked_at = now() WHERE id = :id")
                .param("id", sessionId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("UPDATE identity_session SET revocation_reason = 'LOGOUT' WHERE id = :id")
                .param("id", sessionId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyKnownRevocationReasonsAreAccepted() {
        UUID sessionId = insertSession();

        assertThatThrownBy(() -> revoke(sessionId, "DELETED_BY_ADMIN"))
                .isInstanceOf(DataIntegrityViolationException.class);

        revoke(sessionId, "LOGOUT");
        assertThat(jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId).query(String.class).single()).isEqualTo("LOGOUT");
    }

    @Test
    void sessionsCreatedBeforeTheMigrationRemainUnrevoked() {
        try {
            migrateTransitionSchema("3");
            UUID accountId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_account (id, email, password_hash, created_at) "
                            + "VALUES (:id, 'existing@example.com', '{test}hash', now())")
                    .param("id", accountId)
                    .update();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_session (id, account_id, established_at, expires_at) "
                            + "VALUES (:id, :accountId, now(), now() + INTERVAL '1 day')")
                    .param("id", sessionId)
                    .param("accountId", accountId)
                    .update();

            migrateTransitionSchema(null);

            Map<String, Object> row = jdbc.sql("SELECT revoked_at, revocation_reason FROM "
                            + TRANSITION_SCHEMA + ".identity_session WHERE id = :id")
                    .param("id", sessionId)
                    .query()
                    .singleRow();
            assertThat(row.get("revoked_at")).isNull();
            assertThat(row.get("revocation_reason")).isNull();
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

    private void revoke(UUID sessionId, String reason) {
        jdbc.sql("UPDATE identity_session SET revoked_at = :at, revocation_reason = :reason WHERE id = :id")
                .param("at", ESTABLISHED.plusSeconds(60).atOffset(ZoneOffset.UTC))
                .param("reason", reason)
                .param("id", sessionId)
                .update();
    }
}