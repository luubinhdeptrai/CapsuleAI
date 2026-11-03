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
                        INSERT INTO identity_account (id, email, password_hash, display_name, created_at)
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
                        FROM identity_account
                        WHERE email = :email
                        """)
                .param("email", email.value())
                .query(JdbcAccountRepository::mapAccount)
                .optional();
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