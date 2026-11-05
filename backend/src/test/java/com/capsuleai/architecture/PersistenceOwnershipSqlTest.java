package com.capsuleai.architecture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architecture refinements AR-02 to AR-04 for SQL: production SQL names only its own
 * module's schema, always schema-qualified, and every migration after the Identity
 * relocation changes exactly one owner schema. Runs without a database.
 */
class PersistenceOwnershipSqlTest {

    private static final Path PRODUCTION_SOURCES = Path.of("src/main/java/com/capsuleai");
    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");

    private static final Set<String> FIXTURE_TABLES =
            Set.of("identity_account", "identity_session", "wardrobe_garment", "wardrobe_garment_material");

    @Test
    void productionSqlUsesOnlyTheOwningModulesSchemaAndAlwaysQualifiesIt() {
        Set<String> knownTables = SqlOwnershipRules.knownTables(migrations().values());
        assertThat(knownTables).as("tables found in the migrations").isNotEmpty();
        List<String> violations = new ArrayList<>();
        for (Path file : javaFiles()) {
            Path relative = PRODUCTION_SOURCES.relativize(file);
            Optional<BusinessModule> owner = relative.getNameCount() > 1
                    ? BusinessModule.ofPackageName(relative.getName(0).toString())
                    : Optional.empty();
            violations.addAll(SqlOwnershipRules.javaSourceViolations(owner, relative.toString(), read(file), knownTables));
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void everyMigrationAfterTheIdentityRelocationChangesExactlyOneOwnerSchema() {
        Map<String, String> migrations = migrations();
        Set<String> knownTables = SqlOwnershipRules.knownTables(migrations.values());
        List<String> violations = new ArrayList<>();
        migrations.forEach((name, sql) -> violations.addAll(SqlOwnershipRules.migrationViolations(name, sql, knownTables)));
        assertThat(violations).isEmpty();
    }

    @Test
    void onlyTheMigrationsWrittenBeforeTheRefinementAndTheRelocationAreExempt() {
        assertThat(SqlOwnershipRules.EXEMPT_MIGRATIONS).containsOnlyKeys(1, 2, 3, 4, 5, 6);
        assertThat(migrations()).containsKey("V6__move_identity_tables_into_identity_schema.sql");
    }

    // ---------------------------------------------- the checks detect violations

    @Test
    void aJoinAcrossTwoContextsIsReported() {
        String source = javaClass("""
                SELECT g.id
                FROM wardrobe.wardrobe_garment g
                JOIN identity.identity_account a ON g.owner_account_id = a.id
                """);

        assertThat(SqlOwnershipRules.javaSourceViolations(Optional.of(BusinessModule.WARDROBE), "Fixture.java",
                source, FIXTURE_TABLES))
                .singleElement().asString()
                .contains("module wardrobe uses identity.identity_account", "module identity owns", "published api");
    }

    @Test
    void aJoinInsideOneContextIsAccepted() {
        String source = javaClass("""
                SELECT g.id, m.material
                FROM wardrobe.wardrobe_garment g
                JOIN wardrobe.wardrobe_garment_material m ON m.garment_id = g.id
                WHERE g.owner_account_id = :owner
                """);

        assertThat(SqlOwnershipRules.javaSourceViolations(Optional.of(BusinessModule.WARDROBE), "Fixture.java",
                source, FIXTURE_TABLES)).isEmpty();
    }

    @Test
    void anUnqualifiedOwnerTableIsReportedWithItsQualifiedName() {
        String source = javaClass("SELECT id FROM identity_account WHERE email = :email");

        assertThat(SqlOwnershipRules.javaSourceViolations(Optional.of(BusinessModule.IDENTITY), "Fixture.java",
                source, FIXTURE_TABLES))
                .singleElement().asString()
                .contains("unqualified table identity_account", "write identity.identity_account");
    }

    @Test
    void anUnknownOwnerPrefixedTableAfterATableKeywordIsReportedToo() {
        String source = javaClass("INSERT INTO wardrobe_future_table (id) VALUES (:id)");

        assertThat(SqlOwnershipRules.javaSourceViolations(Optional.of(BusinessModule.WARDROBE), "Fixture.java",
                source, FIXTURE_TABLES))
                .singleElement().asString().contains("unqualified table wardrobe_future_table");
    }

    @Test
    void codeOutsideEveryModuleMayNotUseAnyOwnerTable() {
        String source = javaClass("SELECT count(*) FROM identity.identity_session");

        assertThat(SqlOwnershipRules.javaSourceViolations(Optional.empty(), "Root.java", source, FIXTURE_TABLES))
                .singleElement().asString().contains("code outside every module");
    }

    @Test
    void commentsConstraintNamesAndPlainTextAreIgnored() {
        String source = """
                class Fixture {
                    // SELECT * FROM identity.identity_account
                    /* JOIN identity.identity_session */
                    String constraint = "uq_identity_account_email";
                    String property = "capsuleai.identity.access-token";
                    String message = "The identity session could not be reached";
                    char quote = '"';
                    String sql = "SELECT id FROM wardrobe.wardrobe_garment";
                }
                """;

        assertThat(SqlOwnershipRules.javaSourceViolations(Optional.of(BusinessModule.WARDROBE), "Fixture.java",
                source, FIXTURE_TABLES)).isEmpty();
    }

    @Test
    void aMigrationWithACrossSchemaForeignKeyIsReported() {
        String migration = """
                CREATE SCHEMA IF NOT EXISTS wardrobe;
                CREATE TABLE wardrobe.wardrobe_garment (
                    id               UUID PRIMARY KEY,
                    owner_account_id UUID NOT NULL REFERENCES identity.identity_account (id)
                );
                """;

        assertThat(SqlOwnershipRules.migrationViolations("V8__create_wardrobe_garment.sql", migration, FIXTURE_TABLES))
                .singleElement().asString().contains("changes several owner schemas [identity, wardrobe]");
    }

    @Test
    void aMigrationWithForeignKeysInsideOneSchemaIsAccepted() {
        String migration = """
                -- Wardrobe owns its garments; the owner is a plain identifier (AR-04).
                CREATE SCHEMA IF NOT EXISTS wardrobe;
                CREATE TABLE wardrobe.wardrobe_garment (
                    id               UUID PRIMARY KEY,
                    owner_account_id UUID NOT NULL
                );
                CREATE TABLE wardrobe.wardrobe_garment_material (
                    garment_id UUID NOT NULL REFERENCES wardrobe.wardrobe_garment (id) ON DELETE CASCADE,
                    material   TEXT NOT NULL,
                    CONSTRAINT pk_wardrobe_garment_material PRIMARY KEY (garment_id, material)
                );
                CREATE INDEX ix_wardrobe_garment_owner ON wardrobe.wardrobe_garment (owner_account_id);
                """;

        assertThat(SqlOwnershipRules.migrationViolations("V8__create_wardrobe_garment.sql", migration, FIXTURE_TABLES))
                .isEmpty();
    }

    @Test
    void aMigrationMustQualifyItsTablesAndUseARegisteredSchema() {
        String migration = """
                CREATE SCHEMA catalogue;
                ALTER TABLE identity_session ADD COLUMN note TEXT;
                """;

        assertThat(SqlOwnershipRules.migrationViolations("V9__example.sql", migration, FIXTURE_TABLES))
                .anySatisfy(violation -> assertThat(violation).contains("schema catalogue is not a registered owner"))
                .anySatisfy(violation -> assertThat(violation).contains("unqualified table identity_session"));
    }

    @Test
    void onlyTheRelocationMayAddressFlywaysDefaultSchema() {
        String migration = "ALTER TABLE ${flyway:defaultSchema}.wardrobe_garment SET SCHEMA wardrobe;";

        assertThat(SqlOwnershipRules.migrationViolations("V9__example.sql", migration, FIXTURE_TABLES))
                .anySatisfy(violation -> assertThat(violation).contains("only the V6 relocation"));
    }

    @Test
    void aTableBelongsToTheModuleWithTheLongestMatchingPrefix() {
        assertThat(BusinessModule.ofTableName("wardrobe_intelligence_gap")).contains(BusinessModule.WARDROBE_INTELLIGENCE);
        assertThat(BusinessModule.ofTableName("wardrobe_garment")).contains(BusinessModule.WARDROBE);
        assertThat(BusinessModule.ofTableName("flyway_schema_history")).isEmpty();
    }

    // -------------------------------------------------------------- helpers

    private static String javaClass(String sql) {
        return "class Fixture {\n    String sql = \"\"\"\n" + sql + "\"\"\";\n}\n";
    }

    private static List<Path> javaFiles() {
        try (Stream<Path> files = Files.walk(PRODUCTION_SOURCES)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Map<String, String> migrations() {
        try (Stream<Path> files = Files.list(MIGRATIONS)) {
            Map<String, String> migrations = new TreeMap<>();
            files.filter(path -> path.toString().endsWith(".sql"))
                    .forEach(path -> migrations.put(path.getFileName().toString(), read(path)));
            return migrations;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
