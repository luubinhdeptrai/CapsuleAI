package com.capsuleai.identity.application;

import java.time.Duration;
import java.util.Objects;

/**
 * Too many password-reset attempts for one email address or client in the current
 * window (FR-AUTH-019). The same exception, and therefore the same response, is used for
 * every scope and for registered and unregistered addresses alike.
 */
public final class PasswordResetRateLimitedException extends RuntimeException {

    private final Duration retryAfter;

    public PasswordResetRateLimitedException(Duration retryAfter) {
        super("Too many password-reset attempts", null, false, false);
        this.retryAfter = Objects.requireNonNull(retryAfter, "retryAfter");
    }

    /** Time until the current window ends, after which attempts are counted afresh. */
    public Duration retryAfter() {
        return retryAfter;
    }
}