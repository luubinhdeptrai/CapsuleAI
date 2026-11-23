package com.capsuleai.personalcontext.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * US-CTX-001 AC-03 (S4-CTX-04, OPEN-S4-21) — when weather evidence is current. The approved rule: evidence
 * exactly {@link #CURRENT_FOR} old is current; older evidence is refreshed before current use. Age is measured
 * from the provider's evidence time, never from the time it was fetched. A small tolerance absorbs clock skew
 * for evidence stamped slightly in the future; evidence further in the future is not plausible.
 */
public final class WeatherFreshness {

    /** Approved boundary (BR-013, FR-WEATHER-003): 30 minutes, inclusive. Not configurable. */
    public static final Duration CURRENT_FOR = Duration.ofMinutes(30);

    /** OPEN-S4-21: clock-skew tolerance for evidence stamped after the server's current time. */
    public static final Duration FUTURE_TOLERANCE = Duration.ofMinutes(5);

    private WeatherFreshness() {
    }

    /** True when evidence observed at {@code observedAt} may be used as current weather at {@code now}. */
    public static boolean isCurrentAt(Instant observedAt, Instant now) {
        Duration age = Duration.between(observedAt, now);
        if (age.isNegative()) {
            return age.negated().compareTo(FUTURE_TOLERANCE) <= 0;
        }
        return age.compareTo(CURRENT_FOR) <= 0;
    }

    /** True when evidence is stamped further in the future than the tolerance (an invalid reading). */
    public static boolean isImplausiblyFuture(Instant observedAt, Instant now) {
        return Duration.between(now, observedAt).compareTo(FUTURE_TOLERANCE) > 0;
    }
}