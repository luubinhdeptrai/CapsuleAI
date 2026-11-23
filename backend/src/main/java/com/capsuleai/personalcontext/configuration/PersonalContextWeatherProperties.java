package com.capsuleai.personalcontext.configuration;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * US-CTX-001 configuration {@code capsuleai.personal-context.weather.*} (OPEN-S4-22/24/33). Validated at startup:
 * the acquisition deadline implements the approved 2-second boundary and may not exceed it; the provider is
 * reached only over HTTPS (plain HTTP is accepted for loopback hosts, which only tests use).
 */
@ConfigurationProperties("capsuleai.personal-context.weather")
public record PersonalContextWeatherProperties(
        boolean enabled,
        URI baseUrl,
        Duration acquisitionDeadline,
        int maximumConcurrentRequests,
        int maximumResponseBytes,
        String attribution) {

    /** Approved (FR-WEATHER-004, AC-03): an external weather request may delay an assessment by at most 2 seconds. */
    public static final Duration MAXIMUM_ACQUISITION_DEADLINE = Duration.ofSeconds(2);

    private static final Set<String> LOOPBACK_HOSTS = Set.of("127.0.0.1", "localhost");

    public PersonalContextWeatherProperties {
        Objects.requireNonNull(baseUrl, "capsuleai.personal-context.weather.base-url");
        Objects.requireNonNull(acquisitionDeadline, "capsuleai.personal-context.weather.acquisition-deadline");
        Objects.requireNonNull(attribution, "capsuleai.personal-context.weather.attribution");
        if (acquisitionDeadline.isNegative() || acquisitionDeadline.isZero()
                || acquisitionDeadline.compareTo(MAXIMUM_ACQUISITION_DEADLINE) > 0) {
            throw new IllegalArgumentException("The weather acquisition deadline must be positive and at most PT2S");
        }
        if (maximumConcurrentRequests < 1 || maximumResponseBytes < 1024) {
            throw new IllegalArgumentException("Weather concurrency must be at least 1 and the response limit at least 1024 bytes");
        }
        boolean secure = "https".equals(baseUrl.getScheme());
        boolean loopback = "http".equals(baseUrl.getScheme()) && LOOPBACK_HOSTS.contains(baseUrl.getHost());
        if (!secure && !loopback) {
            throw new IllegalArgumentException("The weather base URL must use https (http only for a loopback test host)");
        }
        if (baseUrl.getQuery() != null || baseUrl.getFragment() != null || baseUrl.getUserInfo() != null) {
            throw new IllegalArgumentException("The weather base URL must not carry a query, fragment or credentials");
        }
        if (attribution.isBlank()) {
            throw new IllegalArgumentException("The weather attribution must not be blank");
        }
    }

    /** Never print more than needed, even when debugging configuration. */
    @Override
    public String toString() {
        return "PersonalContextWeatherProperties[enabled=" + enabled + ", host=" + baseUrl.getHost()
                + ", acquisitionDeadline=" + acquisitionDeadline + ", maximumConcurrentRequests="
                + maximumConcurrentRequests + ", maximumResponseBytes=" + maximumResponseBytes + "]";
    }
}
