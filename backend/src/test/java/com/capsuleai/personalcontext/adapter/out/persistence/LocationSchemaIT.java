package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-CTX-001: the V14 constraints on their own, and the upgrade from V13. */
class LocationSchemaIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void anIncompleteOrInvalidSelectionIsRejectedByTheDatabase() {
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), null, null, "DEVICE"))
                .as("latitude without longitude").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), new BigDecimal("106.70"),
                null, "MANUAL")).as("manual without label").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("95.00"), new BigDecimal("106.70"),
                null, "DEVICE")).as("latitude out of range").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), new BigDecimal("106.70"),
                "x".repeat(81), "MANUAL")).as("label too long").isInstanceOf(DataIntegrityViolationException.class);

        insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), new BigDecimal("106.70"), "Hồ Chí Minh", "MANUAL");
        insertLocation(UUID.randomUUID(), null, null, null, null);
        assertThat(countRows("personal_context.personal_context_location")).isEqualTo(2);
    }

    @Test
    void evidenceBelongsToALocationAndIsBounded() {
        UUID owner = UUID.randomUUID();
        insertLocation(owner, new BigDecimal("10.78"), new BigDecimal("106.70"), null, "DEVICE");

        assertThatThrownBy(() -> insertEvidence(UUID.randomUUID(), 50, "OPEN_METEO"))
                .as("evidence needs a location").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertEvidence(owner, 101, "OPEN_METEO"))
                .as("humidity is a percentage").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertEvidence(owner, 50, "SOMEWHERE_ELSE"))
                .as("only the configured provider").isInstanceOf(DataIntegrityViolationException.class);
        insertEvidence(owner, 50, "OPEN_METEO");
        assertThat(countRows("personal_context.personal_context_weather_evidence")).isOne();
    }

    @Test
    void existingOptionalContextSurvivesTheUpgradeFromV13() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("13");
            JdbcClient existing = database.jdbc();
            UUID owner = UUID.randomUUID();
            existing.sql("""
                            INSERT INTO personal_context.personal_context_optional_context (owner_account_id,
                                gender_context, revision, created_at, updated_at)
                            VALUES (:owner, 'NON_BINARY', 2, now(), now())
                            """)
                    .param("owner", owner).update();

            assertThat(database.migrateTo("14").migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT gender_context FROM personal_context.personal_context_optional_context "
                            + "WHERE owner_account_id = :owner")
                    .param("owner", owner).query(String.class).single()).isEqualTo("NON_BINARY");
            assertThat(existing.sql("SELECT count(*) FROM personal_context.personal_context_location")
                    .query(Long.class).single()).as("no location is invented").isZero();
        }
    }

    private void insertLocation(UUID owner, BigDecimal latitude, BigDecimal longitude, String label, String source) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_location (owner_account_id, latitude, longitude,
                            label, source, revision, created_at, updated_at)
                        VALUES (:owner, :latitude, :longitude, :label, :source, 1, now(), now())
                        """)
                .param("owner", owner)
                .param("latitude", latitude, Types.NUMERIC)
                .param("longitude", longitude, Types.NUMERIC)
                .param("label", label, Types.VARCHAR)
                .param("source", source, Types.VARCHAR)
                .update();
    }

    private void insertEvidence(UUID owner, int humidity, String provider) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_weather_evidence (owner_account_id,
                            location_revision, observed_at, retrieved_at, temperature_celsius, relative_humidity_percent,
                            weather_code, provider, basis)
                        VALUES (:owner, 1, now(), now(), 30.10, :humidity, 2, :provider, 'MODELLED')
                        """)
                .param("owner", owner)
                .param("humidity", humidity)
                .param("provider", provider)
                .update();
    }
}
