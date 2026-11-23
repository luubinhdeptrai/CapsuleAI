package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-CTX-001 (OPEN-S4-20): translation of the documented Open-Meteo current-weather shape. The sample follows the
 * provider's public documentation; a real response must still be checked (guide §14 M7).
 */
class OpenMeteoResponseTranslatorTest {

    static final String DOCUMENTED_RESPONSE = """
            {"latitude":10.75,"longitude":106.75,"generationtime_ms":0.05,"utc_offset_seconds":0,
             "timezone":"GMT","timezone_abbreviation":"GMT","elevation":9.0,
             "current_units":{"time":"unixtime","interval":"seconds","temperature_2m":"°C",
               "apparent_temperature":"°C","relative_humidity_2m":"%","precipitation":"mm",
               "weather_code":"wmo code","wind_speed_10m":"km/h"},
             "current":{"time":1763285400,"interval":900,"temperature_2m":31.4,"apparent_temperature":35.1,
               "relative_humidity_2m":70,"precipitation":0.0,"weather_code":2,"wind_speed_10m":12.3}}
            """;

    private static final WeatherQuery QUERY = new WeatherQuery(new BigDecimal("10.78"), new BigDecimal("106.70"));
    private static final Instant RETRIEVED_AT = Instant.parse("2026-11-16T09:31:00Z");

    private final OpenMeteoResponseTranslator translator = new OpenMeteoResponseTranslator();

    @Test
    void theDocumentedCurrentResponseIsTranslated() {
        WeatherAcquisition acquisition = translate(DOCUMENTED_RESPONSE);

        assertThat(acquisition).isInstanceOf(WeatherAcquisition.Acquired.class);
        WeatherReading reading = ((WeatherAcquisition.Acquired) acquisition).reading();
        assertThat(reading.observedAt()).isEqualTo(Instant.ofEpochSecond(1763285400));
        assertThat(reading.retrievedAt()).isEqualTo(RETRIEVED_AT);
        assertThat(reading.temperatureCelsius()).isEqualTo(new BigDecimal("31.40"));
        assertThat(reading.apparentTemperatureCelsius()).contains(new BigDecimal("35.10"));
        assertThat(reading.relativeHumidityPercent()).contains(70);
        assertThat(reading.precipitationMillimetres()).contains(new BigDecimal("0.00"));
        assertThat(reading.windSpeedKilometresPerHour()).contains(new BigDecimal("12.30"));
        assertThat(reading.weatherCode()).isEqualTo(2);
        assertThat(reading.condition()).isEqualTo(WeatherCondition.PARTLY_CLOUDY);
        assertThat(reading.basis()).isEqualTo("MODELLED");
        assertThat(reading.provider()).isEqualTo("OPEN_METEO");
    }

    @Test
    void malformedJsonOrAMissingCurrentBlockIsInvalid() {
        assertInvalid("{not json");
        assertInvalid("[]");
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"current\":{", "\"hourly\":{"));
    }

    @Test
    void anUnexpectedUnitOrANonNumericValueIsInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":\"°C\"", "\"temperature_2m\":\"°F\""));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":31.4", "\"temperature_2m\":\"31.4\""));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"time\":1763285400", "\"time\":\"2026-11-16T09:30\""));
    }

    @Test
    void aGridPointFarFromTheRequestedLocationIsInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"latitude\":10.75", "\"latitude\":12.0"));
    }

    @Test
    void anUnknownWeatherCodeOrAnImplausibleValueIsInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"weather_code\":2", "\"weather_code\":42"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"relative_humidity_2m\":70", "\"relative_humidity_2m\":140"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":31.4", "\"temperature_2m\":3140"));
    }

    /**
     * Numbers that cannot be represented are invalid provider data, never an exception or a provider failure:
     * a code beyond int, humidity beyond int, a value that overflows a double, a time beyond long or beyond the
     * representable instants, and a non-finite grid coordinate.
     */
    @Test
    void unrepresentableNumbersAreInvalidProviderData() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"weather_code\":2", "\"weather_code\":4294967298"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"relative_humidity_2m\":70", "\"relative_humidity_2m\":1e10"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":31.4", "\"temperature_2m\":1e400"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"time\":1763285400", "\"time\":99999999999999999999"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"time\":1763285400", "\"time\":9223372036854775807"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"latitude\":10.75", "\"latitude\":1e400"));
    }

    /** Negative amounts are impossible; units are never converted (km/h is required, not assumed). */
    @Test
    void negativeAmountsAndOtherUnitsAreInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"precipitation\":0.0", "\"precipitation\":-0.1"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"wind_speed_10m\":12.3", "\"wind_speed_10m\":-1"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"wind_speed_10m\":\"km/h\"", "\"wind_speed_10m\":\"m/s\""));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"precipitation\":\"mm\"", "\"precipitation\":\"inch\""));
    }

    /** Only time, temperature and weather code are required; absent or null optional values stay absent. */
    @Test
    void absentOrNullOptionalValuesStayAbsentAndARequiredOneIsNeeded() {
        String minimal = """
                {"latitude":10.75,"longitude":106.75,"current_units":{"temperature_2m":"°C"},
                 "current":{"time":1763285400,"temperature_2m":31.4,"weather_code":0,"precipitation":null}}
                """;

        WeatherReading reading = ((WeatherAcquisition.Acquired) translate(minimal)).reading();

        assertThat(reading.apparentTemperatureCelsius()).isEmpty();
        assertThat(reading.relativeHumidityPercent()).isEmpty();
        assertThat(reading.precipitationMillimetres()).isEmpty();
        assertThat(reading.windSpeedKilometresPerHour()).isEmpty();
        assertThat(reading.condition()).isEqualTo(WeatherCondition.CLEAR);
        assertInvalid(minimal.replace("\"temperature_2m\":31.4,", ""));
        assertInvalid(minimal.replace("\"weather_code\":0,", ""));
    }

    private WeatherAcquisition translate(String json) {
        return translator.translate(json.getBytes(StandardCharsets.UTF_8), QUERY, RETRIEVED_AT);
    }

    private void assertInvalid(String json) {
        assertThat(translate(json)).isEqualTo(
                new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA));
    }
}
