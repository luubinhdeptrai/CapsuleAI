package com.capsuleai.personalcontext.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * US-CTX-001 (S4-CTX-03/04) — translated weather evidence. {@code observedAt} is the provider's evidence time,
 * the time freshness is measured from; {@code retrievedAt} is when this server received it and never makes old
 * evidence current. {@code basis} tells modelled values from observations: Open-Meteo values are MODELLED.
 */
public record WeatherReading(
        Instant observedAt,
        Instant retrievedAt,
        BigDecimal temperatureCelsius,
        Optional<BigDecimal> apparentTemperatureCelsius,
        Optional<Integer> relativeHumidityPercent,
        Optional<BigDecimal> precipitationMillimetres,
        Optional<BigDecimal> windSpeedKilometresPerHour,
        int weatherCode,
        WeatherCondition condition,
        String provider,
        String basis) {

    public static final String OPEN_METEO = "OPEN_METEO";
    public static final String MODELLED = "MODELLED";

    public WeatherReading {
        Objects.requireNonNull(observedAt, "observedAt");
        Objects.requireNonNull(retrievedAt, "retrievedAt");
        Objects.requireNonNull(temperatureCelsius, "temperatureCelsius");
        Objects.requireNonNull(apparentTemperatureCelsius, "apparentTemperatureCelsius");
        Objects.requireNonNull(relativeHumidityPercent, "relativeHumidityPercent");
        Objects.requireNonNull(precipitationMillimetres, "precipitationMillimetres");
        Objects.requireNonNull(windSpeedKilometresPerHour, "windSpeedKilometresPerHour");
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(basis, "basis");
        if (WeatherCondition.fromWmoCode(weatherCode).orElse(null) != condition) {
            throw new IllegalArgumentException("The condition must be the translation of the weather code");
        }
        relativeHumidityPercent.ifPresent(humidity -> {
            if (humidity < 0 || humidity > 100) {
                throw new IllegalArgumentException("Relative humidity is a percentage");
            }
        });
    }
}