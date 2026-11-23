package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.EnvironmentalContext;
import com.capsuleai.personalcontext.application.LocationContextView;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;

/**
 * US-CTX-001 AC-02 — the accepted selection and, separately, its environmental context. Every field is always
 * present: {@code revision} is null while nothing was ever selected, {@code selection} is null when none is
 * selected, and {@code environment.weather} is null unless the status is CURRENT. Weather carries both the
 * provider evidence time ({@code observedAt}) and the acquisition time ({@code retrievedAt}).
 */
public record LocationContextResponse(String revision, SelectionResponse selection, EnvironmentResponse environment) {

    public record SelectionResponse(BigDecimal latitude, BigDecimal longitude, String label, String source) {
    }

    /** {@code basisRevision}: the location revision the status applies to; null for NO_SELECTION. */
    public record EnvironmentResponse(String status, String basisRevision, String unavailableReason,
                                      WeatherResponse weather) {
    }

    public record WeatherResponse(String condition, int weatherCode, BigDecimal temperatureCelsius,
                                  BigDecimal apparentTemperatureCelsius, Integer relativeHumidityPercent,
                                  BigDecimal precipitationMillimetres, BigDecimal windSpeedKilometresPerHour,
                                  String observedAt, String retrievedAt, String basis, String provider,
                                  String attribution) {
    }

    static LocationContextResponse from(LocationContextView view, String weatherAttribution) {
        String revision = view.profile().map(profile -> Long.toString(profile.revision())).orElse(null);
        SelectionResponse selection = view.profile().flatMap(LocationProfile::selection)
                .map(LocationContextResponse::selection).orElse(null);
        return new LocationContextResponse(revision, selection, environment(view.environment(), weatherAttribution));
    }

    private static SelectionResponse selection(LocationSelection selection) {
        return new SelectionResponse(selection.latitude(), selection.longitude(), selection.label().orElse(null),
                selection.source().name());
    }

    private static EnvironmentResponse environment(EnvironmentalContext environment, String weatherAttribution) {
        return switch (environment) {
            case EnvironmentalContext.NoSelection none -> new EnvironmentResponse("NO_SELECTION", null, null, null);
            case EnvironmentalContext.Current current -> new EnvironmentResponse("CURRENT",
                    Long.toString(current.locationRevision()), null, weather(current.reading(), weatherAttribution));
            case EnvironmentalContext.Unavailable unavailable -> new EnvironmentResponse("UNAVAILABLE",
                    Long.toString(unavailable.locationRevision()), unavailable.reason().name(), null);
        };
    }

    private static WeatherResponse weather(WeatherReading reading, String weatherAttribution) {
        return new WeatherResponse(reading.condition().name(), reading.weatherCode(), reading.temperatureCelsius(),
                reading.apparentTemperatureCelsius().orElse(null), reading.relativeHumidityPercent().orElse(null),
                reading.precipitationMillimetres().orElse(null), reading.windSpeedKilometresPerHour().orElse(null),
                reading.observedAt().toString(), reading.retrievedAt().toString(), reading.basis(), reading.provider(),
                weatherAttribution);
    }
}
