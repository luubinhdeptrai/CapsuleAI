package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.DisplayName;
import java.sql.Types;
import java.time.ZoneOffset;
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
                .param("createdAt", account.createdAt().atOffset(ZoneOffset.UTC))
                .update();
        return inserted == 1;
    }
}