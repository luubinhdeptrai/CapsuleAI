package com.capsuleai.personalcontext.support;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * TEST-ONLY weather provider (US-CTX-001). Every integration context uses it instead of Open-Meteo: it records each
 * query, answers as told (PROVIDER_UNAVAILABLE by default) and can run an action just before answering, to make
 * races with location changes deterministic. Production never uses it.
 */
public final class ControllableWeatherProvider implements WeatherProvider {

    private final List<WeatherQuery> queries = new CopyOnWriteArrayList<>();
    private volatile Function<WeatherQuery, WeatherAcquisition> responder;
    private volatile Runnable beforeNextAcquisition;

    public ControllableWeatherProvider() {
        reset();
    }

    @Override
    public WeatherAcquisition acquire(WeatherQuery query) {
        queries.add(query);
        Runnable action = beforeNextAcquisition;
        beforeNextAcquisition = null;
        if (action != null) {
            action.run();
        }
        return responder.apply(query);
    }

    public void respondWith(WeatherAcquisition acquisition) {
        responder = query -> acquisition;
    }

    /** Answers with a typical reading whose provider evidence time is {@code observedAt}. */
    public void respondWithReadingObservedAt(Instant observedAt) {
        respondWith(new WeatherAcquisition.Acquired(reading(observedAt, observedAt.plusSeconds(1))));
    }

    public void beforeNextAcquisition(Runnable action) {
        beforeNextAcquisition = action;
    }

    public List<WeatherQuery> queries() {
        return List.copyOf(queries);
    }

    public void reset() {
        queries.clear();
        responder = query -> new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        beforeNextAcquisition = null;
    }

    public static WeatherReading reading(Instant observedAt, Instant retrievedAt) {
        return new WeatherReading(observedAt, retrievedAt, new BigDecimal("31.40"), Optional.of(new BigDecimal("35.10")),
                Optional.of(70), Optional.of(new BigDecimal("0.00")), Optional.of(new BigDecimal("12.30")), 2,
                WeatherCondition.PARTLY_CLOUDY, WeatherReading.OPEN_METEO, WeatherReading.MODELLED);
    }
}
