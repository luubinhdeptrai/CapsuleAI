package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.domain.PasswordResetPolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Deletes password-reset data that no longer serves a purpose (OPEN-S2-07, DATA-AUTH-006).
 *
 * <p>An interaction ends when it is consumed, replaced or expires; it is deleted
 * {@link PasswordResetPolicy#RECORD_RETENTION} after that moment. A usable interaction has
 * neither ended nor been kept for 30 days, so it is never deleted. Attempt counters are
 * removed one day after their window started; they are never needed longer.
 *
 * <p>Each batch is its own short statement, so the cleanup never holds locks for long and
 * never blocks a concurrent reset. A failed run changes nothing that the next run cannot redo.
 */
public final class PurgeEndedPasswordResetsService {

    static final Duration ATTEMPT_COUNTER_RETENTION = Duration.ofDays(1);

    private final PasswordResetRepository resets;
    private final PasswordResetAttemptLimiter attempts;
    private final int batchSize;
    private final Clock clock;

    public PurgeEndedPasswordResetsService(PasswordResetRepository resets,
                                           PasswordResetAttemptLimiter attempts,
                                           int batchSize,
                                           Clock clock) {
        this.resets = Objects.requireNonNull(resets, "resets");
        this.attempts = Objects.requireNonNull(attempts, "attempts");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (batchSize < 1) {
            throw new IllegalArgumentException("The cleanup batch size must be at least 1");
        }
        this.batchSize = batchSize;
    }

    /** Runs one cleanup pass and reports what was deleted. */
    public PurgeResult purge() {
        Instant now = clock.instant();
        Instant interactionCutoff = now.minus(PasswordResetPolicy.RECORD_RETENTION);
        int interactions = 0;
        int deleted;
        do {
            deleted = resets.deleteEndedBefore(interactionCutoff, batchSize);
            interactions += deleted;
        } while (deleted == batchSize);
        int counters = attempts.forgetWindowsStartedBefore(now.minus(ATTEMPT_COUNTER_RETENTION));
        return new PurgeResult(interactions, counters);
    }

    /** Numbers of deleted interactions and attempt counters. */
    public record PurgeResult(int deletedInteractions, int deletedAttemptCounters) {
    }
}