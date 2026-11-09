package com.capsuleai.identity.adapter.in.scheduling;

import com.capsuleai.identity.application.PurgeEndedPasswordResetsService;
import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.support.MutableClock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

/** A failed run must not escape the scheduler thread; the next run simply tries again. */
class PasswordResetCleanupJobTest {

    @Test
    void aFailedRunIsLoggedAndDoesNotPropagate() {
        PurgeEndedPasswordResetsService failing = new PurgeEndedPasswordResetsService(
                new UnreachableResets(), new NoCounters(), 10, new MutableClock(MutableClock.DEFAULT_START));

        assertThatCode(() -> new PasswordResetCleanupJob(failing).run()).doesNotThrowAnyException();
    }

    private static final class UnreachableResets implements PasswordResetRepository {
        @Override public int replaceOpenTokens(UUID accountId, Instant replacedAt) { throw new IllegalStateException(); }
        @Override public void add(PasswordResetToken token, PasswordResetTokenHash hash) { throw new IllegalStateException(); }
        @Override public Optional<PasswordResetToken> findByTokenHash(PasswordResetTokenHash hash) { return Optional.empty(); }
        @Override public Optional<PasswordResetToken> lockByTokenHash(PasswordResetTokenHash hash) { return Optional.empty(); }
        @Override public void consume(UUID tokenId, Instant consumedAt) { throw new IllegalStateException(); }
        @Override public int deleteEndedBefore(Instant cutoff, int batchSize) {
            throw new IllegalStateException("database unreachable");
        }
    }

    private static final class NoCounters implements PasswordResetAttemptLimiter {
        @Override public int recordAttempt(PasswordResetAttemptScope scope, String subject, Instant windowStart) { return 1; }
        @Override public int forgetWindowsStartedBefore(Instant cutoff) { return 0; }
    }
}
