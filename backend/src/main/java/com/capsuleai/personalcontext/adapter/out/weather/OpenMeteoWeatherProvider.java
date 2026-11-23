package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * US-CTX-001 (S4-CTX-03/05; OPEN-S4-20/22/24) — Open-Meteo current weather behind the narrow
 * {@link WeatherProvider} port, under one elapsed acquisition deadline.
 *
 * <p>The deadline starts on entry and covers waiting for capacity, connection and TLS, headers, the body
 * (read through a size limit) and translation. The synchronous Spring {@link RestClient} call runs on a bounded
 * executor; the caller waits only for the remaining time, then cancels and reports DEADLINE_EXCEEDED. A capacity
 * permit is released only when the call really ends, so stuck calls never multiply threads or queues. Connect
 * and read timeouts equal to the deadline are backstops, not the deadline. There is no retry.
 *
 * <p>Privacy (AC-07): the request carries only the rounded point and fixed parameters. No URL, query or
 * coordinate is logged. Open-Meteo terms, attribution and eligibility for the deployment are not verified here.
 */
public final class OpenMeteoWeatherProvider implements WeatherProvider, AutoCloseable {

    static final String FORECAST_PATH = "/v1/forecast";
    static final String CURRENT_VARIABLES =
            "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m";

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherProvider.class);

    private final HttpClient httpClient;
    private final RestClient restClient;
    private final OpenMeteoResponseTranslator responseTranslator = new OpenMeteoResponseTranslator();
    private final Duration acquisitionDeadline;
    private final Semaphore acquisitionCapacity;
    private final ExecutorService acquisitionExecutor;
    private final int maximumResponseBytes;
    private final Clock clock;

    public OpenMeteoWeatherProvider(URI baseUrl, Duration acquisitionDeadline, int maximumConcurrentRequests,
                                    int maximumResponseBytes, Clock clock) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        this.acquisitionDeadline = Objects.requireNonNull(acquisitionDeadline, "acquisitionDeadline");
        if (acquisitionDeadline.isNegative() || acquisitionDeadline.isZero() || maximumConcurrentRequests < 1
                || maximumResponseBytes < 1) {
            throw new IllegalArgumentException("Weather acquisition limits must be positive");
        }
        this.maximumResponseBytes = maximumResponseBytes;
        this.clock = Objects.requireNonNull(clock, "clock");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(acquisitionDeadline)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(acquisitionDeadline);
        this.restClient = RestClient.builder().requestFactory(requestFactory).baseUrl(baseUrl.toString()).build();
        this.acquisitionCapacity = new Semaphore(maximumConcurrentRequests);
        this.acquisitionExecutor = Executors.newFixedThreadPool(maximumConcurrentRequests, task -> {
            Thread thread = new Thread(task, "weather-acquisition");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public WeatherAcquisition acquire(WeatherQuery query) {
        long deadline = System.nanoTime() + acquisitionDeadline.toNanos();
        WeatherAcquisition outcome = acquireBefore(query, deadline);
        if (outcome instanceof WeatherAcquisition.Unavailable unavailable) {
            log.info("Weather acquisition unavailable ({})", unavailable.reason());
        }
        return outcome;
    }

    private WeatherAcquisition acquireBefore(WeatherQuery query, long deadline) {
        try {
            if (!acquisitionCapacity.tryAcquire(remainingNanos(deadline), TimeUnit.NANOSECONDS)) {
                return unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
        // Exactly one side releases the permit: the task if it started, otherwise the caller that abandoned it.
        AtomicBoolean claimed = new AtomicBoolean();
        Future<WeatherAcquisition> call;
        try {
            call = acquisitionExecutor.submit(() -> {
                if (!claimed.compareAndSet(false, true)) {
                    return unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED);
                }
                try {
                    return fetch(query);
                } finally {
                    acquisitionCapacity.release();
                }
            });
        } catch (RejectedExecutionException rejected) {
            acquisitionCapacity.release();
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
        try {
            return call.get(remainingNanos(deadline), TimeUnit.NANOSECONDS);
        } catch (TimeoutException late) {
            abandon(call, claimed);
            return unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED);
        } catch (ExecutionException failed) {
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        } catch (InterruptedException interrupted) {
            abandon(call, claimed);
            Thread.currentThread().interrupt();
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
    }

    private void abandon(Future<WeatherAcquisition> call, AtomicBoolean claimed) {
        if (claimed.compareAndSet(false, true)) {
            acquisitionCapacity.release();
        }
        call.cancel(true);
    }

    private WeatherAcquisition fetch(WeatherQuery query) {
        try {
            WeatherAcquisition acquisition = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path(FORECAST_PATH)
                            .queryParam("latitude", query.latitude().toPlainString())
                            .queryParam("longitude", query.longitude().toPlainString())
                            .queryParam("current", CURRENT_VARIABLES)
                            .queryParam("timezone", "GMT")
                            .queryParam("timeformat", "unixtime")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
                        }
                        try (InputStream body = response.getBody()) {
                            byte[] content = body.readNBytes(maximumResponseBytes + 1);
                            if (content.length > maximumResponseBytes) {
                                return unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA);
                            }
                            return responseTranslator.translate(content, query, now());
                        }
                    });
            return acquisition != null ? acquisition : unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        } catch (RestClientException | IllegalArgumentException failure) {
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
    }

    /**
     * Stops the acquisition threads and the HTTP client (its selector thread and any open exchange) when the
     * application context closes.
     */
    @Override
    public void close() {
        acquisitionExecutor.shutdownNow();
        httpClient.shutdownNow();
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }

    private static long remainingNanos(long deadline) {
        return Math.max(0, deadline - System.nanoTime());
    }

    private static WeatherAcquisition unavailable(EnvironmentUnavailableReason reason) {
        return new WeatherAcquisition.Unavailable(reason);
    }
}
