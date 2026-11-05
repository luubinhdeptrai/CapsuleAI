package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * V6 moves the Sprint 1 Identity tables into the {@code identity} schema (architecture
 * refinement AR-01). Each test runs the real migration files on its own database, starting
 * from the Sprint 1 state (V1-V5) with representative accepted data, exactly as an existing
 * development or deployed database would be upgraded. Assertions about the relocation itself
 * stop at V6; later migrations (for example US-AUTH-005's) must then still apply on top.
 */
class IdentitySchemaRelocationIT extends IdentityIntegrationTestSupport {

    private static final String LAST_SPRINT_ONE_VERSION = "5";
    private static final String RELOCATION_VERSION = "6";
    private static final List<String> IDENTITY_TABLES =
            List.of("identity_account", "identity_refresh_token", "identity_session");
    private static final Instant ESTABLISHED = Instant.parse("2026-10-01T08:00:00Z");
    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD_HASH =
            "{pbkdf2-sha256-v1}c2FsdC1mb3ItdGVzdHM=$aGFzaC1mb3ItdGVzdHMtb25seQ==";

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void anExistingSprintOneDatabaseKeepsEveryRecordAndDefinitionWhenItsTablesMove() {
        try (IsolatedMigrationDatabase database = sprintOneDatabaseWithData()) {
            JdbcClient jdbc = database.jdbc();
            String defaultSchema = defaultSchemaOf(jdbc);
            Map<String, List<Map<String, Object>>> rowsBefore = rowsOf(jdbc, defaultSchema);
            Map<String, String> definitionsBefore = definitionsOf(jdbc, defaultSchema);

            MigrateResult relocation = database.migrateTo(RELOCATION_VERSION);

            assertThat(relocation.success).isTrue();
            assertThat(relocation.migrations).extracting(migration -> migration.version)
                    .containsExactly(RELOCATION_VERSION);
            assertThat(tablesIn(jdbc, "identity")).containsExactlyElementsOf(IDENTITY_TABLES);
            assertThat(tablesIn(jdbc, defaultSchema)).containsExactly("flyway_schema_history");
            assertThat(rowsOf(jdbc, "identity")).isEqualTo(rowsBefore);
            assertThat(definitionsOf(jdbc, "identity")).isEqualTo(definitionsBefore);
            assertThat(jdbc.sql("""
                            SELECT count(*) FROM pg_index i
                            JOIN pg_class c ON c.oid = i.indexrelid
                            JOIN pg_namespace n ON n.oid = c.relnamespace
                            WHERE n.nspname = 'identity' AND NOT (i.indisvalid AND i.indisready)
                            """).query(Long.class).single())
                    .as("indexes that are not valid and ready")
                    .isZero();
            assertThat(database.migrateToLatest().success).as("later migrations apply on top").isTrue();
            assertThat(rowsOf(jdbc, "identity")).isEqualTo(rowsBefore);
        }
    }

    @Test
    void theMovedTablesStillEnforceTheirKeysAndConstraints() {
        try (IsolatedMigrationDatabase database = sprintOneDatabaseWithData()) {
            database.migrateToLatest();
            JdbcClient jdbc = database.jdbc();

            assertThatThrownBy(() -> insertAccount(jdbc, "identity", UUID.randomUUID(), EXISTING_EMAIL, null))
                    .as("unique normalized email")
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThatThrownBy(() -> insertSession(jdbc, "identity", UUID.randomUUID(), UUID.randomUUID()))
                    .as("session of an unknown account")
                    .isInstanceOf(DataIntegrityViolationException.class);
            UUID sessionWithCurrentToken = jdbc.sql("""
                            SELECT session_id FROM identity.identity_refresh_token WHERE consumed_at IS NULL LIMIT 1
                            """).query(UUID.class).single();
            assertThatThrownBy(() -> insertRefreshToken(jdbc, "identity", sessionWithCurrentToken, "second-current", null))
                    .as("at most one current refresh token per session")
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThatThrownBy(() -> jdbc.sql("""
                            UPDATE identity.identity_session
                            SET revoked_at = established_at, revocation_reason = 'UNKNOWN'
                            WHERE id = :id
                            """).param("id", sessionWithCurrentToken).update())
                    .as("only approved revocation reasons")
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    void theIdentityPersistenceAdaptersWorkOnTheUpgradedRecords() {
        try (IsolatedMigrationDatabase database = sprintOneDatabaseWithData()) {
            database.migrateToLatest();
            JdbcAccountRepository accounts = new JdbcAccountRepository(database.jdbc());
            JdbcSessionRepository sessions = new JdbcSessionRepository(database.jdbc());

            assertThat(accounts.findByEmail(new EmailAddress(EXISTING_EMAIL)))
                    .hasValueSatisfying(account -> {
                        assertThat(account.passwordHash().value()).isEqualTo(EXISTING_PASSWORD_HASH);
                        assertThat(account.displayName()).hasValueSatisfying(
                                name -> assertThat(name.value()).isEqualTo("Nguyễn Văn An"));
                    });

            LockedRefreshCredential current = sessions.lockByRefreshToken(RefreshTokenHash.of("active-current"))
                    .orElseThrow();
            assertThat(current.token().isConsumed()).isFalse();
            assertThat(current.session().isUsableAt(ESTABLISHED.plus(Duration.ofDays(1)))).isTrue();
            Instant rotatedAt = ESTABLISHED.plus(Duration.ofHours(2));
            sessions.consumeRefreshToken(current.token().id(), rotatedAt);
            sessions.addRefreshToken(current.session().id(), RefreshTokenHash.of("active-rotated"), rotatedAt);
            assertThat(sessions.lockByRefreshToken(RefreshTokenHash.of("active-rotated"))).isPresent();

            LoginSession loggedOut = sessions.lockByRefreshToken(RefreshTokenHash.of("logged-out-current"))
                    .orElseThrow().session();
            assertThat(loggedOut.revocationReason()).isEqualTo(RevocationReason.LOGOUT);
            LoginSession reused = sessions.lockByRefreshToken(RefreshTokenHash.of("reused-consumed"))
                    .orElseThrow().session();
            assertThat(reused.revocationReason()).isEqualTo(RevocationReason.REFRESH_TOKEN_REUSE);

            assertThat(sessions.revoke(current.session().id(), current.session().accountId(),
                    RevocationReason.LOGOUT, rotatedAt)).isTrue();
            assertThat(sessions.findById(current.session().id())).get()
                    .extracting(LoginSession::isRevoked).isEqualTo(true);
        }
    }

    @Test
    void theSchemaHistoryStaysInPlaceAndARestartAppliesNothingAndChangesNothing() {
        try (IsolatedMigrationDatabase database = sprintOneDatabaseWithData()) {
            database.migrateToLatest();
            JdbcClient jdbc = database.jdbc();
            String defaultSchema = defaultSchemaOf(jdbc);
            Map<String, List<Map<String, Object>>> rowsAfterUpgrade = rowsOf(jdbc, "identity");

            MigrateResult restart = database.migrateToLatest();

            assertThat(restart.success).isTrue();
            assertThat(restart.migrationsExecuted).isZero();
            assertThat(database.flyway(null).validateWithResult().validationSuccessful).isTrue();
            assertThat(database.flyway(null).info().applied())
                    .extracting(MigrationInfo::getVersion)
                    .extracting(Object::toString)
                    .startsWith("1", "2", "3", "4", "5", RELOCATION_VERSION);
            assertThat(database.flyway(null).info().applied()).allMatch(migration -> migration.getState().isApplied());
            assertThat(jdbc.sql("SELECT to_regclass(:name) IS NOT NULL")
                    .param("name", defaultSchema + ".flyway_schema_history").query(Boolean.class).single())
                    .as("schema history still in Flyway's default schema").isTrue();
            assertThat(jdbc.sql("SELECT to_regclass('identity.flyway_schema_history') IS NULL")
                    .query(Boolean.class).single())
                    .as("no second schema history").isTrue();
            assertThat(rowsOf(jdbc, "identity")).isEqualTo(rowsAfterUpgrade);
        }
    }

    @Test
    void aFreshDatabaseRunsTheCompleteMigrationSequence() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            MigrateResult result = database.migrateToLatest();

            JdbcClient fresh = database.jdbc();
            assertThat(result.success).isTrue();
            assertThat(tablesIn(fresh, "identity")).containsAll(IDENTITY_TABLES);
            assertThat(tablesIn(fresh, defaultSchemaOf(fresh))).containsExactly("flyway_schema_history");
            assertThat(database.flyway(null).info().pending()).isEmpty();
        }
    }

    // ------------------------------------------------------------ fixtures

    /** A database in the exact Sprint 1 state (V1-V5) holding accepted Sprint 1 records. */
    private IsolatedMigrationDatabase sprintOneDatabaseWithData() {
        IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc);
        try {
            database.migrateTo(LAST_SPRINT_ONE_VERSION);
            JdbcClient legacy = database.jdbc();
            String schema = defaultSchemaOf(legacy);

            UUID existing = UUID.randomUUID();
            insertAccount(legacy, schema, existing, EXISTING_EMAIL, "Nguyễn Văn An");
            UUID withoutDisplayName = UUID.randomUUID();
            insertAccount(legacy, schema, withoutDisplayName, "second.user@example.com", null);

            UUID active = insertSession(legacy, schema, UUID.randomUUID(), existing);
            insertRefreshToken(legacy, schema, active, "active-consumed", ESTABLISHED.plusSeconds(600));
            insertRefreshToken(legacy, schema, active, "active-current", null);

            UUID loggedOut = insertSession(legacy, schema, UUID.randomUUID(), existing);
            insertRefreshToken(legacy, schema, loggedOut, "logged-out-current", null);
            revoke(legacy, schema, loggedOut, "LOGOUT");

            UUID reused = insertSession(legacy, schema, UUID.randomUUID(), existing);
            insertRefreshToken(legacy, schema, reused, "reused-consumed", ESTABLISHED.plusSeconds(60));
            revoke(legacy, schema, reused, "REFRESH_TOKEN_REUSE");

            UUID other = insertSession(legacy, schema, UUID.randomUUID(), withoutDisplayName);
            insertRefreshToken(legacy, schema, other, "other-current", null);
            return database;
        } catch (RuntimeException setupFailure) {
            database.close();
            throw setupFailure;
        }
    }

    private static void insertAccount(JdbcClient jdbc, String schema, UUID id, String email, String displayName) {
        jdbc.sql("INSERT INTO " + schema + ".identity_account (id, email, password_hash, display_name, created_at) "
                        + "VALUES (:id, :email, :hash, :displayName, :createdAt)")
                .param("id", id)
                .param("email", email)
                .param("hash", EXISTING_PASSWORD_HASH)
                .param("displayName", displayName, java.sql.Types.VARCHAR)
                .param("createdAt", ESTABLISHED.minus(Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
                .update();
    }

    private static UUID insertSession(JdbcClient jdbc, String schema, UUID id, UUID accountId) {
        jdbc.sql("INSERT INTO " + schema + ".identity_session (id, account_id, established_at, expires_at) "
                        + "VALUES (:id, :accountId, :establishedAt, :expiresAt)")
                .param("id", id)
                .param("accountId", accountId)
                .param("establishedAt", ESTABLISHED.atOffset(ZoneOffset.UTC))
                .param("expiresAt", ESTABLISHED.plus(Duration.ofDays(30)).atOffset(ZoneOffset.UTC))
                .update();
        return id;
    }

    private static void insertRefreshToken(JdbcClient jdbc, String schema, UUID sessionId, String rawToken,
                                           Instant consumedAt) {
        jdbc.sql("INSERT INTO " + schema + ".identity_refresh_token (id, session_id, token_hash, issued_at, consumed_at) "
                        + "VALUES (:id, :sessionId, :hash, :issuedAt, :consumedAt)")
                .param("id", UUID.randomUUID())
                .param("sessionId", sessionId)
                .param("hash", RefreshTokenHash.of(rawToken).value())
                .param("issuedAt", ESTABLISHED.atOffset(ZoneOffset.UTC))
                .param("consumedAt", consumedAt == null ? null : consumedAt.atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
    }

    private static void revoke(JdbcClient jdbc, String schema, UUID sessionId, String reason) {
        jdbc.sql("UPDATE " + schema + ".identity_session SET revoked_at = :at, revocation_reason = :reason WHERE id = :id")
                .param("at", ESTABLISHED.plusSeconds(900).atOffset(ZoneOffset.UTC))
                .param("reason", reason)
                .param("id", sessionId)
                .update();
    }

    // ------------------------------------------------------ inspection

    private static String defaultSchemaOf(JdbcClient jdbc) {
        return jdbc.sql("SELECT current_schema()").query(String.class).single();
    }

    private static List<String> tablesIn(JdbcClient jdbc, String schema) {
        return jdbc.sql("SELECT tablename FROM pg_tables WHERE schemaname = :schema ORDER BY tablename")
                .param("schema", schema).query(String.class).list();
    }

    /** Every row of every Identity table in {@code schema}, in a stable order. */
    private static Map<String, List<Map<String, Object>>> rowsOf(JdbcClient jdbc, String schema) {
        Map<String, List<Map<String, Object>>> rows = new LinkedHashMap<>();
        for (String table : IDENTITY_TABLES) {
            rows.put(table, jdbc.sql("SELECT * FROM " + schema + "." + table + " ORDER BY id").query().listOfRows());
        }
        return rows;
    }

    /**
     * Index and constraint definitions of the Identity tables in {@code schema}, with the
     * schema name removed so that definitions from different schemas can be compared.
     */
    private static Map<String, String> definitionsOf(JdbcClient jdbc, String schema) {
        Map<String, String> definitions = new LinkedHashMap<>();
        jdbc.sql("""
                        SELECT indexname, indexdef FROM pg_indexes
                        WHERE schemaname = :schema AND tablename LIKE 'identity\\_%'
                        ORDER BY indexname
                        """)
                .param("schema", schema)
                .query((row, number) -> Map.entry("index " + row.getString("indexname"),
                        row.getString("indexdef").replace(schema + ".", "")))
                .list()
                .forEach(entry -> definitions.put(entry.getKey(), entry.getValue()));
        jdbc.sql("""
                        SELECT c.relname AS table_name, k.conname, pg_get_constraintdef(k.oid) AS definition
                        FROM pg_constraint k
                        JOIN pg_class c ON c.oid = k.conrelid
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = :schema AND c.relname LIKE 'identity\\_%'
                        ORDER BY c.relname, k.conname
                        """)
                .param("schema", schema)
                .query((row, number) -> Map.entry(
                        "constraint " + row.getString("table_name") + "." + row.getString("conname"),
                        row.getString("definition").replace(schema + ".", "")))
                .list()
                .forEach(entry -> definitions.put(entry.getKey(), entry.getValue()));
        return definitions;
    }
}
