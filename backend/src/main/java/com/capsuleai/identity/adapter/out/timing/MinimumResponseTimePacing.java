package com.capsuleai.identity.adapter.out.timing;

import com.capsuleai.identity.application.ports.RecoveryResponsePacing;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

/**
 * Holds a successful Forgot Password request until a minimum duration has passed since it
 * started (OPEN-S2-05). Uses the monotonic {@link System#nanoTime()} clock, not the
 * application's business {@code Clock}, because it measures real elapsed time. A minimum of
 * zero disables pacing.
 */
public final class MinimumResponseTimePacing implements RecoveryResponsePacing {

    private final long minimumNanos;

    public MinimumResponseTimePacing(Duration minimum) {
        Objects.requireNonNull(minimum, "minimum");
        if (minimum.isNegative()) {
            throw new IllegalArgumentException("The minimum response time cannot be negative");
        }
        this.minimumNanos = minimum.toNanos();
    }

    @Override
    public Pace start() {
        long started = System.nanoTime();
        return () -> {
            long remaining;
            while ((remaining = minimumNanos - (System.nanoTime() - started)) > 0) {
                LockSupport.parkNanos(remaining);
            }
        };
    }
}