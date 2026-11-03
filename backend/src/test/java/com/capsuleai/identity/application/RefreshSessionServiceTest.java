package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshSessionServiceTest {

    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final SequenceRefreshTokenGenerator refreshTokens = new SequenceRefreshTokenGenerator();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final ImmediateTransactions transactions = new ImmediateTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final RefreshSessionService service =
            new RefreshSessionService(sessions, refreshTokens, accessTokens, transactions, clock);
    private final UUID accountId = UUID.randomUUID();

    private record StoredSession(LoginSession session, String refreshToken) {
    }

    @Test
    void renewalRotatesTheRefreshTokenAndKeepsTheSessionMaximum() {
        StoredSession stored = loggedIn();
        clock.advance(Duration.ofMinutes(20));

        SessionTokens renewed = renew(stored.refreshToken());

        assertThat(renewed.accountId()).isEqualTo(accountId);
        assertThat(renewed.refreshToken()).isNotEqualTo(stored.refreshToken());
        assertThat(renewed.sessionExpiresAt()).isEqualTo(stored.session().expiresAt());
        assertThat(renewed.accessTokenExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(15)));
        assertThat(sessions.refreshToken(RefreshTokenHash.of(stored.refreshToken())))
                .hasValueSatisfying(old -> assertThat(old.consumedAt()).isEqualTo(clock.instant()));
        assertThat(sessions.refreshToken(RefreshTokenHash.of(renewed.refreshToken())))
                .hasValueSatisfying(replacement -> {
                    assertThat(replacement.sessionId()).isEqualTo(stored.session().id());
                    assertThat(replacement.isConsumed()).isFalse();
                });
        assertThat(accessTokens.issued()).singleElement()
                .satisfies(issued -> assertThat(issued.sessionId()).isEqualTo(stored.session().id()));
    }

    @Test
    void consumedTokenReuseRevokesTheSessionIncludingItsNewerToken() {
        StoredSession stored = loggedIn();
        SessionTokens renewed = renew(stored.refreshToken());

        assertThatThrownBy(() -> renew(stored.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().revocationReason())
                .isEqualTo(RevocationReason.REFRESH_TOKEN_REUSE);
        assertThatThrownBy(() -> renew(renewed.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void reuseRevokesOnlyTheAffectedSession() {
        StoredSession deviceA = loggedIn();
        StoredSession deviceB = loggedIn();
        renew(deviceA.refreshToken());

        assertThatThrownBy(() -> renew(deviceA.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(deviceB.session().id()).orElseThrow().isRevoked()).isFalse();
        assertThat(renew(deviceB.refreshToken()).accountId()).isEqualTo(accountId);
    }

    @Test
    void unknownRefreshTokenChangesNothing() {
        StoredSession stored = loggedIn();

        assertThatThrownBy(() -> renew("never-issued"))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().isRevoked()).isFalse();
    }

    @Test
    void sessionCannotBeRenewedAtItsMaximumLifetimeAndNothingChanges() {
        StoredSession stored = loggedIn();
        clock.setInstant(stored.session().expiresAt());

        assertThatThrownBy(() -> renew(stored.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().isRevoked()).isFalse();
        assertThat(sessions.refreshToken(RefreshTokenHash.of(stored.refreshToken())).orElseThrow().isConsumed())
                .isFalse();
    }

    @Test
    void renewalJustBeforeTheMaximumCapsTheAccessTokenAtTheSessionEnd() {
        StoredSession stored = loggedIn();
        clock.setInstant(stored.session().expiresAt().minus(Duration.ofMinutes(5)));

        SessionTokens renewed = renew(stored.refreshToken());

        assertThat(renewed.accessTokenExpiresAt()).isEqualTo(stored.session().expiresAt());
    }

    @Test
    void rotationsNeverExtendTheSessionMaximum() {
        StoredSession stored = loggedIn();
        String token = stored.refreshToken();
        for (int day : new int[] {9, 18, 27}) {
            clock.setInstant(stored.session().establishedAt().plus(Duration.ofDays(day)));
            SessionTokens renewed = renew(token);
            assertThat(renewed.sessionExpiresAt()).isEqualTo(stored.session().expiresAt());
            token = renewed.refreshToken();
        }

        clock.setInstant(stored.session().expiresAt());
        String lastToken = token;
        assertThatThrownBy(() -> renew(lastToken)).isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void loggedOutSessionCannotBeRenewedAndKeepsItsLogoutReason() {
        StoredSession stored = loggedIn();
        sessions.revoke(stored.session().id(), accountId, RevocationReason.LOGOUT, clock.instant());

        assertThatThrownBy(() -> renew(stored.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().revocationReason())
                .isEqualTo(RevocationReason.LOGOUT);
    }

    @Test
    void missingRefreshTokenIsAValidationError() {
        assertThatThrownBy(() -> renew(" "))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations())
                                .containsExactly(new FieldViolation("refreshToken", ValueProblem.REQUIRED)));
    }

    private StoredSession loggedIn() {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), accountId, clock.instant());
        String token = "initial-" + session.id();
        sessions.create(session, RefreshTokenHash.of(token));
        return new StoredSession(session, token);
    }

    private SessionTokens renew(String refreshToken) {
        return service.renew(new RenewSessionCommand(refreshToken));
    }
}