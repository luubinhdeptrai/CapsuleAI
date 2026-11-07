package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.DisplayName;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.PasswordHash;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link AccountRepository}. */
public final class JdbcAccountRepository implements AccountRepository {

    private final JdbcClient jdbc;

    public JdbcAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addIfEmailAvailable(Account account) {
        int inserted = jdbc.sql("""
                        INSERT INTO identity.identity_account (id, email, password_hash, display_name, created_at)
                        VALUES (:id, :email, :passwordHash, :displayName, :createdAt)
                        ON CONFLICT (email) DO NOTHING
                        """)
                .param("id", account.id())
                .param("email", account.email().value())
                .param("passwordHash", account.passwordHash().value())
                .param("displayName", account.displayName().map(DisplayName::value).orElse(null), Types.VARCHAR)
                .param("createdAt", JdbcTime.toDatabase(account.createdAt()))
                .update();
        return inserted == 1;
    }

    @Override
    public Optional<Account> findByEmail(EmailAddress email) {
        return jdbc.sql("""
                        SELECT id, email, password_hash, display_name, created_at
                        FROM identity.identity_account
                        WHERE email = :email
                        """)
                .param("email", email.value())
                .query(JdbcAccountRepository::mapAccount)
                .optional();
    }

        @Override
    public Optional<PasswordHash> lockCredentialAgainstChange(UUID accountId) {
        return jdbc.sql("""
                        SELECT password_hash
                        FROM identity.identity_account
                        WHERE id = :id
                        FOR SHARE
                        """)
                .param("id", accountId)
                .query((row, rowNumber) -> new PasswordHash(row.getString("password_hash")))
                .optional();
    }

    @Override
    public boolean lockForCredentialChange(UUID accountId) {
        return jdbc.sql("""
                        SELECT id
                        FROM identity.identity_account
                        WHERE id = :id
                        FOR UPDATE
                        """)
                .param("id", accountId)
                .query(UUID.class)
                .optional()
                .isPresent();
    }

    @Override
    public void replacePasswordHash(UUID accountId, PasswordHash newPasswordHash) {
        int updated = jdbc.sql("""
                        UPDATE identity.identity_account
                        SET password_hash = :passwordHash
                        WHERE id = :id
                        """)
                .param("passwordHash", newPasswordHash.value())
                .param("id", accountId)
                .update();
        if (updated != 1) {
            throw new IllegalStateException("Account " + accountId + " does not exist");
        }
    }
    
    private static Account mapAccount(ResultSet row, int rowNumber) throws SQLException {
        return new Account(
                row.getObject("id", UUID.class),
                new EmailAddress(row.getString("email")),
                new PasswordHash(row.getString("password_hash")),
                Optional.ofNullable(row.getString("display_name")).map(DisplayName::new),
                JdbcTime.fromDatabase(row, "created_at"));
    }
}