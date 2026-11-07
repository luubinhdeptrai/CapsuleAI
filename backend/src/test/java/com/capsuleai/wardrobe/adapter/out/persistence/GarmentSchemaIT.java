package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The V8 constraints on their own, without the application: the database
 * refuses invalid vocabulary, applicability, Price, Brand and duplicate-addition
 * rows even if a future code path skipped validation (defence in depth).
 */
class GarmentSchemaIT extends WardrobeIntegrationTestSupport {

    private static final String WARDROBE_VERSION = "8";

    @Autowired
    private Flyway flyway;

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void wardrobeMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains(WARDROBE_VERSION);
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void aValidMinimumRowIsAccepted() {
        insertGarment(Map.of());

        assertThat(countRows("wardrobe.wardrobe_garment")).isOne();
    }

    @Test
    void categoryAndColorMustBeApprovedCodes() {
        assertRejected(Map.of("category", "SOCKS"));
        assertRejected(Map.of("color", "red"));
    }

    @Test
    void aSubtypeMustBelongToTheCategory() {
        assertRejected(Map.of("category", "PANTS", "subtype", "T_SHIRTS"));
        assertRejected(Map.of("category", "TOPS", "subtype", "TANKS_AND_CAMILS"));
        insertGarment(Map.of("category", "PANTS", "subtype", "JEANS"));
        insertGarment(Map.of("category", "TOPS", "subtype", "TANKS_AND_CAMIS"));
    }

    @Test
    void categorySpecificColumnsAreRefusedOutsideTheirCategories() {
        assertRejected(Map.of("category", "PANTS", "fit", "SLIM"));
        assertRejected(Map.of("category", "SHOES", "neckline", "ROUND"));
        assertRejected(Map.of("category", "SKIRTS", "sleeve_length", "SHORT_SLEEVE"));
        assertRejected(Map.of("category", "PANTS", "length", "CROPPED"));
        assertRejected(Map.of("category", "SKIRTS", "silhouette", "FLARE"));
        insertGarment(Map.of("category", "PANTS", "length", "LONG", "silhouette", "WIDE_LEG"));
    }

    @Test
    void priceIsCompleteNonNegativeAndExact() {
        assertRejected(Map.of("price_amount", new BigDecimal("10")));
        assertRejected(Map.of("price_currency", "USD"));
        assertRejected(Map.of("price_amount", new BigDecimal("-1"), "price_currency", "USD"));
        assertRejected(Map.of("price_amount", new BigDecimal("1.23456"), "price_currency", "USD"));
        assertRejected(Map.of("price_amount", new BigDecimal("10"), "price_currency", "usd"));

        UUID id = insertGarment(Map.of("price_amount", new BigDecimal("12.50"), "price_currency", "USD"));
        assertThat(jdbc.sql("SELECT price_amount::text FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(String.class).single()).isEqualTo("12.50");
    }

    @Test
    void brandIsNeverBlankAndAtMostOneHundredCharacters() {
        assertRejected(Map.of("brand", "   "));
        assertRejected(Map.of("brand", "b".repeat(101)));
        insertGarment(Map.of("brand", "b".repeat(100)));
    }

    @Test
    void aMaterialCannotBeRepeatedOnOneGarment() {
        UUID garmentId = insertGarment(Map.of());
        insertMaterial(garmentId, "COTTON");

        assertThatThrownBy(() -> insertMaterial(garmentId, "COTTON")).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertMaterial(garmentId, "PLASTIC")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void stylesAreApprovedDistinctRowsAndTheFormerSingleStyleColumnIsGone() {
        UUID garmentId = insertGarment(Map.of());
        insertStyle(garmentId, "CASUAL");
        insertStyle(garmentId, "STREET");

        assertThatThrownBy(() -> insertStyle(garmentId, "CASUAL")).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertStyle(garmentId, "ETC")).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertStyle(UUID.randomUUID(), "CASUAL"))
                .as("a Style belongs to an existing garment").isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.sql("SELECT count(*) FROM information_schema.columns "
                        + "WHERE table_schema = 'wardrobe' AND table_name = 'wardrobe_garment' AND column_name = 'style'")
                .query(Long.class).single()).isZero();
    }

    @Test
    void aCreationKeyIsUsedOncePerOwnerButIndependentlyAcrossOwners() {
        UUID owner = UUID.randomUUID();
        UUID key = UUID.randomUUID();
        insertGarment(Map.of("owner_account_id", owner, "creation_key", key));

        assertRejected(Map.of("owner_account_id", owner, "creation_key", key));
        insertGarment(Map.of("owner_account_id", UUID.randomUUID(), "creation_key", key));
    }

    @Test
    void wardrobeTablesLiveOnlyInTheWardrobeSchema() {
        assertThat(jdbc.sql("SELECT tablename FROM pg_tables WHERE schemaname = 'wardrobe' ORDER BY tablename")
                .query(String.class).list())
                .containsExactly("wardrobe_garment", "wardrobe_garment_material", "wardrobe_garment_style");
        assertThat(jdbc.sql("SELECT count(*) FROM pg_tables WHERE tablename LIKE 'wardrobe\\_%' AND schemaname <> 'wardrobe'")
                .query(Long.class).single()).isZero();
    }

    /**
     * A database in the state before this story (Sprint 1 and US-AUTH-005, V1-V7) is
     * upgraded by exactly the Wardrobe migration, which creates Wardrobe's own schema and
     * leaves Identity's data untouched.
     */
    @Test
    void existingIdentityDataSurvivesTheUpgradeFromV7() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("7");
            JdbcClient existing = database.jdbc();
            UUID accountId = UUID.randomUUID();
            existing.sql("INSERT INTO identity.identity_account (id, email, password_hash, created_at) "
                            + "VALUES (:id, 'existing@example.com', '{test}hash', now())")
                    .param("id", accountId).update();
            assertThat(existing.sql("SELECT to_regnamespace('wardrobe') IS NULL").query(Boolean.class).single())
                    .as("no Wardrobe schema before its first story").isTrue();

            assertThat(database.migrateTo(WARDROBE_VERSION).migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT count(*) FROM identity.identity_account WHERE id = :id")
                    .param("id", accountId).query(Long.class).single()).isOne();
            assertThat(existing.sql("SELECT count(*) FROM wardrobe.wardrobe_garment").query(Long.class).single())
                    .isZero();
            List<String> crossSchemaForeignKeys = existing.sql("""
                            SELECT k.conname FROM pg_constraint k
                            JOIN pg_class src ON src.oid = k.conrelid
                            JOIN pg_class dst ON dst.oid = k.confrelid
                            WHERE k.contype = 'f' AND src.relnamespace <> dst.relnamespace
                            """).query(String.class).list();
            assertThat(crossSchemaForeignKeys).isEmpty();
        }
    }

    private void assertRejected(Map<String, Object> overrides) {
        assertThatThrownBy(() -> insertGarment(overrides))
                .as("row with %s", overrides)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertGarment(Map<String, Object> overrides) {
        Map<String, Object> columns = new LinkedHashMap<>();
        UUID id = UUID.randomUUID();
        columns.put("id", id);
        columns.put("owner_account_id", UUID.randomUUID());
        columns.put("creation_key", UUID.randomUUID());
        columns.put("creation_fingerprint", "0".repeat(64));
        columns.put("category", "TOPS");
        columns.put("color", "NAVY");
        columns.put("value_provenance", "USER_ENTERED");
        columns.putAll(overrides);
        String names = String.join(", ", columns.keySet());
        String values = String.join(", ", columns.keySet().stream().map(name -> ":" + name).toList());
        var statement = jdbc.sql("INSERT INTO wardrobe.wardrobe_garment (" + names + ", created_at) VALUES ("
                + values + ", now())");
        for (Map.Entry<String, Object> column : columns.entrySet()) {
            statement = statement.param(column.getKey(), column.getValue());
        }
        statement.update();
        return (UUID) columns.get("id");
    }

    private void insertStyle(UUID garmentId, String style) {
        jdbc.sql("INSERT INTO wardrobe.wardrobe_garment_style (garment_id, style) VALUES (:garmentId, :style)")
                .param("garmentId", garmentId)
                .param("style", style)
                .update();
    }

    private void insertMaterial(UUID garmentId, String material) {
        jdbc.sql("INSERT INTO wardrobe.wardrobe_garment_material (garment_id, material) VALUES (:garmentId, :material)")
                .param("garmentId", garmentId)
                .param("material", material)
                .update();
    }
}
