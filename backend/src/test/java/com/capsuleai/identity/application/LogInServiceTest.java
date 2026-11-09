package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogInServiceTest {

    private static final String PASSWORD = "correct horse battery staple";
    /** "  Mật khẩu rất dài  " */
    private static final String UNICODE_PASSWORD = "  M\u1EADt kh\u1EA9u r\u1EA5t d\u00E0i  ";

    private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    private final RecordingPasswordHasher hasher = new RecordingPasswordHasher();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final SequenceRefreshTokenGenerator refreshTokens = new SequenceRefreshTokenGenerator();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final ImmediateTransactions transactions = new ImmediateTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);

    private final LogInService service =
            new LogInService(accounts, hasher, sessions, refreshTokens, accessTokens, transactions, clock);

    @Test
    void establishesASessionForTheNormalizedEmailAndExactPassword() {
        Account account = registered("person@example.com", PASSWORD);

        SessionTokens tokens = service.logIn(new LogInCommand("  PERSON@example.com ", PASSWORD));

        assertThat(sessions.all()).hasSize(1);
        LoginSession session = sessions.all().get(0);
        assertThat(tokens.accountId()).isEqualTo(account.id());
        assertThat(session.accountId()).isEqualTo(account.id());
        assertThat(session.establishedAt()).isEqualTo(MutableClock.DEFAULT_START);
        assertThat(tokens.sessionExpiresAt())
                .isEqualTo(session.expiresAt())
                .isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofDays(30)));
        assertThat(tokens.accessTokenExpiresAt())
                .isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofMinutes(15)));
        assertThat(sessions.sessionIdForRefreshToken(RefreshTokenHash.of(tokens.refreshToken())))
                .contains(session.id());
        assertThat(accessTokens.issued()).singleElement().satisfies(issued -> {
            assertThat(issued.accountId()).isEqualTo(account.id());
            assertThat(issued.sessionId()).isEqualTo(session.id());
            assertThat(issued.expiresAt()).isEqualTo(tokens.accessTokenExpiresAt());
        });
        assertThat(transactions.count()).isOne();
    }

    @Test
    void everySuccessfulLoginCreatesASeparateSession() {
        registered("person@example.com", PASSWORD);

        SessionTokens first = service.logIn(new LogInCommand("person@example.com", PASSWORD));
        SessionTokens second = service.logIn(new LogInCommand("person@example.com", PASSWORD));

        assertThat(sessions.all()).hasSize(2);
        assertThat(sessions.all().get(0).id()).isNotEqualTo(sessions.all().get(1).id());
        assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
    }

    @Test
    void wrongPasswordGrantsNoSessionAndNoToken() {
        registered("person@example.com", PASSWORD);

        assertThatThrownBy(() -> service.logIn(new LogInCommand("person@example.com", "not the password")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(sessions.all()).isEmpty();
        assertThat(accessTokens.issued()).isEmpty();
        assertThat(transactions.count()).isZero();
    }

    @Test
    void unknownEmailIsRejectedExactlyLikeAWrongPassword() {
        assertThatThrownBy(() -> service.logIn(new LogInCommand("nobody@example.com", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(sessions.all()).isEmpty();
    }

    @Test
    void exactPasswordWithSpacesAndUnicodeAuthenticates() {
        registered("unicode@example.com", UNICODE_PASSWORD);

        assertThat(service.logIn(new LogInCommand("unicode@example.com", UNICODE_PASSWORD)).accessToken())
                .isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("changedVariantsOfTheUnicodePassword")
    void changedPasswordVariantsDoNotAuthenticate(String variant) {
        registered("unicode@example.com", UNICODE_PASSWORD);

        assertThatThrownBy(() -> service.logIn(new LogInCommand("unicode@example.com", variant)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    static Stream<String> changedVariantsOfTheUnicodePassword() {
        return Stream.of(
                UNICODE_PASSWORD.strip(),
                UNICODE_PASSWORD.replace(" ", ""),
                Normalizer.normalize(UNICODE_PASSWORD, Normalizer.Form.NFD));
    }

    @Test
    void incompleteCredentialsAreValidationErrors() {
        assertThatThrownBy(() -> service.logIn(new LogInCommand(" ", "")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new FieldViolation("email", ValueProblem.REQUIRED),
                                new FieldViolation("password", ValueProblem.REQUIRED)));
    }

    @Test
    void malformedEmailIsTreatedAsInvalidCredentials() {
        assertThatThrownBy(() -> service.logIn(new LogInCommand("not-an-email", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginNeedsNoOptionalProfileInformation() {
        Account account = registered("minimal@example.com", PASSWORD);
        assertThat(account.displayName()).isEmpty();

        assertThat(service.logIn(new LogInCommand("minimal@example.com", PASSWORD)).accountId())
                .isEqualTo(account.id());
    }

    /** US-AUTH-005: a reset that replaces the credential after verification must win. */
    @Test
    void credentialReplacedBetweenVerificationAndSessionCreationGrantsNoSession() {
        Account account = registered("reset-race@example.com", PASSWORD);
        PasswordHasher resetDuringVerification = new PasswordHasher() {
            @Override
            public PasswordHash hash(String rawPassword) {
                return hasher.hash(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, PasswordHash storedHash) {
                boolean matched = hasher.matches(rawPassword, storedHash);
                accounts.replacePasswordHash(account.id(), hasher.hash("the password set by reset"));
                return matched;
            }
        };
        LogInService racingLogin = new LogInService(
                accounts, resetDuringVerification, sessions, refreshTokens, accessTokens, transactions, clock);

        assertThatThrownBy(() -> racingLogin.logIn(new LogInCommand("reset-race@example.com", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(sessions.all()).isEmpty();
    }

    private Account registered(String email, String password) {
        Account account = new Account(UUID.randomUUID(), EmailAddress.normalize(email),
                hasher.hash(password), Optional.empty(), clock.instant());
        accounts.addIfEmailAvailable(account);
        return account;
    }
}
