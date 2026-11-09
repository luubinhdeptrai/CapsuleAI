package com.capsuleai.architecture;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architecture refinements AR-01 and AR-04, checked on the fully migrated database
 * through PostgreSQL's own catalog: every business table lives in the schema of the
 * module its name names, Flyway's default schema holds only Flyway's history, and no
 * foreign key or view connects two owners.
 *
 * <p>This proves where objects are and how they are linked. It does not prove that only
 * the owner's code reads them: the application uses one database role with access to
 * every schema, so code ownership is enforced by {@code ArchitectureDependencyTest} and
 * {@code PersistenceOwnershipSqlTest}, not by database privileges.
 */
class DatabaseSchemaOwnershipIT extends IdentityIntegrationTestSupport {

    /** Schemas created by PostgreSQL itself. */
    private static final String SYSTEM_SCHEMAS = "n.nspname NOT LIKE 'pg\\_%' AND n.nspname <> 'information_schema'";

    @Test
    void everyNonSystemSchemaIsAnOwnerSchemaOrFlywaysDefaultSchema() {
        Set<String> allowed = Arrays.stream(BusinessModule.values()).map(BusinessModule::schema)
                .collect(Collectors.toCollection(HashSet::new));
        allowed.add(flywayDefaultSchema());

        List<String> schemas = jdbc.sql("SELECT n.nspname FROM pg_namespace n WHERE " + SYSTEM_SCHEMAS)
                .query(String.class).list();

        assertThat(schemas).isSubsetOf(allowed).contains("identity");
    }

    @Test
    void flywaysDefaultSchemaHoldsOnlyTheSchemaHistory() {
        List<String> relations = jdbc.sql("""
                        SELECT c.relname FROM pg_class c
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = :schema AND c.relkind IN ('r', 'p', 'v', 'm', 'S', 'f')
                        """)
                .param("schema", flywayDefaultSchema())
                .query(String.class).list();

        assertThat(relations).containsExactly("flyway_schema_history");
    }

    @Test
    void everyTableLivesInTheSchemaOfTheModuleItsNameNames() {
        List<Map<String, Object>> tables = jdbc.sql("""
                        SELECT n.nspname AS schema_name, c.relname AS table_name FROM pg_class c
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE c.relkind IN ('r', 'p') AND n.nspname <> :defaultSchema AND
                        """ + SYSTEM_SCHEMAS)
                .param("defaultSchema", flywayDefaultSchema())
                .query().listOfRows();

        assertThat(tables).isNotEmpty().allSatisfy(table -> assertThat(
                BusinessModule.ofTableName((String) table.get("table_name")).map(BusinessModule::schema))
                .as("owner of %s.%s", table.get("schema_name"), table.get("table_name"))
                .contains((String) table.get("schema_name")));
        assertThat(tables).extracting(table -> table.get("schema_name") + "." + table.get("table_name"))
                .contains("identity.identity_account", "identity.identity_session", "identity.identity_refresh_token");
    }

    @Test
    void noForeignKeyCrossesAnOwnerBoundary() {
        List<String> crossing = jdbc.sql("""
                        SELECT k.conname || ': ' || src_ns.nspname || '.' || src.relname
                               || ' -> ' || dst_ns.nspname || '.' || dst.relname
                        FROM pg_constraint k
                        JOIN pg_class src ON src.oid = k.conrelid
                        JOIN pg_namespace src_ns ON src_ns.oid = src.relnamespace
                        JOIN pg_class dst ON dst.oid = k.confrelid
                        JOIN pg_namespace dst_ns ON dst_ns.oid = dst.relnamespace
                        WHERE k.contype = 'f' AND src_ns.oid <> dst_ns.oid
                        """)
                .query(String.class).list();

        assertThat(crossing).isEmpty();
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM pg_constraint k
                        JOIN pg_namespace n ON n.oid = k.connamespace
                        WHERE k.contype = 'f' AND n.nspname = 'identity'
                        """).query(Long.class).single())
                .as("foreign keys inside Identity remain")
                .isPositive();
    }

    @Test
    void noViewOrRoutineInAnOwnerSchemaCouldReadAnotherOwnersTables() {
        List<String> viewsReadingAnotherSchema = jdbc.sql("""
                        SELECT DISTINCT view_ns.nspname || '.' || view_class.relname
                               || ' reads ' || used_ns.nspname || '.' || used.relname
                        FROM pg_rewrite r
                        JOIN pg_class view_class ON view_class.oid = r.ev_class
                        JOIN pg_namespace view_ns ON view_ns.oid = view_class.relnamespace
                        JOIN pg_depend d ON d.objid = r.oid AND d.classid = 'pg_rewrite'::regclass
                        JOIN pg_class used ON used.oid = d.refobjid
                        JOIN pg_namespace used_ns ON used_ns.oid = used.relnamespace
                        WHERE view_class.relkind IN ('v', 'm') AND used_ns.oid <> view_ns.oid
                          AND used_ns.nspname NOT IN ('pg_catalog', 'information_schema')
                        """)
                .query(String.class).list();
        // Function bodies cannot be analysed reliably, so owner schemas contain none;
        // introducing one needs an architecture review and an explicit change to this test.
        List<String> routines = jdbc.sql("""
                        SELECT n.nspname || '.' || p.proname FROM pg_proc p
                        JOIN pg_namespace n ON n.oid = p.pronamespace
                        WHERE n.nspname IN (:schemas)
                        """)
                .param("schemas", Arrays.stream(BusinessModule.values()).map(BusinessModule::schema).toList())
                .query(String.class).list();

        assertThat(viewsReadingAnotherSchema).isEmpty();
        assertThat(routines).isEmpty();
    }

    private String flywayDefaultSchema() {
        return jdbc.sql("""
                        SELECT n.nspname FROM pg_class c
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE c.relname = 'flyway_schema_history' AND c.relkind IN ('r', 'p')
                        """)
                .query(String.class).single();
    }
}
