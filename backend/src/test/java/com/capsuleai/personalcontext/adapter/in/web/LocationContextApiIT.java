package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-CTX-001 through the real filter chain and PostgreSQL, with the weather provider replaced by
 * ControllableWeatherProvider: selection lifecycle, rounding, evidence times, inclusive freshness, truthful
 * fallback, clearing, conflicts, validation and access control (AC-01–AC-03, AC-06, AC-07).
 */
class LocationContextApiIT extends PersonalContextIntegrationTestSupport {

    @Test
    void anAccountWithoutASelectionHasNoEnvironmentAndCausesNoProviderCall() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-none@example.com");

        viewLocation(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.selection").value(nullValue()))
                .andExpect(jsonPath("$.environment.status").value("NO_SELECTION"))
                .andExpect(jsonPath("$.environment.basisRevision").value(nullValue()))
                .andExpect(jsonPath("$.environment.weather").value(nullValue()));
        assertThat(weatherProvider.queries()).isEmpty();
    }

    @Test
    void aManualCitySelectionReportsCurrentWeatherWithBothEvidenceTimes() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-manual@example.com");
        Instant observedAt = clock.instant().minus(Duration.ofMinutes(10));
        weatherProvider.respondWithReadingObservedAt(observedAt);

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.selection.latitude").value(10.78))
                .andExpect(jsonPath("$.selection.longitude").value(106.7))
                .andExpect(jsonPath("$.selection.label").value("Hồ Chí Minh"))
                .andExpect(jsonPath("$.selection.source").value("MANUAL"))
                .andExpect(jsonPath("$.environment.status").value("CURRENT"))
                .andExpect(jsonPath("$.environment.basisRevision").value("1"))
                .andExpect(jsonPath("$.environment.weather.observedAt").value(observedAt.toString()))
                .andExpect(jsonPath("$.environment.weather.retrievedAt").isString())
                .andExpect(jsonPath("$.environment.weather.basis").value("MODELLED"))
                .andExpect(jsonPath("$.environment.weather.attribution").value("Weather data by Open-Meteo.com"));
    }

    @Test
    void deviceCoordinatesAreRoundedBeforeStorageAndBeforeTheProvider() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-device@example.com");

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 21.028511, "longitude", 105.804817, "source", "DEVICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selection.label").value(nullValue()));

        assertThat(weatherProvider.queries())
                .containsExactly(new WeatherQuery(new BigDecimal("21.03"), new BigDecimal("105.80")));
        assertThat(jdbc.sql("SELECT latitude::text || ',' || longitude::text FROM personal_context.personal_context_location "
                        + "WHERE owner_account_id = :owner")
                .param("owner", tokens.accountId()).query(String.class).single()).isEqualTo("21.03,105.80");
    }

    @Test
    void aProviderFailureKeepsTheSelectionAndDisclosesUnavailableWeather() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-failure@example.com");

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 16.4637, "longitude", 107.5909, "label", "Huế", "source", "MANUAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selection.label").value("Huế"))
                .andExpect(jsonPath("$.environment.status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.environment.unavailableReason").value("PROVIDER_UNAVAILABLE"))
                .andExpect(jsonPath("$.environment.weather").value(nullValue()));
        viewLocation(tokens.accessToken()).andExpect(jsonPath("$.selection.label").value("Huế"));
    }

    @Test
    void evidenceExactlyThirtyMinutesOldIsReusedAndOlderEvidenceIsRefreshed() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-freshness@example.com");
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")))
                .andExpect(status().isOk());

        clock.advance(Duration.ofMinutes(30));
        // Access tokens live 15 minutes (Identity), so the user signs in again after the clock has moved.
        String laterAccessToken = logIn("location-freshness@example.com", PASSWORD).accessToken();
        viewLocation(laterAccessToken).andExpect(jsonPath("$.environment.status").value("CURRENT"));
        assertThat(weatherProvider.queries()).hasSize(1);

        clock.advance(Duration.ofSeconds(1));
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        viewLocation(laterAccessToken)
                .andExpect(jsonPath("$.environment.status").value("CURRENT"))
                .andExpect(jsonPath("$.environment.weather.observedAt").value(clock.instant().toString()));
        assertThat(weatherProvider.queries()).hasSize(2);
    }

    @Test
    void clearingRemovesTheSelectionAndItsEvidence() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-clear@example.com");
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")));

        reviseLocation(tokens.accessToken(), json("expectedRevision", "1", "selection", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.selection").value(nullValue()))
                .andExpect(jsonPath("$.environment.status").value("NO_SELECTION"));

        assertThat(countRows("personal_context.personal_context_weather_evidence")).isZero();
        assertThat(weatherProvider.queries()).hasSize(1);
    }

    @Test
    void aStaleRevisionIsAConflictAndInvalidInputIsRejectedWithoutEcho() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-invalid@example.com");
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")));

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 21.0285, "longitude", 105.8048, "label", "Hà Nội", "source", "MANUAL")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        reviseLocation(tokens.accessToken(), json("expectedRevision", "1", "selection",
                Map.of("latitude", "north", "longitude", 200, "source", "MANUAL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.latitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.longitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.label')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3))
                .andExpect(jsonPath("$.detail").value(not(containsString("north"))));
        viewLocation(tokens.accessToken()).andExpect(jsonPath("$.selection.label").value("Hồ Chí Minh"));
    }

    @Test
    void noOtherAccountOrSessionCanReadOrChangeIt() throws Exception {
        IssuedTokens owner = registerAndLogIn("location-owner@example.com");
        IssuedTokens other = registerAndLogIn("location-other@example.com");
        reviseLocation(owner.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")));

        viewLocation(other.accessToken()).andExpect(jsonPath("$.environment.status").value("NO_SELECTION"));
        mockMvc.perform(get(LOCATION)).andExpect(status().isUnauthorized());
    }

    /**
     * A coordinate that overflows a double, a selection that is not an object, a source in another case and a label
     * of the wrong kind or length are field errors (400), never a server error; nothing is stored or requested.
     */
    @Test
    void unusableValuesAreFieldErrorsNeverServerErrors() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-unusable@example.com");

        reviseLocationRaw(tokens.accessToken(), """
                {"expectedRevision":null,"selection":{"latitude":1e400,"longitude":-1e400,"source":"DEVICE"}}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.latitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.longitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection", "Hà Nội"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("selection"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("UNSUPPORTED_VALUE"));
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", 7, "source", "manual")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.source')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.label')].code", hasItem("INVALID_FORMAT")));
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "x".repeat(81), "source", "MANUAL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("selection.label"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_FORMAT"));

        assertThat(countRows("personal_context.personal_context_location")).isZero();
        assertThat(weatherProvider.queries()).isEmpty();
    }

    /** M6a automated: neither the submitted coordinates nor the label reach the application log. */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void coordinatesAndLabelsNeverReachTheLog(CapturedOutput output) throws Exception {
        LoggerFactory.getLogger(LocationContextApiIT.class).info("location log capture is active");
        IssuedTokens tokens = registerAndLogIn("location-log@example.com");

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 16.4637, "longitude", 107.5909, "label", "Huế Citadel", "source", "MANUAL")))
                .andExpect(status().isOk());
        reviseLocation(tokens.accessToken(), json("expectedRevision", "1", "selection",
                Map.of("latitude", 95.1234, "longitude", 107.5909, "label", "Huế Citadel", "source", "MANUAL")))
                .andExpect(status().isBadRequest());
        viewLocation(tokens.accessToken()).andExpect(status().isOk());

        assertThat(output.getAll())
                .as("the capture must see this class's own log line, or the check below proves nothing")
                .contains("location log capture is active")
                .doesNotContain("16.46").doesNotContain("107.59").doesNotContain("16.4637").doesNotContain("95.12")
                .doesNotContain("Huế Citadel");
    }

    private ResultActions reviseLocationRaw(String accessToken, String body) throws Exception {
        return mockMvc.perform(patch(LOCATION)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
