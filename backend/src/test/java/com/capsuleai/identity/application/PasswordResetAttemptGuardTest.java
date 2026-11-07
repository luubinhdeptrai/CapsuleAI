package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetAttemptGuardTest {

    private static final Instant WINDOW_START = Instant.parse("2026-01-05T08:00:00Z");

    private final MutableClock clock = new MutableClock(WINDOW_START.plusSeconds(60));
    private final InMemoryPasswordResetAttemptLimiter limiter = new InMemoryPasswordResetAttemptLimiter();
    private final PasswordResetAttemptGuard guard =
            new PasswordResetAttemptGuard(limiter, 3, 5, 7, Duration.ofMinutes(15), clock);

    @Test
    void theConfiguredNumberOfAttemptsIsAdmittedAndTheNextOneIsLimitedUntilTheWindowEnds() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            guard.admit(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com");
        }

        assertThatThrownBy(() -> guard.admit(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com"))
                .isInstanceOf(PasswordResetRateLimitedException.class)
                .satisfies(limited -> assertThat(((PasswordResetRateLimitedException) limited).retryAfter())
                        .isEqualTo(Duration.ofMinutes(14)));
    }

    @Test
    void eachScopeHasItsOwnLimit() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            guard.admit(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "198.51.100.7");
        }
        for (int attempt = 1; attempt <= 7; attempt++) {
            guard.admit(PasswordResetAttemptScope.VERIFICATION_PER_CLIENT, "198.51.100.7");
        }

        assertThatThrownBy(() -> guard.admit(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "198.51.100.7"))
                .isInstanceOf(PasswordResetRateLimitedException.class);
        assertThatThrownBy(() -> guard.admit(PasswordResetAttemptScope.VERIFICATION_PER_CLIENT, "198.51.100.7"))
                .isInstanceOf(PasswordResetRateLimitedException.class);
        assertThatCode(() -> guard.admit(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "203.0.113.9"))
                .as("another client is not affected").doesNotThrowAnyException();
    }

    @Test
    void aNewWindowCountsAfresh() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            guard.admit(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com");
        }
        clock.setInstant(WINDOW_START.plus(Duration.ofMinutes(15)));

        assertThatCode(() -> guard.admit(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void blockedAttemptsAreStillCountedSoPersistentProbingStaysBlocked() {
        for (int attempt = 1; attempt <= 6; attempt++) {
            try {
                guard.admit(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com");
            } catch (PasswordResetRateLimitedException expected) {
                // keep trying, as an abusive client would
            }
        }

        assertThat(limiter.attemptsOf(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com")).isEqualTo(6);
    }

    @Test
    void windowsAreAlignedToTheEpochSoEveryInstanceAgrees() {
        assertThat(guard.windowStartOf(Instant.parse("2026-01-05T08:14:59Z"))).isEqualTo(WINDOW_START);
        assertThat(guard.windowStartOf(Instant.parse("2026-01-05T08:15:00Z"))).isEqualTo(WINDOW_START.plusSeconds(900));
    }

    @Test
    void limitsAndWindowMustBeUsable() {
        assertThatThrownBy(() -> new PasswordResetAttemptGuard(limiter, 0, 5, 7, Duration.ofMinutes(15), clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PasswordResetAttemptGuard(limiter, 3, 5, 7, Duration.ofMillis(500), clock))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
