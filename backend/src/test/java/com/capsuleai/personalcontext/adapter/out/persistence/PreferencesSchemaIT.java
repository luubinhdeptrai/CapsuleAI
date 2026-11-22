package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-001: the first Personal Context migration and its constraints (S3-PROF-01/02, AR-01). */
class PreferencesSchemaIT extends PersonalContextIntegrationTestSupport {

    @Test
    void theMigrationsCreateOnlyThePersonalContextSchemaAndItsTables() {
        assertThat(jdbc.sql("""
                        SELECT table_name FROM information_schema.tables
                        WHERE table_schema = 'personal_context' ORDER BY table_name
                        """).query(String.class).list())
                .containsExactly("personal_context_common_need",
                        "personal_context_optional_context", // V13, US-PROF-002
                        "personal_context_preferences",
                        "personal_context_preferred_style");
    }

    @Test
    void theDatabaseRefusesUnknownStylesNeedsAndPriorities() {
        UUID owner = insertProfile(1);

        assertThatThrownBy(() -> jdbc.sql("INSERT INTO personal_context.personal_context_preferred_style "
                + "(owner_account_id, style) VALUES (:owner, 'STREET')").param("owner", owner).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("INSERT INTO personal_context.personal_context_common_need "
                + "(owner_account_id, need, priority) VALUES (:owner, 'NIGHT_OUT', 1)").param("owner", owner).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("INSERT INTO personal_context.personal_context_common_need "
                + "(owner_account_id, need, priority) VALUES (:owner, 'TRAVEL', 4)").param("owner", owner).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.sql("INSERT INTO personal_context.personal_context_common_need (owner_account_id, need, priority) "
                + "VALUES (:owner, 'TRAVEL', 0)").param("owner", owner).update();
    }

    @Test
    void anOwnerHasOneProfileAndRevisionsArePositive() {
        UUID owner = insertProfile(1);

        assertThatThrownBy(() -> insertProfile(owner, 1)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertProfile(0)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertProfile(long revision) {
        UUID owner = UUID.randomUUID();
        insertProfile(owner, revision);
        return owner;
    }

    private void insertProfile(UUID owner, long revision) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_preferences (owner_account_id, revision, created_at, updated_at)
                        VALUES (:owner, :revision, now(), now())
                        """)
                .param("owner", owner).param("revision", revision).update();
    }
}
