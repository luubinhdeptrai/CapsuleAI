package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.out.persistence.JdbcAccountRepository;
import com.capsuleai.identity.adapter.out.persistence.JdbcSessionRepository;
import com.capsuleai.identity.adapter.out.persistence.SpringIdentityTransactions;
import com.capsuleai.identity.adapter.out.security.Pbkdf2PasswordHasher;
import com.capsuleai.identity.application.LogInService;
import com.capsuleai.identity.application.RegisterAccountService;
import com.capsuleai.identity.application.SessionAuthorityService;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import com.capsuleai.identity.application.ports.SessionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

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
    SessionRepository sessionRepository(JdbcClient jdbcClient) {
        return new JdbcSessionRepository(jdbcClient);
    }

    @Bean
    IdentityTransactions identityTransactions(PlatformTransactionManager transactionManager) {
        return new SpringIdentityTransactions(transactionManager);
    }

    @Bean
    RegisterAccountService registerAccountService(
            AccountRepository accounts, PasswordHasher passwordHasher, Clock clock) {
        return new RegisterAccountService(accounts, passwordHasher, clock);
    }

    @Bean
    LogInService logInService(AccountRepository accounts,
                              PasswordHasher passwordHasher,
                              SessionRepository sessions,
                              RefreshTokenGenerator refreshTokens,
                              AccessTokenIssuer accessTokens,
                              IdentityTransactions transactions,
                              Clock clock) {
        return new LogInService(accounts, passwordHasher, sessions, refreshTokens, accessTokens, transactions, clock);
    }

    @Bean
    SessionAuthorityService sessionAuthorityService(SessionRepository sessions, Clock clock) {
        return new SessionAuthorityService(sessions, clock);
    }
}