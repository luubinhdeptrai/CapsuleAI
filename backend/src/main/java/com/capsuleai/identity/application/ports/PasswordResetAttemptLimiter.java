package com.capsuleai.identity.application.ports;

import java.time.Instant;

/**
 * Durable attempt counters for password-reset abuse protection (FR-AUTH-019, ADA-008).
 *
 * <p>Counting must be atomic: concurrent attempts for the same subject and window each
 * receive a distinct count, so a limit cannot be exceeded by a burst of parallel
 * requests. The adapter never stores the subject itself, only a one-way hash.
 */
public interface PasswordResetAttemptLimiter {

    /**
     * Records one attempt and returns how many attempts, including this one, the subject
     * has made in the window that starts at {@code windowStart}.
     */
    int recordAttempt(PasswordResetAttemptScope scope, String subject, Instant windowStart);

    /** Removes counters of windows that started before {@code cutoff}; returns how many were removed. */
    int forgetWindowsStartedBefore(Instant cutoff);
}