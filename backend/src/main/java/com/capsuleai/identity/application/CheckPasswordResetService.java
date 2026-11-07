package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * US-AUTH-005 AC-06/AC-08 — opening received instructions: is this interaction
 * still usable right now? Read-only. Opening never extends, consumes or changes
 * anything, and completion checks validity again (AC-09).
 */
public final class CheckPasswordResetService {

    private final PasswordResetRepository resets;
    private final Clock clock;
    private final PasswordResetAttemptGuard attemptGuard;

    public CheckPasswordResetService(PasswordResetRepository resets, Clock clock, PasswordResetAttemptGuard attemptGuard) {
        this.resets = Objects.requireNonNull(resets, "resets");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.attemptGuard = Objects.requireNonNull(attemptGuard, "attemptGuard");
    }

    /**
     * @throws RequestValidationException     if no token was supplied
     * @throws PasswordResetRejectedException if the interaction is unknown, expired, used or replaced
     * @throws PasswordResetRateLimitedException if the client made too many checks/completions
     */
    public void check(CheckPasswordResetCommand command) {
        attemptGuard.admit(PasswordResetAttemptScope.VERIFICATION_PER_CLIENT, command.clientAddress());
        if (command.token() == null || command.token().isBlank()) {
            throw new RequestValidationException(List.of(new FieldViolation("token", ValueProblem.REQUIRED)));
        }
        boolean usable = resets.findByTokenHash(PasswordResetTokenHash.of(command.token()))
                .filter(token -> token.isUsableAt(clock.instant()))
                .isPresent();
        if (!usable) {
            throw new PasswordResetRejectedException();
        }
    }
}