package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.application.ports.PasswordResetInstructionSender;
import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.application.ports.PasswordResetTokenGenerator;
import com.capsuleai.identity.application.ports.RecoveryResponsePacing;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.InvalidValueException;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-AUTH-005 — Forgot Password initiation (FR-AUTH-003, FR-AUTH-018, SI-003).
 *
 * <p>For a registered email it issues a new 30-minute interaction, replacing every
 * earlier open one, and hands the instructions to the email boundary after the
 * commit. For an unregistered email it does nothing. Both return normally, so the
 * caller answers both identically. Neither changes a password or a session.
 *
 * <p>Account-existence protection (OPEN-S2-04, OPEN-S2-05): attempts are counted per client
 * and per normalized email <em>before</em> the account is looked up, so registered and
 * unregistered addresses are limited identically and an outage of the counters fails both
 * alike. A successful request lasts at least the configured minimum response time whether
 * or not it issued anything. Residual risk: only a registered address reaches the issuance
 * writes, so a database failure exactly there is a 503 that an unregistered address cannot
 * produce; that response is truthful by decision, and rate limiting bounds any probing.
 */
public final class RequestPasswordResetService {

    private final AccountRepository accounts;
    private final PasswordResetRepository resets;
    private final PasswordResetTokenGenerator resetTokens;
    private final PasswordResetInstructionSender instructionSender;
    private final IdentityTransactions transactions;
    private final Clock clock;
    private final PasswordResetAttemptGuard attemptGuard;
    private final RecoveryResponsePacing pacing;

    public RequestPasswordResetService(AccountRepository accounts,
                                       PasswordResetRepository resets,
                                       PasswordResetTokenGenerator resetTokens,
                                       PasswordResetInstructionSender instructionSender,
                                       IdentityTransactions transactions,
                                       Clock clock,
                                       PasswordResetAttemptGuard attemptGuard,
                                       RecoveryResponsePacing pacing) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.resets = Objects.requireNonNull(resets, "resets");
        this.resetTokens = Objects.requireNonNull(resetTokens, "resetTokens");
        this.instructionSender = Objects.requireNonNull(instructionSender, "instructionSender");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.attemptGuard = Objects.requireNonNull(attemptGuard, "attemptGuard");
        this.pacing = Objects.requireNonNull(pacing, "pacing");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws RequestValidationException if the email is missing or cannot be an email
     *         address; this depends only on the typed value, never on account existence
     * @throws PasswordResetRateLimitedException if the client or the email exceeded its limit;
     *         decided before the account lookup, so it is the same for every address
     */
    public void request(RequestPasswordResetCommand command) {
        RecoveryResponsePacing.Pace pace = pacing.start();
        attemptGuard.admit(PasswordResetAttemptScope.REQUEST_PER_CLIENT, command.clientAddress());
        EmailAddress email = normalizedEmail(command.email());
        attemptGuard.admit(PasswordResetAttemptScope.REQUEST_PER_EMAIL, email.value());

        Optional<Account> account = accounts.findByEmail(email);
        if (account.isPresent()) {
            issueFor(account.get());
        }
        pace.finishNoSoonerThanMinimum();
    }

    private void issueFor(Account owner) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        String rawToken = resetTokens.newToken();
        PasswordResetToken issued = PasswordResetToken.issue(UUID.randomUUID(), owner.id(), now);

        boolean committed = transactions.inTransaction(() -> {
            if (!accounts.lockForCredentialChange(issued.accountId())) {
                return false;
            }
            resets.replaceOpenTokens(issued.accountId(), now);
            resets.add(issued, PasswordResetTokenHash.of(rawToken));
            return true;
        });

        // Instructions leave only for an interaction that is committed and therefore usable.
        if (committed) {
            instructionSender.sendInstructions(
                    new PasswordResetInstructions(owner.email(), rawToken, issued.expiresAt()));
        }
    }

    private static EmailAddress normalizedEmail(String rawEmail) {
        try {
            return EmailAddress.normalize(rawEmail);
        } catch (InvalidValueException rejected) {
            throw new RequestValidationException(List.of(new FieldViolation("email", rejected.problem())));
        }
    }
}