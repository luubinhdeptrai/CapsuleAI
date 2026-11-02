package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogOutServiceTest {

    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final LogOutService service = new LogOutService(sessions, clock);
    private final UUID accountId = UUID.randomUUID();

    @Test
    void revokesTheCurrentSessionAtTheCurrentTime() {
        LoginSession current = storedSession(accountId);
        clock.advance(Duration.ofMinutes(5));

        LogoutOutcome outcome = service.logOut(new AuthenticatedAccount(accountId, current.id()));

        assertThat(outcome).isEqualTo(LogoutOutcome.REVOKED);
        LoginSession stored = sessions.findById(current.id()).orElseThrow();
        assertThat(stored.revokedAt()).isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofMinutes(5)));
        assertThat(stored.revocationReason()).isEqualTo(RevocationReason.LOGOUT);
        assertThat(stored.isUsableAt(clock.instant())).isFalse();
    }

    @Test
    void leavesAnotherSessionOfTheSameAccountUsable() {
        LoginSession deviceA = storedSession(accountId);
        LoginSession deviceB = storedSession(accountId);

        service.logOut(new AuthenticatedAccount(accountId, deviceA.id()));

        assertThat(sessions.findById(deviceB.id()).orElseThrow().isUsableAt(clock.instant())).isTrue();
    }

    @Test
    void repeatedLogoutFindsNothingToRevokeAndKeepsTheFirstRevocation() {
        LoginSession current = storedSession(accountId);
        AuthenticatedAccount principal = new AuthenticatedAccount(accountId, current.id());
        service.logOut(principal);
        clock.advance(Duration.ofMinutes(1));

        assertThat(service.logOut(principal)).isEqualTo(LogoutOutcome.NOTHING_TO_REVOKE);
        assertThat(sessions.findById(current.id()).orElseThrow().revokedAt()).isEqualTo(MutableClock.DEFAULT_START);
    }

    @Test
    void neverRevokesASessionOfAnotherAccount() {
        LoginSession someoneElses = storedSession(accountId);

        LogoutOutcome outcome = service.logOut(new AuthenticatedAccount(UUID.randomUUID(), someoneElses.id()));

        assertThat(outcome).isEqualTo(LogoutOutcome.NOTHING_TO_REVOKE);
        assertThat(sessions.findById(someoneElses.id()).orElseThrow().isRevoked()).isFalse();
    }

    private LoginSession storedSession(UUID owner) {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), owner, clock.instant());
        sessions.create(session, RefreshTokenHash.of("refresh-" + session.id()));
        return session;
    }
}