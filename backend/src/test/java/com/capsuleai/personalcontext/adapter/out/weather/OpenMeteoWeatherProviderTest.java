package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-CTX-001 (S4-CTX-05, OPEN-S4-22) — the real adapter against an in-process HTTP server with controlled
 * delays: one elapsed deadline including capacity wait, slow headers and slow bodies; explicit outcomes for
 * errors, oversized bodies and unreachable providers; only the location is sent. A 500 ms deadline keeps the
 * suite fast; production uses PT2S. The tolerance is a test allowance for scheduling, not a new limit.
 */
class OpenMeteoWeatherProviderTest {

    private static final Duration DEADLINE = Duration.ofMillis(500);
    private static final Duration TOLERANCE = Duration.ofMillis(500);
    private static final WeatherQuery QUERY = new WeatherQuery(new BigDecimal("10.78"), new BigDecimal("106.70"));

    private final List<String> receivedQueries = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private ExecutorService serverThreads;
    private volatile HttpHandler behaviour;
    private OpenMeteoWeatherProvider weatherProvider;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            receivedQueries.add(exchange.getRequestURI().getRawQuery());
            behaviour.handle(exchange);
        });
        serverThreads = Executors.newCachedThreadPool();
        server.setExecutor(serverThreads);
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (weatherProvider != null) {
            weatherProvider.close();
        }
        server.stop(0);
        serverThreads.shutdownNow();
    }

    @Test
    void aValidResponseIsAcquiredAndOnlyTheLocationIsSent() {
        behaviour = exchange -> respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);

        WeatherAcquisition acquisition = provider(2).acquire(QUERY);

        assertThat(acquisition).isInstanceOf(WeatherAcquisition.Acquired.class);
        String query = receivedQueries.getFirst();
        assertThat(Arrays.stream(query.split("&")).map(parameter -> parameter.substring(0, parameter.indexOf('='))))
                .containsExactlyInAnyOrder("latitude", "longitude", "current", "timezone", "timeformat");
        assertThat(query).contains("latitude=10.78", "longitude=106.70", "timezone=GMT", "timeformat=unixtime");
    }

    @Test
    void slowResponseHeadersExceedTheDeadline() {
        behaviour = exchange -> {
            pause(3000);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };

        assertDeadlineExceededInTime(provider(2));
    }

    @Test
    void aSlowBodyExceedsTheDeadline() {
        behaviour = exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write("{\"latitude\":10.75,".getBytes(StandardCharsets.UTF_8));
                body.flush();
                pause(3000);
                body.write("\"longitude\":106.75}".getBytes(StandardCharsets.UTF_8));
            }
        };

        assertDeadlineExceededInTime(provider(2));
    }

    @Test
    void aServerErrorIsProviderUnavailable() {
        behaviour = exchange -> respond(exchange, 500, "{\"error\":true}");

        assertThat(provider(2).acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
    }

    @Test
    void anOversizedBodyIsInvalidProviderData() {
        behaviour = exchange -> respond(exchange, 200, "x".repeat(70_000));

        assertThat(provider(2).acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA));
    }

    @Test
    void waitingForCapacityCountsAgainstTheSameDeadline() throws Exception {
        behaviour = exchange -> {
            pause(3000);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };
        OpenMeteoWeatherProvider singleCapacity = provider(1);

        CompletableFuture<Duration> first = CompletableFuture.supplyAsync(() -> timedDeadlineExceeded(singleCapacity));
        CompletableFuture<Duration> second = CompletableFuture.supplyAsync(() -> timedDeadlineExceeded(singleCapacity));

        assertThat(first.get()).isLessThan(DEADLINE.plus(TOLERANCE));
        assertThat(second.get()).isLessThan(DEADLINE.plus(TOLERANCE));
    }

    @Test
    void anUnreachableProviderIsUnavailableWithinTheDeadline() throws IOException {
        int closedPort;
        try (ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = probe.getLocalPort();
        }
        OpenMeteoWeatherProvider unreachable = provider(URI.create("http://127.0.0.1:" + closedPort), 2);

        long start = System.nanoTime();
        WeatherAcquisition acquisition = unreachable.acquire(QUERY);

        assertThat(acquisition).isInstanceOf(WeatherAcquisition.Unavailable.class);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(DEADLINE.plus(TOLERANCE));
    }

    /**
     * A call abandoned at the deadline keeps its capacity permit only until the abandoned work really ends; the
     * permit is never leaked, so a later acquisition gets through once the late call has stopped.
     */
    @Test
    void anAbandonedCallReleasesItsCapacityOnceItEnds() throws Exception {
        behaviour = exchange -> {
            pause(1500);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };
        OpenMeteoWeatherProvider singleCapacity = provider(1);
        assertDeadlineExceededInTime(singleCapacity);

        behaviour = exchange -> respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        long giveUp = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        WeatherAcquisition later = singleCapacity.acquire(QUERY);
        while (!(later instanceof WeatherAcquisition.Acquired) && System.nanoTime() < giveUp) {
            later = singleCapacity.acquire(QUERY);
        }

        assertThat(later).isInstanceOf(WeatherAcquisition.Acquired.class);
    }

    /**
     * Many concurrent callers against a provider that never answers in time: every caller gets DEADLINE_EXCEEDED
     * within the deadline, and the adapter never runs more acquisition threads than its capacity.
     */
    @Test
    void concurrentTimeoutsNeverGrowAcquisitionThreadsBeyondTheCapacity() throws Exception {
        behaviour = exchange -> {
            pause(3000);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };
        Set<Long> earlierThreads = acquisitionThreadIds();
        OpenMeteoWeatherProvider twoAtATime = provider(2);
        ExecutorService callers = Executors.newFixedThreadPool(12);
        try {
            List<CompletableFuture<Duration>> calls = new ArrayList<>();
            for (int caller = 0; caller < 12; caller++) {
                calls.add(CompletableFuture.supplyAsync(() -> timedDeadlineExceeded(twoAtATime), callers));
            }
            for (CompletableFuture<Duration> call : calls) {
                assertThat(call.get()).isLessThan(DEADLINE.plus(TOLERANCE));
            }
        } finally {
            callers.shutdownNow();
        }

        Set<Long> newThreads = acquisitionThreadIds();
        newThreads.removeAll(earlierThreads);
        assertThat(newThreads).hasSizeLessThanOrEqualTo(2);
    }

    /** OPEN-S4-24: a redirect is never followed, so the request cannot leave the configured host. */
    @Test
    void aRedirectIsNotFollowed() {
        behaviour = exchange -> {
            exchange.getResponseHeaders().add("Location", "http://127.0.0.1:" + server.getAddress().getPort()
                    + "/elsewhere");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        };

        assertThat(provider(2).acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
        assertThat(receivedQueries).as("only the original request reached the server").hasSize(1);
    }

    /** AC-07, OPEN-S4-24: the only log line names the outcome category, never the point or the URL. */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void aFailureIsLoggedWithoutTheLocationOrTheUrl(CapturedOutput output) {
        behaviour = exchange -> respond(exchange, 503, "{\"error\":true}");

        provider(2).acquire(QUERY);

        assertThat(output.getAll())
                .contains("Weather acquisition unavailable (PROVIDER_UNAVAILABLE)")
                .doesNotContain("10.78")
                .doesNotContain("106.70")
                .doesNotContain("latitude")
                .doesNotContain("127.0.0.1:" + server.getAddress().getPort());
    }

    /**
     * Creating the adapter contacts nobody; closing it (the application context does on shutdown) stops its
     * acquisition threads and its HTTP client, and an acquisition after closing is unavailable, never a hang.
     */
    @Test
    void creatingOpensNoConnectionAndClosingStopsTheAdaptersThreads() {
        behaviour = exchange -> respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        Set<Long> earlierThreads = adapterThreadIds();
        OpenMeteoWeatherProvider adapter = provider(2);
        assertThat(receivedQueries).as("construction sends no request").isEmpty();
        assertThat(adapter.acquire(QUERY)).isInstanceOf(WeatherAcquisition.Acquired.class);

        adapter.close();

        long giveUp = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        Set<Long> remaining = adapterThreadIds();
        remaining.removeAll(earlierThreads);
        while (!remaining.isEmpty() && System.nanoTime() < giveUp) {
            pause(20);
            remaining = adapterThreadIds();
            remaining.removeAll(earlierThreads);
        }
        assertThat(remaining).as("acquisition and HTTP client threads after close").isEmpty();
        assertThat(adapter.acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
    }

    /** Threads owned by an adapter: its acquisition pool and the JDK HTTP client's selector and workers. */
    private static Set<Long> adapterThreadIds() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(thread -> thread.getName().equals("weather-acquisition") || thread.getName().startsWith("HttpClient-"))
                .map(Thread::threadId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Set<Long> acquisitionThreadIds() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().equals("weather-acquisition") && thread.isAlive())
                .map(Thread::threadId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private OpenMeteoWeatherProvider provider(int maximumConcurrentRequests) {
        return provider(URI.create("http://127.0.0.1:" + server.getAddress().getPort()), maximumConcurrentRequests);
    }

    private OpenMeteoWeatherProvider provider(URI baseUrl, int maximumConcurrentRequests) {
        weatherProvider = new OpenMeteoWeatherProvider(baseUrl, DEADLINE, maximumConcurrentRequests, 65_536,
                Clock.systemUTC());
        return weatherProvider;
    }

    private void assertDeadlineExceededInTime(OpenMeteoWeatherProvider provider) {
        assertThat(timedDeadlineExceeded(provider)).isLessThan(DEADLINE.plus(TOLERANCE));
    }

    private static Duration timedDeadlineExceeded(OpenMeteoWeatherProvider provider) {
        long start = System.nanoTime();
        WeatherAcquisition acquisition = provider.acquire(QUERY);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertThat(acquisition)
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED));
        return elapsed;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
