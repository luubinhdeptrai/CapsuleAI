package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.LocationSource;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link LocationRepository} (V14, schema personal_context). */
public final class JdbcLocationRepository implements LocationRepository {

    static final String SELECT_LOCATION = """
            SELECT owner_account_id, latitude, longitude, label, source, revision, created_at, updated_at
            FROM personal_context.personal_context_location
            WHERE owner_account_id = :ownerAccountId
            """;

    static final String CREATE_IF_ABSENT = """
            INSERT INTO personal_context.personal_context_location (
                owner_account_id, latitude, longitude, label, source, revision, created_at, updated_at)
            VALUES (:ownerAccountId, :latitude, :longitude, :label, :source, :revision, :createdAt, :updatedAt)
            ON CONFLICT (owner_account_id) DO NOTHING
            """;

    static final String REVISE_IF_CURRENT_AT = """
            UPDATE personal_context.personal_context_location
            SET latitude = :latitude, longitude = :longitude, label = :label, source = :source,
                revision = revision + 1, updated_at = :updatedAt
            WHERE owner_account_id = :ownerAccountId AND revision = :expectedRevision
            """;

    static final String DELETE_EVIDENCE = """
            DELETE FROM personal_context.personal_context_weather_evidence WHERE owner_account_id = :ownerAccountId
            """;

    static final String SELECT_EVIDENCE = """
            SELECT location_revision, observed_at, retrieved_at, temperature_celsius, apparent_temperature_celsius,
                   relative_humidity_percent, precipitation_millimetres, wind_speed_kilometres_per_hour,
                   weather_code, provider, basis
            FROM personal_context.personal_context_weather_evidence
            WHERE owner_account_id = :ownerAccountId
            """;

    /** Holds the selection in share mode: a change or clear waits until this publication commits, and vice versa. */
    static final String LOCK_CURRENT_SELECTION = """
            SELECT revision FROM personal_context.personal_context_location
            WHERE owner_account_id = :ownerAccountId AND revision = :locationRevision AND latitude IS NOT NULL
            FOR SHARE
            """;

    /** Latest evidence only; never replaced by evidence of an older revision or an older observation. */
    static final String UPSERT_EVIDENCE = """
            INSERT INTO personal_context.personal_context_weather_evidence AS stored (
                owner_account_id, location_revision, observed_at, retrieved_at, temperature_celsius,
                apparent_temperature_celsius, relative_humidity_percent, precipitation_millimetres,
                wind_speed_kilometres_per_hour, weather_code, provider, basis)
            VALUES (:ownerAccountId, :locationRevision, :observedAt, :retrievedAt, :temperatureCelsius,
                    :apparentTemperatureCelsius, :relativeHumidityPercent, :precipitationMillimetres,
                    :windSpeedKilometresPerHour, :weatherCode, :provider, :basis)
            ON CONFLICT (owner_account_id) DO UPDATE SET
                location_revision = EXCLUDED.location_revision, observed_at = EXCLUDED.observed_at,
                retrieved_at = EXCLUDED.retrieved_at, temperature_celsius = EXCLUDED.temperature_celsius,
                apparent_temperature_celsius = EXCLUDED.apparent_temperature_celsius,
                relative_humidity_percent = EXCLUDED.relative_humidity_percent,
                precipitation_millimetres = EXCLUDED.precipitation_millimetres,
                wind_speed_kilometres_per_hour = EXCLUDED.wind_speed_kilometres_per_hour,
                weather_code = EXCLUDED.weather_code, provider = EXCLUDED.provider, basis = EXCLUDED.basis
            WHERE stored.location_revision < EXCLUDED.location_revision
               OR (stored.location_revision = EXCLUDED.location_revision AND stored.observed_at < EXCLUDED.observed_at)
            """;

    private final JdbcClient jdbcClient;

    public JdbcLocationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<LocationProfile> find(UUID ownerAccountId) {
        return jdbcClient.sql(SELECT_LOCATION)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> mapProfile(row))
                .optional();
    }

    @Override
    public boolean createIfAbsent(LocationProfile profile) {
        return bindSelection(jdbcClient.sql(CREATE_IF_ABSENT), profile.selection())
                .param("ownerAccountId", profile.ownerAccountId())
                .param("revision", profile.revision())
                .param("createdAt", toDatabase(profile.createdAt()))
                .param("updatedAt", toDatabase(profile.updatedAt()))
                .update() == 1;
    }

    @Override
    public boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, Optional<LocationSelection> selection,
                                     Instant updatedAt) {
        int updated = bindSelection(jdbcClient.sql(REVISE_IF_CURRENT_AT), selection)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .param("updatedAt", toDatabase(updatedAt))
                .update();
        if (updated == 0) {
            return false;
        }
        // The UPDATE holds the location row lock until commit; evidence of the old selection goes with it.
        jdbcClient.sql(DELETE_EVIDENCE).param("ownerAccountId", ownerAccountId).update();
        return true;
    }

    @Override
    public Optional<StoredWeatherEvidence> findEvidence(UUID ownerAccountId) {
        return jdbcClient.sql(SELECT_EVIDENCE)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> new StoredWeatherEvidence(row.getLong("location_revision"), mapReading(row)))
                .optional();
    }

    @Override
    public WeatherPublication publishEvidenceIfCurrent(UUID ownerAccountId, long locationRevision,
                                                       WeatherReading reading) {
        Optional<Long> lockedRevision = jdbcClient.sql(LOCK_CURRENT_SELECTION)
                .param("ownerAccountId", ownerAccountId)
                .param("locationRevision", locationRevision)
                .query(Long.class)
                .optional();
        if (lockedRevision.isEmpty()) {
            return WeatherPublication.SELECTION_CHANGED;
        }
        int written = jdbcClient.sql(UPSERT_EVIDENCE)
                .param("ownerAccountId", ownerAccountId)
                .param("locationRevision", locationRevision)
                .param("observedAt", toDatabase(reading.observedAt()))
                .param("retrievedAt", toDatabase(reading.retrievedAt()))
                .param("temperatureCelsius", reading.temperatureCelsius())
                .param("apparentTemperatureCelsius", reading.apparentTemperatureCelsius().orElse(null), Types.NUMERIC)
                .param("relativeHumidityPercent", reading.relativeHumidityPercent().orElse(null), Types.SMALLINT)
                .param("precipitationMillimetres", reading.precipitationMillimetres().orElse(null), Types.NUMERIC)
                .param("windSpeedKilometresPerHour", reading.windSpeedKilometresPerHour().orElse(null), Types.NUMERIC)
                .param("weatherCode", (short) reading.weatherCode())
                .param("provider", reading.provider())
                .param("basis", reading.basis())
                .update();
        return written == 1 ? WeatherPublication.PUBLISHED : WeatherPublication.KEPT_NEWER;
    }

    private static JdbcClient.StatementSpec bindSelection(JdbcClient.StatementSpec statement,
                                                          Optional<LocationSelection> selection) {
        return statement
                .param("latitude", selection.map(LocationSelection::latitude).orElse(null), Types.NUMERIC)
                .param("longitude", selection.map(LocationSelection::longitude).orElse(null), Types.NUMERIC)
                .param("label", selection.flatMap(LocationSelection::label).orElse(null), Types.VARCHAR)
                .param("source", selection.map(chosen -> chosen.source().name()).orElse(null), Types.VARCHAR);
    }

    private static LocationProfile mapProfile(ResultSet row) throws SQLException {
        BigDecimal latitude = row.getBigDecimal("latitude");
        Optional<LocationSelection> selection = latitude == null
                ? Optional.empty()
                : Optional.of(new LocationSelection(latitude, row.getBigDecimal("longitude"),
                        Optional.ofNullable(row.getString("label")), LocationSource.valueOf(row.getString("source"))));
        return new LocationProfile(row.getObject("owner_account_id", UUID.class), selection, row.getLong("revision"),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static WeatherReading mapReading(ResultSet row) throws SQLException {
        int weatherCode = row.getInt("weather_code");
        return new WeatherReading(
                row.getObject("observed_at", OffsetDateTime.class).toInstant(),
                row.getObject("retrieved_at", OffsetDateTime.class).toInstant(),
                row.getBigDecimal("temperature_celsius"),
                Optional.ofNullable(row.getBigDecimal("apparent_temperature_celsius")),
                Optional.ofNullable(row.getObject("relative_humidity_percent", Integer.class)),
                Optional.ofNullable(row.getBigDecimal("precipitation_millimetres")),
                Optional.ofNullable(row.getBigDecimal("wind_speed_kilometres_per_hour")),
                weatherCode,
                WeatherCondition.fromWmoCode(weatherCode)
                        .orElseThrow(() -> new IllegalStateException("Stored weather code is unknown")),
                row.getString("provider"),
                row.getString("basis"));
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
