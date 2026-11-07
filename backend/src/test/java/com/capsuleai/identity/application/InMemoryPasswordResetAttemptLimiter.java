package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Test fake: exact in-memory attempt counters per scope, subject and window. */
public final class InMemoryPasswordResetAttemptLimiter implements PasswordResetAttemptLimiter {

    record Key(PasswordResetAttemptScope scope, String subject, Instant windowStart) {
    }

    private final Map<Key, Integer> attempts = new HashMap<>();

    /** A guard whose limits no unit test reaches. */
    public static PasswordResetAttemptGuard permissiveGuard(Clock clock) {
        return new PasswordResetAttemptGuard(new InMemoryPasswordResetAttemptLimiter(), 1_000, 1_000, 1_000,
                Duration.ofMinutes(15), clock);
    }

    @Override
    public synchronized int recordAttempt(PasswordResetAttemptScope scope, String subject, Instant windowStart) {
        return attempts.merge(new Key(scope, subject, windowStart), 1, Integer::sum);
    }

    @Override
    public synchronized int forgetWindowsStartedBefore(Instant cutoff) {
        List<Key> old = attempts.keySet().stream().filter(key -> key.windowStart().isBefore(cutoff)).toList();
        old.forEach(attempts::remove);
        return old.size();
    }

    public synchronized int attemptsOf(PasswordResetAttemptScope scope, String subject) {
        return attempts.entrySet().stream()
                .filter(entry -> entry.getKey().scope() == scope && entry.getKey().subject().equals(subject))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }
}
