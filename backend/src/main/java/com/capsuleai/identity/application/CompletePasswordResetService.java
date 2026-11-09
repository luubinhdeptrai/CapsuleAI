package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.InvalidValueException;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.domain.PasswordPolicy;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-AUTH-005 — accepted reset completion (FR-AUTH-004, FR-AUTH-017, BRULE-AUTH-005).
 *
 * <p>One transaction changes the password, consumes the interaction and revokes
 * every active session of the account; all three commit together or none does.
 * It locks the account first, then the interaction, then the sessions: the same
 * order reset issuance and login use, so these paths cannot deadlock each other.
 */
public final class CompletePasswordResetService {

    private final AccountRepository accounts;
    private final PasswordResetRepository resets;
    private final SessionRepository sessions;
    private final PasswordHasher passwordHasher;
    private final IdentityTransactions transactions;
    private final Clock clock;
    private final PasswordResetAttemptGuard attemptGuard;

    public CompletePasswordResetService(AccountRepository accounts,
                                        PasswordResetRepository resets,
                                        SessionRepository sessions,
                                        PasswordHasher passwordHasher,
                                        IdentityTransactions transactions,
                                        Clock clock,
                                        PasswordResetAttemptGuard attemptGuard) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.resets = Objects.requireNonNull(resets, "resets");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.attemptGuard = Objects.requireNonNull(attemptGuard, "attemptGuard");
    }

    /**
     * @throws RequestValidationException     if the token is missing or the new password breaks
     *         the policy; the interaction stays usable for a corrected submission (AC-10)
     * @throws PasswordResetRejectedException if the interaction is unknown, expired, used or
     *         replaced at the moment of submission (AC-08, AC-09); nothing was changed
     * @throws PasswordResetRateLimitedException if the client made too many checks/completions
     */
    public void complete(CompletePasswordResetCommand command) {
        attemptGuard.admit(PasswordResetAttemptScope.VERIFICATION_PER_CLIENT, command.clientAddress());
        validate(command);
        PasswordResetTokenHash presented = PasswordResetTokenHash.of(command.token());

        UUID accountId = resets.findByTokenHash(presented)
                .map(PasswordResetToken::accountId)
                .orElseThrow(PasswordResetRejectedException::new);

        // Hash before taking any lock: PBKDF2 is deliberately slow.
        PasswordHash newPasswordHash = passwordHasher.hash(command.newPassword());
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        boolean completed = transactions.inTransaction(
                () -> completeInsideTransaction(accountId, presented, newPasswordHash, now));
        if (!completed) {
            throw new PasswordResetRejectedException();
        }
    }

    /** Returns false instead of throwing for a refusal: nothing has changed at that point. */
    private boolean completeInsideTransaction(
            UUID accountId, PasswordResetTokenHash presented, PasswordHash newPasswordHash, Instant now) {
        if (!accounts.lockForCredentialChange(accountId)) {
            return false;
        }
        Optional<PasswordResetToken> usable = resets.lockByTokenHash(presented)
                .filter(token -> token.accountId().equals(accountId))
                .filter(token -> token.isUsableAt(now));
        if (usable.isEmpty()) {
            return false;
        }
        accounts.replacePasswordHash(accountId, newPasswordHash);
        resets.consume(usable.get().id(), now);
        sessions.revokeAllActiveForAccount(accountId, RevocationReason.PASSWORD_RESET, now);
        return true;
    }

    private static void validate(CompletePasswordResetCommand command) {
        List<FieldViolation> violations = new ArrayList<>();
        if (command.token() == null || command.token().isBlank()) {
            violations.add(new FieldViolation("token", ValueProblem.REQUIRED));
        }
        try {
            PasswordPolicy.validateNewPassword(command.newPassword());
        } catch (InvalidValueException rejected) {
            violations.add(new FieldViolation("newPassword", rejected.problem()));
        }
        if (!violations.isEmpty()) {
            throw new RequestValidationException(violations);
        }
    }
}