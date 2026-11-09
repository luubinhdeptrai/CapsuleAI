package com.capsuleai.foundation;

import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * A new, empty PostgreSQL database on the test container's server, for migration tests
 * that must start from an earlier schema version with existing data.
 *
 * <p>It behaves like a real CapsuleAI database: Flyway runs the application's own
 * migration files with the application's default configuration (default schema and
 * schema history in the connection's default schema). It is separate from the
 * application's test database, so moving tables between schemas here cannot disturb
 * other tests. {@link #close()} drops it.
 */
public final class IsolatedMigrationDatabase implements AutoCloseable {

    private static final String MIGRATIONS = "classpath:db/migration";
    private static final int POSTGRESQL_PORT = 5432;

    private final JdbcClient serverAdministration;
    private final String databaseName;
    private final DataSource dataSource;

    private IsolatedMigrationDatabase(JdbcClient serverAdministration, String databaseName, DataSource dataSource) {
        this.serverAdministration = serverAdministration;
        this.databaseName = databaseName;
        this.dataSource = dataSource;
    }

    /**
     * Creates the database.
     *
     * @param server               the running test container
     * @param serverAdministration a client on any database of that server, used for CREATE/DROP DATABASE
     */
    public static IsolatedMigrationDatabase create(PostgreSQLContainer server, JdbcClient serverAdministration) {
        Objects.requireNonNull(server, "server");
        String databaseName = "migration_" + UUID.randomUUID().toString().replace("-", "");
        serverAdministration.sql("CREATE DATABASE " + databaseName).update();
        String url = "jdbc:postgresql://" + server.getHost() + ":" + server.getMappedPort(POSTGRESQL_PORT)
                + "/" + databaseName;
        DataSource dataSource = new DriverManagerDataSource(url, server.getUsername(), server.getPassword());
        return new IsolatedMigrationDatabase(serverAdministration, databaseName, dataSource);
    }

    /** Applies the application's migrations up to and including {@code version}. */
    public MigrateResult migrateTo(String version) {
        return flyway(version).migrate();
    }

    /** Applies every remaining application migration, as application startup does. */
    public MigrateResult migrateToLatest() {
        return flyway(null).migrate();
    }

    /** Flyway with the application's configuration; {@code target} null means latest. */
    public Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(dataSource)
                .locations(MIGRATIONS)
                .cleanDisabled(true);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    public JdbcClient jdbc() {
        return JdbcClient.create(dataSource);
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public String databaseName() {
        return databaseName;
    }

    @Override
    public void close() {
        serverAdministration.sql("DROP DATABASE IF EXISTS " + databaseName + " WITH (FORCE)").update();
    }
}
