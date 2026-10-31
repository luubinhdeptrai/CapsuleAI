package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.out.persistence.JdbcAccountRepository;
import com.capsuleai.identity.adapter.out.security.Pbkdf2PasswordHasher;
import com.capsuleai.identity.application.RegisterAccountService;
import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.PasswordHasher;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Wires the Identity module's private implementations. */
@Configuration(proxyBeanMethods = false)
public class IdentityConfiguration {

    @Bean
    Clock identityClock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordHasher passwordHasher() {
        return new Pbkdf2PasswordHasher();
    }

    @Bean
    AccountRepository accountRepository(JdbcClient jdbcClient) {
        return new JdbcAccountRepository(jdbcClient);
    }

    @Bean
    RegisterAccountService registerAccountService(
            AccountRepository accounts, PasswordHasher passwordHasher, Clock clock) {
        return new RegisterAccountService(accounts, passwordHasher, clock);
    }
}