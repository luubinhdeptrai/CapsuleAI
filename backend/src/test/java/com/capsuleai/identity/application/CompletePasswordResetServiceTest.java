package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.identity.support.RecordingPasswordResetInstructionSender;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompletePasswordResetServiceTest {

    private static final String NEW_PASSWORD = "a brand new passphrase";

    private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    private final InMemoryPasswordResetRepository resets = new InMemoryPasswordResetRepository();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final RecordingPasswordHasher hasher = new RecordingPasswordHasher();
    private final ImmediateTransactions transactions = new ImmediateTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final RecordingPasswordResetInstructionSender sender = new RecordingPasswordResetInstructionSender();

    private final RequestPasswordResetService requestService = new RequestPasswordResetService(
            accounts, resets, new SequencePasswordResetTokenGenerator(), sender, transactions, clock,
            InMemoryPasswordResetAttemptLimiter.permissiveGuard(clock), new RecordingResponsePacing());
    private final CompletePasswordResetService service =
            new CompletePasswordResetService(accounts, resets, sessions, hasher, transactions, clock,
                    InMemoryPasswordResetAttemptLimiter.permissiveGuard(clock));

    @Test
    void anAcceptedResetChangesThePasswordConsumesTheInteractionAndRevokesEverySession() {
        Account account = registered("person@example.com");
        LoginSession phone = sessionOf(account);
        LoginSession laptop = sessionOf(account);
        String token = issuedTokenFor(account);

        service.complete(new CompletePasswordResetCommand(token, NEW_PASSWORD, "198.51.100.7"));

        assertThat(accounts.findById(account.id()).orElseThrow().passwordHash())
                .isEqualTo(new PasswordHash("test-hash:" + NEW_PASSWORD));
        assertThat(resets.findByTokenHash(PasswordResetTokenHash.of(token)).orElseThrow().consumedAt())
                .isEqualTo(clock.instant());
        assertThat(sessions.all()).extracting(LoginSession::id).containsExactlyInAnyOrder(phone.id(), laptop.id());
        assertThat(sessions.all()).allSatisfy(session -> {
            assertThat(session.revocationReason()).isEqualTo(RevocationReason.PASSWORD_RESET);
            assertThat(session.isUsableAt(clock.instant())).isFalse();
        });
    }

    @Test
    void anotherAccountKeepsItsPasswordAndSessions() {
        Account account = registered("person@example.com");
        Account other = registered("other@example.com");
        LoginSession otherSession = sessionOf(other);

        service.complete(new CompletePasswordResetCommand(issuedTokenFor(account), NEW_PASSWORD, "198.51.100.7"));

        assertThat(accounts.findById(other.id()).orElseThrow().passwordHash()).isEqualTo(other.passwordHash());
        assertThat(sessions.findById(otherSession.id()).orElseThrow().isUsableAt(clock.instant())).isTrue();
    }

    @Test
    void theExactNewPasswordIsHashedWithoutTrimmingOrNormalization() {
        Account account = registered("person@example.com");
        String unicodePassword = "  Mật khẩu mới  ";

        service.complete(new CompletePasswordResetCommand(issuedTokenFor(account), unicodePassword, "198.51.100.7"));

        assertThat(hasher.hashedValues()).containsExactly(unicodePassword);
    }

    @Test
    void aConsumedInteractionCannotChangeThePasswordASecondTime() {
        Account account = registered("person@example.com");
        String token = issuedTokenFor(account);
        service.complete(new CompletePasswordResetCommand(token, NEW_PASSWORD, "198.51.100.7"));

        assertThatThrownBy(() -> service.complete(new CompletePasswordResetCommand(token, "yet another passphrase", "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);
        assertThat(accounts.findById(account.id()).orElseThrow().passwordHash())
                .isEqualTo(new PasswordHash("test-hash:" + NEW_PASSWORD));
    }

    @Test
    void anInteractionIsUsableOneSecondBeforeExpiryButNotAtThirtyMinutes() {
        Account account = registered("person@example.com");
        String expiring = issuedTokenFor(account);
        clock.advance(Duration.ofMinutes(30));

        assertThatThrownBy(() -> service.complete(new CompletePasswordResetCommand(expiring, NEW_PASSWORD, "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);
        assertThat(accounts.findById(account.id()).orElseThrow().passwordHash()).isEqualTo(account.passwordHash());

        String fresh = issuedTokenFor(account);
        clock.advance(Duration.ofMinutes(30).minusSeconds(1));
        service.complete(new CompletePasswordResetCommand(fresh, NEW_PASSWORD, "198.51.100.7"));
        assertThat(resets.findByTokenHash(PasswordResetTokenHash.of(fresh)).orElseThrow().consumedAt()).isNotNull();
    }

    @Test
    void aReplacedInteractionIsRejectedAndTheReplacementStillWorks() {
        Account account = registered("person@example.com");
        String replaced = issuedTokenFor(account);
        String replacement = issuedTokenFor(account);

        assertThatThrownBy(() -> service.complete(new CompletePasswordResetCommand(replaced, NEW_PASSWORD, "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);
        service.complete(new CompletePasswordResetCommand(replacement, NEW_PASSWORD, "198.51.100.7"));

        assertThat(accounts.findById(account.id()).orElseThrow().passwordHash())
                .isEqualTo(new PasswordHash("test-hash:" + NEW_PASSWORD));
    }

    @Test
    void anUnknownSecretIsRejectedWithoutHashingOrChangingAnything() {
        Account account = registered("person@example.com");
        LoginSession session = sessionOf(account);

        assertThatThrownBy(() -> service.complete(new CompletePasswordResetCommand("made-up", NEW_PASSWORD, "198.51.100.7")))
                .isInstanceOf(PasswordResetRejectedException.class);

        assertThat(hasher.hashedValues()).isEmpty();
        assertThat(sessions.findById(session.id()).orElseThrow().isRevoked()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 129})
    void aPolicyBreakingPasswordIsRejectedAndLeavesTheInteractionUsable(int length) {
        Account account = registered("person@example.com");
        String token = issuedTokenFor(account);

        assertThatThrownBy(() -> service.complete(new CompletePasswordResetCommand(token, "p".repeat(length), "198.51.100.7")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected -> assertThat(rejected.violations())
                        .singleElement()
                        .extracting(FieldViolation::field)
                        .isEqualTo("newPassword"));

        PasswordResetToken unchanged = resets.findByTokenHash(PasswordResetTokenHash.of(token)).orElseThrow();
        assertThat(unchanged.isUsableAt(clock.instant())).isTrue();
        service.complete(new CompletePasswordResetCommand(token, NEW_PASSWORD, "198.51.100.7"));
    }

    @ParameterizedTest
    @ValueSource(ints = {12, 128})
    void passwordsAtTheLengthBoundariesAreAccepted(int length) {
        Account account = registered("person@example.com");

        service.complete(new CompletePasswordResetCommand(issuedTokenFor(account), "p".repeat(length), "198.51.100.7"));

        assertThat(hasher.hashedValues()).containsExactly("p".repeat(length));
    }

    @Test
    void missingTokenAndPasswordAreReportedTogether() {
        assertThatThrownBy(() -> service.complete(new CompletePasswordResetCommand(null, null, "198.51.100.7")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected -> assertThat(rejected.violations())
                        .containsExactly(
                                new FieldViolation("token", ValueProblem.REQUIRED),
                                new FieldViolation("newPassword", ValueProblem.REQUIRED)));
    }

    private Account registered(String normalizedEmail) {
        Account account = new Account(UUID.randomUUID(), new EmailAddress(normalizedEmail),
                new PasswordHash("test-hash:original password"), Optional.empty(), clock.instant());
        accounts.addIfEmailAvailable(account);
        return account;
    }

    private String issuedTokenFor(Account account) {
        requestService.request(new RequestPasswordResetCommand(account.email().value(), "198.51.100.7"));
        return sender.latestTokenFor(account.email().value()).orElseThrow();
    }

    private LoginSession sessionOf(Account account) {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), account.id(), clock.instant());
        sessions.create(session, RefreshTokenHash.of(UUID.randomUUID().toString()));
        return session;
    }


    @Test
    void completionsFromALimitedClientChangeNothing() {
        Account account = registered("person@example.com");
        String token = issuedTokenFor(account);
        PasswordResetAttemptGuard strictGuard = new PasswordResetAttemptGuard(
                new InMemoryPasswordResetAttemptLimiter(), 100, 100, 1, Duration.ofMinutes(15), clock);
        CompletePasswordResetService strict =
                new CompletePasswordResetService(accounts, resets, sessions, hasher, transactions, clock, strictGuard);
        strictGuard.admit(com.capsuleai.identity.application.ports.PasswordResetAttemptScope.VERIFICATION_PER_CLIENT,
                "198.51.100.7");

        assertThatThrownBy(() -> strict.complete(new CompletePasswordResetCommand(token, NEW_PASSWORD, "198.51.100.7")))
                .isInstanceOf(PasswordResetRateLimitedException.class);
        assertThat(accounts.findByEmail(account.email()).orElseThrow().passwordHash())
                .isEqualTo(account.passwordHash());
    }

    /** X-2: only active sessions are revoked; earlier revocations keep their recorded reason. */
    @Test
    void earlierRevocationsKeepTheirReasonAndOnlyActiveSessionsAreRevoked() {
        Account account = registered("person@example.com");
        LoginSession active = sessionOf(account);
        LoginSession loggedOut = sessionOf(account);
        sessions.revoke(loggedOut.id(), account.id(), RevocationReason.LOGOUT, clock.instant());
        String token = issuedTokenFor(account);

        service.complete(new CompletePasswordResetCommand(token, NEW_PASSWORD, "198.51.100.7"));

        assertThat(sessions.findById(loggedOut.id()).orElseThrow().revocationReason()).isEqualTo(RevocationReason.LOGOUT);
        assertThat(sessions.findById(active.id()).orElseThrow().revocationReason())
                .isEqualTo(RevocationReason.PASSWORD_RESET);
    }
}
