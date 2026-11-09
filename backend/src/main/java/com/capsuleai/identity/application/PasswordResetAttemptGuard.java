package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Applies the configured password-reset attempt limits (FR-AUTH-019, OPEN-S2-04).
 *
 * <p>Fixed windows: an attempt at {@code now} belongs to the window starting at
 * {@code now} rounded down to a multiple of the window length since the epoch, so every
 * instance of the application agrees on the window without coordination. An attempt is
 * counted before it is judged, so a client that keeps trying while blocked stays blocked
 * for the rest of the window.
 */
public final class PasswordResetAttemptGuard {

    private final PasswordResetAttemptLimiter limiter;
    private final Map<PasswordResetAttemptScope, Integer> maximumAttempts;
    private final Duration window;
    private final Clock clock;

    public PasswordResetAttemptGuard(PasswordResetAttemptLimiter limiter,
                                     int maximumRequestsPerEmail,
                                     int maximumRequestsPerClient,
                                     int maximumVerificationsPerClient,
                                     Duration window,
                                     Clock clock) {
        this.limiter = Objects.requireNonNull(limiter, "limiter");
        this.window = Objects.requireNonNull(window, "window");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (window.toSeconds() < 1) {
            throw new IllegalArgumentException("The attempt window must be at least one second");
        }
        this.maximumAttempts = new EnumMap<>(PasswordResetAttemptScope.class);
        maximumAttempts.put(PasswordResetAttemptScope.REQUEST_PER_EMAIL, positive(maximumRequestsPerEmail));
        maximumAttempts.put(PasswordResetAttemptScope.REQUEST_PER_CLIENT, positive(maximumRequestsPerClient));
        maximumAttempts.put(PasswordResetAttemptScope.VERIFICATION_PER_CLIENT, positive(maximumVerificationsPerClient));
    }

    /**
     * Counts one attempt for {@code subject}.
     *
     * @throws PasswordResetRateLimitedException if the subject exceeded its limit in the current window
     */
    public void admit(PasswordResetAttemptScope scope, String subject) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(subject, "subject");
        Instant now = clock.instant();
        Instant windowStart = windowStartOf(now);
        int attempts = limiter.recordAttempt(scope, subject, windowStart);
        if (attempts > maximumAttempts.get(scope)) {
            throw new PasswordResetRateLimitedException(Duration.between(now, windowStart.plus(window)));
        }
    }

    /** The longest window in use; counters older than this are no longer needed. */
    public Duration window() {
        return window;
    }

    Instant windowStartOf(Instant now) {
        long windowSeconds = window.toSeconds();
        long start = Math.floorDiv(now.getEpochSecond(), windowSeconds) * windowSeconds;
        return Instant.ofEpochSecond(start);
    }

    private static int positive(int maximum) {
        if (maximum < 1) {
            throw new IllegalArgumentException("An attempt limit must allow at least one attempt");
        }
        return maximum;
    }
}