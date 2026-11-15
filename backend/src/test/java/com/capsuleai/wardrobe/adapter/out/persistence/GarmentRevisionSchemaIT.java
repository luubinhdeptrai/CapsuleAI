package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-002: V9 upgrades existing wardrobes safely and the database defends the revision (S3-GAR-02/05). */
class GarmentRevisionSchemaIT extends WardrobeIntegrationTestSupport {

    private static final String INSERT_MINIMUM_GARMENT = """
            INSERT INTO wardrobe.wardrobe_garment (id, owner_account_id, creation_key, creation_fingerprint,
                category, color, value_provenance, created_at)
            VALUES (:id, :owner, :key, :fingerprint, 'TOPS', 'WHITE', 'USER_ENTERED', now())
            """;

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void upgradingAWardrobeFromV8KeepsEveryGarmentAndStartsItAtRevisionOne() throws Exception {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("8");
            JdbcClient isolated = database.jdbc();
            UUID garmentId = insertMinimumGarment(isolated);
            isolated.sql("INSERT INTO wardrobe.wardrobe_garment_material (garment_id, material) VALUES (:id, 'COTTON')")
                    .param("id", garmentId)
                    .update();

            database.migrateTo("9");

            assertThat(isolated.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                    .param("id", garmentId).query(Long.class).single()).isEqualTo(1L);
            assertThat(isolated.sql("SELECT count(*) FROM wardrobe.wardrobe_garment_material WHERE garment_id = :id")
                    .param("id", garmentId).query(Long.class).single()).isOne();
        }
    }

    @Test
    void theDatabaseDefaultsTheRevisionAndRefusesAMissingOrNonPositiveOne() {
        UUID garmentId = insertMinimumGarment(jdbc);

        assertThat(jdbc.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(Long.class).single()).isEqualTo(1L);
        assertThatThrownBy(() -> jdbc.sql("UPDATE wardrobe.wardrobe_garment SET revision = 0 WHERE id = :id")
                .param("id", garmentId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("UPDATE wardrobe.wardrobe_garment SET revision = NULL WHERE id = :id")
                .param("id", garmentId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static UUID insertMinimumGarment(JdbcClient client) {
        UUID garmentId = UUID.randomUUID();
        client.sql(INSERT_MINIMUM_GARMENT)
                .param("id", garmentId)
                .param("owner", UUID.randomUUID())
                .param("key", UUID.randomUUID())
                .param("fingerprint", "0".repeat(64))
                .update();
        return garmentId;
    }
}
