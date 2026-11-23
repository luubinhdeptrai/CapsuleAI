package com.capsuleai.personalcontext.configuration;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-CTX-001 (OPEN-S4-22/24): startup validation of the weather configuration. */
class PersonalContextWeatherPropertiesTest {

    @Test
    void theShippedDefaultsAreAccepted() {
        PersonalContextWeatherProperties properties = properties(URI.create("https://api.open-meteo.com"),
                Duration.ofSeconds(2));

        assertThat(properties.acquisitionDeadline()).isEqualTo(PersonalContextWeatherProperties.MAXIMUM_ACQUISITION_DEADLINE);
        assertThat(properties.toString()).contains("host=api.open-meteo.com").doesNotContain("https://");
    }

    @Test
    void aDeadlineAboveTheApprovedTwoSecondsOrNotPositiveIsRejected() {
        URI provider = URI.create("https://api.open-meteo.com");

        assertThatThrownBy(() -> properties(provider, Duration.ofMillis(2001))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(provider, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThat(properties(provider, Duration.ofMillis(500)).acquisitionDeadline()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    void onlyAnHttpsOriginOrALoopbackTestHostIsAccepted() {
        Duration deadline = Duration.ofSeconds(2);

        assertThatThrownBy(() -> properties(URI.create("http://api.open-meteo.com"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("https://api.open-meteo.com?apikey=x"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("https://user:secret@api.open-meteo.com"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(properties(URI.create("http://127.0.0.1:18080"), deadline).baseUrl().getPort()).isEqualTo(18080);
    }

    @Test
    void theLimitsAndTheAttributionAreValidated() {
        URI provider = URI.create("https://api.open-meteo.com");
        Duration deadline = Duration.ofSeconds(2);

        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, Duration.ofSeconds(-1), 8, 65_536,
                "Weather data by Open-Meteo.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, deadline, 0, 65_536,
                "Weather data by Open-Meteo.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, deadline, 8, 1023,
                "Weather data by Open-Meteo.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, deadline, 8, 65_536, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("https://api.open-meteo.com#fragment"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("ftp://api.open-meteo.com"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static PersonalContextWeatherProperties properties(URI baseUrl, Duration acquisitionDeadline) {
        return new PersonalContextWeatherProperties(true, baseUrl, acquisitionDeadline, 8, 65_536,
                "Weather data by Open-Meteo.com");
    }
}
