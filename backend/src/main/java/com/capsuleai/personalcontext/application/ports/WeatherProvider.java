package com.capsuleai.personalcontext.application.ports;

/**
 * US-CTX-001 (S4-CTX-03/05) — the narrow weather port. An implementation answers within the configured single
 * acquisition deadline (at most 2 seconds), including any wait for capacity, and reports every failure as
 * {@link WeatherAcquisition.Unavailable} instead of throwing. It is never called inside a database transaction.
 */
@FunctionalInterface
public interface WeatherProvider {

    WeatherAcquisition acquire(WeatherQuery query);
}