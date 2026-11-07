package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.identity.support.RecordingPasswordResetInstructionSender;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestPasswordResetServiceTest {

    private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    private final InMemoryPasswordResetRepository resets = new InMemoryPasswordResetRepository();
    private final SequencePasswordResetTokenGenerator resetTokens = new SequencePasswordResetTokenGenerator();
    private final RecordingPasswordResetInstructionSender sender = new RecordingPasswordResetInstructionSender();
    private final ImmediateTransactions transactions = new ImmediateTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final RecordingResponsePacing pacing = new RecordingResponsePacing();

    private final RequestPasswordResetService service =
            new RequestPasswordResetService(accounts, resets, resetTokens, sender, transactions, clock,
                    InMemoryPasswordResetAttemptLimiter.permissiveGuard(clock), pacing);

    @Test
    void aRegisteredEmailReceivesOneThirtyMinuteInteraction() {
        Account account = registered("person@example.com");

        service.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7"));

        PasswordResetToken stored = resets.all().get(0);
        assertThat(resets.all()).hasSize(1);
        assertThat(stored.accountId()).isEqualTo(account.id());
        assertThat(stored.issuedAt()).isEqualTo(MutableClock.DEFAULT_START);
        assertThat(stored.expiresAt()).isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofMinutes(30)));
        assertThat(sender.sent()).singleElement().satisfies(sent -> {
            assertThat(sent.recipient()).isEqualTo(account.email());
            assertThat(sent.expiresAt()).isEqualTo(stored.expiresAt());
            assertThat(resets.findByTokenHash(PasswordResetTokenHash.of(sent.resetToken()))).contains(stored);
        });
    }

    @Test
    void caseAndSurroundingWhitespaceVariantsReachTheSameAccount() {
        Account account = registered("person@example.com");

        service.request(new RequestPasswordResetCommand("  PERSON@Example.COM ", "198.51.100.7"));

        assertThat(sender.sent()).singleElement()
                .extracting(PasswordResetInstructions::recipient)
                .isEqualTo(account.email());
    }

    @Test
    void providerAliasesAreNotMergedIntoAnotherAccount() {
        registered("person@example.com");

        service.request(new RequestPasswordResetCommand("person+alias@example.com", "198.51.100.7"));

        assertThat(resets.all()).isEmpty();
        assertThat(sender.sent()).isEmpty();
    }

    @Test
    void anUnregisteredEmailReturnsNormallyAndIssuesNothing() {
        service.request(new RequestPasswordResetCommand("nobody@example.com", "198.51.100.7"));

        assertThat(resets.all()).isEmpty();
        assertThat(sender.sent()).isEmpty();
        assertThat(transactions.count()).isZero();
    }

    @Test
    void aNewIssuanceReplacesEveryEarlierOpenInteraction() {
        registered("person@example.com");
        service.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7"));
        clock.advance(Duration.ofMinutes(5));

        service.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7"));

        assertThat(resets.all()).hasSize(2);
        PasswordResetToken first = resets.findByTokenHash(PasswordResetTokenHash.of("reset-token-1")).orElseThrow();
        PasswordResetToken second = resets.findByTokenHash(PasswordResetTokenHash.of("reset-token-2")).orElseThrow();
        assertThat(first.replacedAt()).isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofMinutes(5)));
        assertThat(first.isUsableAt(clock.instant())).isFalse();
        assertThat(second.isUsableAt(clock.instant())).isTrue();
    }

    @Test
    void requestingChangesNeitherThePasswordNorTheSessions() {
        Account account = registered("person@example.com");

        service.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7"));

        assertThat(accounts.findById(account.id())).map(Account::passwordHash).contains(account.passwordHash());
    }

    @Test
    void aMissingOrMalformedEmailIsAValidationErrorBeforeAnyLookup() {
        assertThatThrownBy(() -> service.request(new RequestPasswordResetCommand(" ", "198.51.100.7")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected -> assertThat(rejected.violations())
                        .containsExactly(new FieldViolation("email", ValueProblem.REQUIRED)));
        assertThatThrownBy(() -> service.request(new RequestPasswordResetCommand("not-an-email", "198.51.100.7")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected -> assertThat(rejected.violations())
                        .containsExactly(new FieldViolation("email", ValueProblem.INVALID_FORMAT)));
        assertThat(sender.sent()).isEmpty();
    }

    @Test
    void instructionsAreNotSentWhenTheIssuanceCannotBeCommitted() {
        Account account = registered("person@example.com");
        RequestPasswordResetService failingCommit = new RequestPasswordResetService(accounts, resets, resetTokens,
                sender, new FailingTransactions(), clock, InMemoryPasswordResetAttemptLimiter.permissiveGuard(clock), pacing);

        assertThatThrownBy(() -> failingCommit.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sender.sent()).isEmpty();
        assertThat(accounts.findById(account.id())).isPresent();
    }

    private Account registered(String normalizedEmail) {
        Account account = new Account(UUID.randomUUID(), new EmailAddress(normalizedEmail),
                new PasswordHash("test-hash:original password"), Optional.empty(), MutableClock.DEFAULT_START);
        accounts.addIfEmailAvailable(account);
        return account;
    }

    /** A transaction boundary whose commit fails, as when the database connection is lost. */
    private static final class FailingTransactions implements IdentityTransactions {

        @Override
        public <T> T inTransaction(Supplier<T> work) {
            work.get();
            throw new IllegalStateException("commit failed");
        }
    }


    // ---- OPEN-S2-04 / OPEN-S2-05: abuse protection and timing hardening

    @Test
    void registeredAndUnregisteredAddressesAreLimitedIdenticallyBeforeAnyAccountLookup() {
        registered("person@example.com");
        RequestPasswordResetService strict = new RequestPasswordResetService(accounts, resets, resetTokens, sender,
                transactions, clock, new PasswordResetAttemptGuard(new InMemoryPasswordResetAttemptLimiter(), 2, 100, 100,
                        Duration.ofMinutes(15), clock), pacing);

        for (String email : new String[] {"person@example.com", "nobody@example.com"}) {
            strict.request(new RequestPasswordResetCommand(email, "198.51.100.7"));
            strict.request(new RequestPasswordResetCommand(email, "198.51.100.7"));
            assertThatThrownBy(() -> strict.request(new RequestPasswordResetCommand(email, "198.51.100.7")))
                    .as("third request for %s in one window", email)
                    .isInstanceOf(PasswordResetRateLimitedException.class);
        }
        assertThat(sender.sent()).as("no instructions once limited").hasSize(2);
    }

    @Test
    void theEmailLimitCountsTheNormalizedAddressSoSpellingVariantsShareIt() {
        RequestPasswordResetService strict = new RequestPasswordResetService(accounts, resets, resetTokens, sender,
                transactions, clock, new PasswordResetAttemptGuard(new InMemoryPasswordResetAttemptLimiter(), 1, 100, 100,
                        Duration.ofMinutes(15), clock), pacing);

        strict.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7"));

        assertThatThrownBy(() -> strict.request(new RequestPasswordResetCommand("  PERSON@Example.com ", "203.0.113.9")))
                .isInstanceOf(PasswordResetRateLimitedException.class);
    }

    @Test
    void theClientLimitAppliesEvenToMalformedRequests() {
        RequestPasswordResetService strict = new RequestPasswordResetService(accounts, resets, resetTokens, sender,
                transactions, clock, new PasswordResetAttemptGuard(new InMemoryPasswordResetAttemptLimiter(), 100, 1, 100,
                        Duration.ofMinutes(15), clock), pacing);

        assertThatThrownBy(() -> strict.request(new RequestPasswordResetCommand("not-an-email", "198.51.100.7")))
                .isInstanceOf(RequestValidationException.class);
        assertThatThrownBy(() -> strict.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7")))
                .isInstanceOf(PasswordResetRateLimitedException.class);
    }

    @Test
    void everySuccessfulRequestIsPacedWhetherOrNotTheAddressIsRegistered() {
        registered("person@example.com");

        service.request(new RequestPasswordResetCommand("person@example.com", "198.51.100.7"));
        service.request(new RequestPasswordResetCommand("nobody@example.com", "198.51.100.7"));

        assertThat(pacing.started()).isEqualTo(2);
        assertThat(pacing.finished()).isEqualTo(2);
    }

    @Test
    void aFailedRequestIsNotPacedSoATruthfulErrorIsNotDelayed() {
        assertThatThrownBy(() -> service.request(new RequestPasswordResetCommand("not-an-email", "198.51.100.7")))
                .isInstanceOf(RequestValidationException.class);

        assertThat(pacing.finished()).isZero();
    }
}
