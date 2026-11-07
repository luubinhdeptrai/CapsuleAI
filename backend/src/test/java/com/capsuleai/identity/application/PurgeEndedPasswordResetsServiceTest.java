package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurgeEndedPasswordResetsServiceTest {

    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final InMemoryPasswordResetRepository resets = new InMemoryPasswordResetRepository();
    private final InMemoryPasswordResetAttemptLimiter attempts = new InMemoryPasswordResetAttemptLimiter();

    @Test
    void everyDueInteractionIsDeletedEvenWhenThereAreMoreThanOneBatch() {
        Instant longAgo = clock.instant().minus(Duration.ofDays(40));
        for (int index = 0; index < 5; index++) {
            resets.add(PasswordResetToken.issue(UUID.randomUUID(), UUID.randomUUID(), longAgo.plusSeconds(index)),
                    PasswordResetTokenHash.of("expired-" + index));
        }
        PasswordResetToken usable = PasswordResetToken.issue(UUID.randomUUID(), UUID.randomUUID(), clock.instant());
        resets.add(usable, PasswordResetTokenHash.of("usable"));
        attempts.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "198.51.100.7",
                clock.instant().minus(Duration.ofDays(2)));

        PurgeEndedPasswordResetsService.PurgeResult result =
                new PurgeEndedPasswordResetsService(resets, attempts, 2, clock).purge();

        assertThat(result.deletedInteractions()).isEqualTo(5);
        assertThat(result.deletedAttemptCounters()).isOne();
        assertThat(resets.all()).containsExactly(usable);
    }
}
