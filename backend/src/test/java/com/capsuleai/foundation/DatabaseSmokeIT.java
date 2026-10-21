package com.capsuleai.foundation;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class DatabaseSmokeIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17.11-bookworm")
                    .withDatabaseName("capsuleai_test");

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    Flyway flyway;

    @Test
    void connectsAndAppliesFoundationMigration() {

        assertThat(
            jdbc.queryForObject("SELECT 1", Integer.class)
        ).isEqualTo(1);

        assertThat(
            jdbc.queryForObject(
                """
                SELECT count(*)
                FROM flyway_schema_history
                WHERE version = '1' AND success
                """,
                Long.class
            )
        ).isEqualTo(1L);

        assertThat(flyway.info().pending()).isEmpty();
    }
}