package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.sql.Types;
import java.time.OffsetDateTime;
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

/** US-GAR-005: the V12 constraints on their own, and the upgrade of existing garments from V11. */
class GarmentRemovalSchemaIT extends WardrobeIntegrationTestSupport {

    private static final String REMOVAL_VERSION = "12";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.of(2026, 11, 1, 8, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime REMOVED_AT = CREATED_AT.plusDays(15);

    @Autowired
    private Flyway flyway;

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void theRemovalMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains(REMOVAL_VERSION);
    }

    @Test
    void aMinimizedGarmentKeepsNoRichValueAndIsNeverMinimizedBeforeItsRemoval() {
        UUID richRemoved = insertGarment(jdbc, REMOVED_AT, "Uniqlo");
        assertThatThrownBy(() -> minimize(richRemoved, REMOVED_AT))
                .as("a minimized row cannot keep its Brand").isInstanceOf(DataIntegrityViolationException.class);

        UUID current = insertGarment(jdbc, null, null);
        assertThatThrownBy(() -> minimize(current, REMOVED_AT))
                .as("a current garment cannot be minimized").isInstanceOf(DataIntegrityViolationException.class);

        UUID plainRemoved = insertGarment(jdbc, REMOVED_AT, null);
        assertThatThrownBy(() -> minimize(plainRemoved, REMOVED_AT.minusDays(1)))
                .as("minimization cannot precede removal").isInstanceOf(DataIntegrityViolationException.class);
        minimize(plainRemoved, REMOVED_AT);
        assertThat(jdbc.sql("SELECT minimized_at IS NOT NULL FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", plainRemoved).query(Boolean.class).single()).isTrue();
    }

    @Test
    void cleanupWorkBelongsToAnExistingGarmentAndRecordsItsStepsConsistently() {
        UUID garmentId = insertGarment(jdbc, REMOVED_AT, null);

        assertThatThrownBy(() -> insertCleanup(UUID.randomUUID(), null, null, null, null))
                .as("an obligation needs its garment").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertCleanup(garmentId, UUID.randomUUID(), null, null, null))
                .as("photo id and object key come together").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertCleanup(garmentId, null, null, null, REMOVED_AT))
                .as("completion needs minimization").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertCleanup(garmentId, UUID.randomUUID(), "photos/not-a-key.png", null, null))
                .as("only server-generated photo keys").isInstanceOf(DataIntegrityViolationException.class);

        insertCleanup(garmentId, UUID.randomUUID(), "photos/" + UUID.randomUUID() + ".jpg", REMOVED_AT, null);
        assertThat(countRows("wardrobe.wardrobe_garment_removal_cleanup")).isOne();
        assertThatThrownBy(() -> insertCleanup(garmentId, null, null, null, null))
                .as("one obligation per garment").isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * A database at V11 (end of Sprint 3) is upgraded by exactly the removal migration; existing garments
     * keep every value and are not minimized.
     */
    @Test
    void existingGarmentsSurviveTheUpgradeFromV11Unminimized() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("11");
            JdbcClient existing = database.jdbc();
            UUID garmentId = insertGarment(existing, null, "Uniqlo");

            assertThat(database.migrateTo(REMOVAL_VERSION).migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT brand FROM wardrobe.wardrobe_garment WHERE id = :id")
                    .param("id", garmentId).query(String.class).single()).isEqualTo("Uniqlo");
            assertThat(existing.sql("SELECT minimized_at IS NULL FROM wardrobe.wardrobe_garment WHERE id = :id")
                    .param("id", garmentId).query(Boolean.class).single()).isTrue();
            assertThat(existing.sql("SELECT count(*) FROM wardrobe.wardrobe_garment_removal_cleanup")
                    .query(Long.class).single()).isZero();
        }
    }

    private static UUID insertGarment(JdbcClient client, OffsetDateTime removedAt, String brand) {
        UUID id = UUID.randomUUID();
        client.sql("""
                        INSERT INTO wardrobe.wardrobe_garment (id, owner_account_id, creation_key, creation_fingerprint,
                            category, color, brand, value_provenance, created_at, removed_at)
                        VALUES (:id, :owner, :creationKey, :fingerprint, 'TOPS', 'NAVY', :brand, 'USER_ENTERED',
                            :createdAt, :removedAt)
                        """)
                .param("id", id)
                .param("owner", UUID.randomUUID())
                .param("creationKey", UUID.randomUUID())
                .param("fingerprint", "0".repeat(64))
                .param("brand", brand, Types.VARCHAR)
                .param("createdAt", CREATED_AT)
                .param("removedAt", removedAt, Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
        return id;
    }

    private void minimize(UUID garmentId, OffsetDateTime minimizedAt) {
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET minimized_at = :minimizedAt WHERE id = :id")
                .param("id", garmentId)
                .param("minimizedAt", minimizedAt)
                .update();
    }

    private void insertCleanup(UUID garmentId, UUID photoUploadId, String objectKey, OffsetDateTime minimizedAt,
                               OffsetDateTime completedAt) {
        jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_garment_removal_cleanup (garment_id, owner_account_id,
                            removed_at, purge_deadline, photo_upload_id, photo_object_key, next_attempt_at,
                            data_minimized_at, completed_at)
                        VALUES (:garmentId, :owner, :removedAt, :deadline, :photoUploadId, :objectKey, :removedAt,
                            :minimizedAt, :completedAt)
                        """)
                .param("garmentId", garmentId)
                .param("owner", UUID.randomUUID())
                .param("removedAt", REMOVED_AT)
                .param("deadline", REMOVED_AT.plusDays(30))
                .param("photoUploadId", photoUploadId, Types.OTHER)
                .param("objectKey", objectKey, Types.VARCHAR)
                .param("minimizedAt", minimizedAt, Types.TIMESTAMP_WITH_TIMEZONE)
                .param("completedAt", completedAt, Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
    }
}
