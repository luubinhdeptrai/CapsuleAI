package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.DisplayName;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountSchemaIT extends IdentityIntegrationTestSupport {

    @Autowired
    private Flyway flyway;

    @Autowired
    private AccountRepository accounts;

    @Test
    void accountMigrationIsAppliedAfterTheFoundationMigration() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("1", "2");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void databaseRejectsADuplicateEmailEvenWhenApplicationChecksAreBypassed() {
        insertAccountRow("dup@example.com");

        assertThatThrownBy(() -> insertAccountRow("dup@example.com"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(countRows("identity_account")).isOne();
    }

    @Test
    void repositoryReportsAnUnavailableEmailAndLeavesTheExistingRowUnchanged() {
        Account first = account("taken@example.com", "{test}first-hash", Optional.empty());
        Account second = account("taken@example.com", "{test}second-hash", Optional.of(new DisplayName("Intruder")));

        assertThat(accounts.addIfEmailAvailable(first)).isTrue();
        assertThat(accounts.addIfEmailAvailable(second)).isFalse();

        Map<String, Object> row = jdbc.sql("SELECT id, password_hash, display_name FROM identity_account")
                .query()
                .singleRow();
        assertThat(row.get("id")).isEqualTo(first.id());
        assertThat(row.get("password_hash")).isEqualTo("{test}first-hash");
        assertThat(row.get("display_name")).isNull();
    }

    @Test
    void storesTheDisplayNameExactlyIncludingDecomposedDiacritics() {
        String decomposed = Normalizer.normalize("Nguy\u1EC5n \u00C1nh", Normalizer.Form.NFD);
        accounts.addIfEmailAvailable(account("nfd@example.com", "{test}hash", Optional.of(new DisplayName(decomposed))));

        String stored = jdbc.sql("SELECT display_name FROM identity_account WHERE email = 'nfd@example.com'")
                .query(String.class)
                .single();

        assertThat(stored)
                .isEqualTo(decomposed)
                .isNotEqualTo(Normalizer.normalize(decomposed, Normalizer.Form.NFC));
    }

    private void insertAccountRow(String email) {
        jdbc.sql("""
                        INSERT INTO identity_account (id, email, password_hash, created_at)
                        VALUES (:id, :email, '{test}hash', now())
                        """)
                .param("id", UUID.randomUUID())
                .param("email", email)
                .update();
    }

    private static Account account(String email, String hash, Optional<DisplayName> displayName) {
        return new Account(UUID.randomUUID(), EmailAddress.normalize(email), new PasswordHash(hash),
                displayName, Instant.parse("2026-01-05T08:00:00Z"));
    }
}