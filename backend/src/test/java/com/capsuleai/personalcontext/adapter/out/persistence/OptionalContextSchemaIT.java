package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.sql.Types;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-002: the V13 constraints on their own and the upgrade of existing preferences from V12. */
class OptionalContextSchemaIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private Flyway flyway;

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void theOptionalContextTableLivesInThePersonalContextSchema() {
        assertThat(flyway.info().applied()).extracting(migration -> migration.getVersion().getVersion()).contains("13");
        assertThat(jdbc.sql("SELECT count(*) FROM pg_tables WHERE schemaname = 'personal_context' "
                        + "AND tablename = 'personal_context_optional_context'")
                .query(Long.class).single()).isOne();
    }

    @Test
    void onlySupportedCodesAndConsistentRevisionsAreStored() {
        assertThatThrownBy(() -> insert(UUID.randomUUID(), "woman", null, 1))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insert(UUID.randomUUID(), null, "SLIM", 1))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insert(UUID.randomUUID(), null, null, 0))
                .isInstanceOf(DataIntegrityViolationException.class);
        insert(UUID.randomUUID(), "ANOTHER_GENDER", "INVERTED_TRIANGLE", 1);
        insert(UUID.randomUUID(), null, null, 3);
        assertThat(countRows("personal_context.personal_context_optional_context")).isEqualTo(2);
    }

    /**
     * One record per owner; only the two values are nullable; the update time never precedes the creation time;
     * and no foreign key ties the record to Identity (AR-04) or to the preferences (S4-PROF-02).
     */
    @Test
    void oneRecordPerOwnerWithOnlyTheValuesNullableOrderedTimesAndNoForeignKey() {
        assertThat(jdbc.sql("""
                        SELECT column_name || ':' || data_type || ':' || is_nullable FROM information_schema.columns
                        WHERE table_schema = 'personal_context' AND table_name = 'personal_context_optional_context'
                        ORDER BY ordinal_position
                        """).query(String.class).list())
                .containsExactly("owner_account_id:uuid:NO", "gender_context:text:YES", "body_shape:text:YES",
                        "revision:bigint:NO", "created_at:timestamp with time zone:NO",
                        "updated_at:timestamp with time zone:NO");
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM information_schema.table_constraints
                        WHERE table_schema = 'personal_context' AND table_name = 'personal_context_optional_context'
                          AND constraint_type = 'FOREIGN KEY'
                        """).query(Long.class).single()).isZero();

        UUID owner = UUID.randomUUID();
        insert(owner, "WOMAN", null, 1);
        assertThatThrownBy(() -> insert(owner, "MAN", null, 1)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("""
                        INSERT INTO personal_context.personal_context_optional_context (owner_account_id,
                            gender_context, body_shape, revision, created_at, updated_at)
                        VALUES (:owner, NULL, NULL, 1, now(), now() - interval '1 second')
                        """).param("owner", UUID.randomUUID()).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(countRows("personal_context.personal_context_optional_context")).isOne();
    }

    @Test
    void existingPreferencesSurviveTheUpgradeFromV12() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("12");
            JdbcClient existing = database.jdbc();
            UUID owner = UUID.randomUUID();
            existing.sql("""
                            INSERT INTO personal_context.personal_context_preferences (owner_account_id, revision,
                                created_at, updated_at)
                            VALUES (:owner, 4, now(), now())
                            """)
                    .param("owner", owner).update();

            assertThat(database.migrateTo("13").migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT revision FROM personal_context.personal_context_preferences "
                            + "WHERE owner_account_id = :owner")
                    .param("owner", owner).query(Long.class).single()).isEqualTo(4L);
            assertThat(existing.sql("SELECT count(*) FROM personal_context.personal_context_optional_context")
                    .query(Long.class).single()).as("no optional context is invented").isZero();
        }
    }

    private void insert(UUID owner, String genderContext, String bodyShape, long revision) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_optional_context (owner_account_id,
                            gender_context, body_shape, revision, created_at, updated_at)
                        VALUES (:owner, :gender, :body, :revision, now(), now())
                        """)
                .param("owner", owner)
                .param("gender", genderContext, Types.VARCHAR)
                .param("body", bodyShape, Types.VARCHAR)
                .param("revision", revision)
                .update();
    }
}
