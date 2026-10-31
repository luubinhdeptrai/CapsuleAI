package com.capsuleai.identity.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Test infrastructure shared by all Identity integration tests. */
@TestConfiguration(proxyBeanMethods = false)
public class IdentityTestConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer identityPostgres() {
        return new PostgreSQLContainer("postgres:17.11-bookworm")
                .withDatabaseName("capsuleai_identity_test");
    }

    @Bean
    @Primary
    MutableClock mutableClock() {
        return new MutableClock(MutableClock.DEFAULT_START);
    }
}