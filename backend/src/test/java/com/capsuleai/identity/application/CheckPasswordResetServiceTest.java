package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckPasswordResetServiceTest {

    private final InMemoryPasswordResetRepository resets = new InMemoryPasswordResetRepository();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final CheckPasswordResetService service = new CheckPasswordResetService(resets, clock,
            InMemoryPasswordResetAttemptLimiter.permissiveGuard(clock));

    @Test
    void anIssuedInteractionCanBeOpenedRepeatedlyWithoutBeingChanged() {
        PasswordResetToken issued = issue("reset-secret");

        assertThatCode(() -> service.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7"))).doesNotThrowAnyException();
        assertThatCode(() -> service.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7"))).doesNotThrowAnyException();

        assertThat(resets.all()).containsExactly(issued);
    }

    @Test
    void openingAtTheThirtyMinuteBoundaryIsRejected() {
        issue("reset-secret");
        clock.advance(Duration.ofMinutes(30).minusSeconds(1));
        assertThatCode(() -> service.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7"))).doesNotThrowAnyException();

        clock.advance(Duration.ofSeconds(1));
        assertThatThrownBy(() -> service.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);
    }

    @Test
    void replacedAndUnknownInteractionsAreRejected() {
        PasswordResetToken issued = issue("reset-secret");
        resets.replaceOpenTokens(issued.accountId(), clock.instant());

        assertThatThrownBy(() -> service.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);
        assertThatThrownBy(() -> service.check(new CheckPasswordResetCommand("never-issued", "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);
    }

    @Test
    void aMissingTokenIsAValidationError() {
        assertThatThrownBy(() -> service.check(new CheckPasswordResetCommand("", "198.51.100.7")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected -> assertThat(rejected.violations())
                        .containsExactly(new FieldViolation("token", ValueProblem.REQUIRED)));
    }

    private PasswordResetToken issue(String rawToken) {
        PasswordResetToken token = PasswordResetToken.issue(UUID.randomUUID(), UUID.randomUUID(), clock.instant());
        resets.add(token, PasswordResetTokenHash.of(rawToken));
        return token;
    }


    @Test
    void checksFromOneClientAreLimited() {
        issue("reset-secret");
        CheckPasswordResetService strict = new CheckPasswordResetService(resets, clock, new PasswordResetAttemptGuard(
                new InMemoryPasswordResetAttemptLimiter(), 100, 100, 2, java.time.Duration.ofMinutes(15), clock));

        strict.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7"));
        strict.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7"));

        assertThatThrownBy(() -> strict.check(new CheckPasswordResetCommand("reset-secret", "198.51.100.7")))
                .isInstanceOf(PasswordResetRateLimitedException.class);
        assertThatCode(() -> strict.check(new CheckPasswordResetCommand("reset-secret", "203.0.113.9")))
                .doesNotThrowAnyException();
    }
}
