package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-BULK-001: the V15 state machine and links on their own, and the upgrade from V14. */
class BulkImportSchemaIT extends WardrobeIntegrationTestSupport {

    private static final OffsetDateTime CREATED_AT = OffsetDateTime.of(2026, 11, 16, 9, 0, 0, 0, ZoneOffset.UTC);

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void everyItemStateHasExactlyItsLinks() {
        UUID batch = insertBatch(jdbc, 3, "OPEN");
        UUID garment = insertGarment(jdbc);

        assertThatThrownBy(() -> insertItem(batch, 1, "REVIEWABLE", null, null))
                .as("reviewable without photo").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertItem(batch, 1, "ACCEPTED", null, null))
                .as("accepted without garment").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertItem(batch, 1, "AWAITING_PHOTO", UUID.randomUUID(), null))
                .as("awaiting with photo").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertItem(batch, 1, "ACCEPTED", null, UUID.randomUUID()))
                .as("unknown garment").isInstanceOf(DataIntegrityViolationException.class);

        insertItem(batch, 1, "AWAITING_PHOTO", null, null);
        insertItem(batch, 2, "REVIEWABLE", UUID.randomUUID(), null);
        insertItem(batch, 3, "ACCEPTED", null, garment);
        assertThat(countRows("wardrobe.wardrobe_bulk_import_item")).isEqualTo(3);
    }

    @Test
    void aStagedPhotoBelongsToAtMostOneItemAndBatchesAreBounded() {
        UUID batch = insertBatch(jdbc, 2, "OPEN");
        UUID sharedPhoto = UUID.randomUUID();
        insertItem(batch, 1, "REVIEWABLE", sharedPhoto, null);

        assertThatThrownBy(() -> insertItem(batch, 2, "REVIEWABLE", sharedPhoto, null))
                .as("one stage, two items").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertItem(batch, 1, "AWAITING_PHOTO", null, null))
                .as("one position, two items").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertBatch(jdbc, 101, "OPEN"))
                .as("above the item ceiling").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertBatch(jdbc, 3, "EXPIRED"))
                .as("EXPIRED is never stored").isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existingGarmentsSurviveTheUpgradeFromV14() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("14");
            JdbcClient existing = database.jdbc();
            UUID garment = insertGarment(existing);

            assertThat(database.migrateTo("15").migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT count(*) FROM wardrobe.wardrobe_garment WHERE id = :id")
                    .param("id", garment).query(Long.class).single()).isOne();
            assertThat(existing.sql("SELECT count(*) FROM wardrobe.wardrobe_bulk_import_batch")
                    .query(Long.class).single()).as("no batch is invented").isZero();
        }
    }

    private static UUID insertBatch(JdbcClient client, int itemCount, String status) {
        UUID id = UUID.randomUUID();
        client.sql("""
                        INSERT INTO wardrobe.wardrobe_bulk_import_batch (id, owner_account_id, creation_key, item_count,
                            status, created_at, expires_at, stopped_at)
                        VALUES (:id, :owner, :creationKey, :itemCount, :status, :createdAt, :expiresAt, NULL)
                        """)
                .param("id", id)
                .param("owner", UUID.randomUUID())
                .param("creationKey", UUID.randomUUID())
                .param("itemCount", itemCount)
                .param("status", status)
                .param("createdAt", CREATED_AT)
                .param("expiresAt", CREATED_AT.plusDays(7))
                .update();
        return id;
    }

    private void insertItem(UUID batch, int queuePosition, String status, UUID photoUploadId, UUID garmentId) {
        jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_bulk_import_item (id, batch_id, queue_position, acceptance_key,
                            status, photo_upload_id, garment_id, revision, updated_at)
                        VALUES (:id, :batchId, :queuePosition, :acceptanceKey, :status, :photoUploadId, :garmentId, 1,
                            :updatedAt)
                        """)
                .param("id", UUID.randomUUID())
                .param("batchId", batch)
                .param("queuePosition", queuePosition)
                .param("acceptanceKey", UUID.randomUUID())
                .param("status", status)
                .param("photoUploadId", photoUploadId, Types.OTHER)
                .param("garmentId", garmentId, Types.OTHER)
                .param("updatedAt", CREATED_AT)
                .update();
    }

    private static UUID insertGarment(JdbcClient client) {
        UUID id = UUID.randomUUID();
        client.sql("""
                        INSERT INTO wardrobe.wardrobe_garment (id, owner_account_id, creation_key, creation_fingerprint,
                            category, color, value_provenance, created_at)
                        VALUES (:id, :owner, :creationKey, :fingerprint, 'TOPS', 'NAVY', 'USER_ENTERED', :createdAt)
                        """)
                .param("id", id)
                .param("owner", UUID.randomUUID())
                .param("creationKey", UUID.randomUUID())
                .param("fingerprint", "0".repeat(64))
                .param("createdAt", CREATED_AT)
                .update();
        return id;
    }
}
