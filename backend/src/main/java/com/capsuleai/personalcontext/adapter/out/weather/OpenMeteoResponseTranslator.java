package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * US-CTX-001 (S4-CTX-03, OPEN-S4-20) — translates an Open-Meteo forecast response (current block, unixtime,
 * default metric units) into {@link WeatherReading}, or reports it as INVALID_PROVIDER_DATA. Nothing is guessed:
 * a missing required value, an unexpected unit, an unknown weather code, an implausible value or a grid point
 * far from the requested location makes the whole response unusable. The raw payload is discarded.
 */
final class OpenMeteoResponseTranslator {

    /** Open-Meteo snaps to model grid cells; a cell further away than this is not the requested place. */
    static final BigDecimal MAXIMUM_GRID_DISTANCE_DEGREES = new BigDecimal("0.5");

    private static final List<String> NUMERIC_FIELDS = List.of("time", "temperature_2m", "apparent_temperature",
            "relative_humidity_2m", "precipitation", "weather_code", "wind_speed_10m");
    private static final BigDecimal LOWEST_TEMPERATURE = new BigDecimal("-100");
    private static final BigDecimal HIGHEST_TEMPERATURE = new BigDecimal("70");
    private static final BigDecimal HIGHEST_PRECIPITATION = new BigDecimal("1000");
    private static final BigDecimal HIGHEST_WIND_SPEED = new BigDecimal("500");

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    WeatherAcquisition translate(byte[] body, WeatherQuery query, Instant retrievedAt) {
        try {
            return translateOrReject(body, query, retrievedAt);
        } catch (JacksonException | ArithmeticException | DateTimeException | IllegalArgumentException unusable) {
            // Malformed JSON, or a number that cannot be represented (an int or long out of range, a value that
            // overflows a double, a time beyond the representable instants): unusable data, never a provider
            // failure and never an exception escaping the adapter.
            return invalid();
        }
    }

    private WeatherAcquisition translateOrReject(byte[] body, WeatherQuery query, Instant retrievedAt) {
        JsonNode root = jsonMapper.readTree(body);
        if (root == null || !root.isObject()) {
            return invalid();
        }
        JsonNode current = root.path("current");
        JsonNode units = root.path("current_units");
        if (!current.isObject() || !units.isObject()
                || !near(root.path("latitude"), query.latitude()) || !near(root.path("longitude"), query.longitude())) {
            return invalid();
        }
        for (String field : NUMERIC_FIELDS) {
            JsonNode value = current.path(field);
            if (!value.isMissingNode() && !value.isNull() && !value.isNumber()) {
                return invalid();
            }
        }
        if (!hasUnit(units, current, "temperature_2m", "°C") || !hasUnit(units, current, "apparent_temperature", "°C")
                || !hasUnit(units, current, "relative_humidity_2m", "%")
                || !hasUnit(units, current, "precipitation", "mm")
                || !hasUnit(units, current, "wind_speed_10m", "km/h")) {
            return invalid();
        }
        JsonNode time = current.path("time");
        JsonNode code = current.path("weather_code");
        Optional<BigDecimal> temperature = decimal(current.path("temperature_2m"));
        if (!time.isIntegralNumber() || !code.isIntegralNumber() || temperature.isEmpty()) {
            return invalid();
        }
        Optional<WeatherCondition> condition = WeatherCondition.fromWmoCode(code.intValue());
        Optional<BigDecimal> apparentTemperature = decimal(current.path("apparent_temperature"));
        Optional<Integer> humidity = integer(current.path("relative_humidity_2m"));
        Optional<BigDecimal> precipitation = decimal(current.path("precipitation"));
        Optional<BigDecimal> windSpeed = decimal(current.path("wind_speed_10m"));
        if (condition.isEmpty()
                || !within(temperature.get(), LOWEST_TEMPERATURE, HIGHEST_TEMPERATURE)
                || apparentTemperature.filter(value -> !within(value, LOWEST_TEMPERATURE, HIGHEST_TEMPERATURE)).isPresent()
                || humidity.filter(value -> value < 0 || value > 100).isPresent()
                || precipitation.filter(value -> !within(value, BigDecimal.ZERO, HIGHEST_PRECIPITATION)).isPresent()
                || windSpeed.filter(value -> !within(value, BigDecimal.ZERO, HIGHEST_WIND_SPEED)).isPresent()) {
            return invalid();
        }
        return new WeatherAcquisition.Acquired(new WeatherReading(Instant.ofEpochSecond(time.longValue()), retrievedAt,
                temperature.get(), apparentTemperature, humidity, precipitation, windSpeed, code.intValue(),
                condition.get(), WeatherReading.OPEN_METEO, WeatherReading.MODELLED));
    }

    /** A value present in {@code current} must declare the expected unit; an absent optional value needs none. */
    private static boolean hasUnit(JsonNode units, JsonNode current, String field, String expectedUnit) {
        JsonNode value = current.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return true;
        }
        JsonNode unit = units.path(field);
        return unit.isString() && unit.stringValue().equals(expectedUnit);
    }

    private static boolean near(JsonNode reported, BigDecimal requested) {
        return reported.isNumber()
                && reported.decimalValue().subtract(requested).abs().compareTo(MAXIMUM_GRID_DISTANCE_DEGREES) <= 0;
    }

    private static Optional<BigDecimal> decimal(JsonNode node) {
        return node.isNumber() ? Optional.of(node.decimalValue().setScale(2, RoundingMode.HALF_EVEN)) : Optional.empty();
    }

    private static Optional<Integer> integer(JsonNode node) {
        if (!node.isNumber()) {
            return Optional.empty();
        }
        return Optional.of(node.decimalValue().setScale(0, RoundingMode.HALF_EVEN).intValueExact());
    }

    private static boolean within(BigDecimal value, BigDecimal lowest, BigDecimal highest) {
        return value.compareTo(lowest) >= 0 && value.compareTo(highest) <= 0;
    }

    private static WeatherAcquisition invalid() {
        return new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA);
    }
}
