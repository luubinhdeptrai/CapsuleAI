package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.MutableClock;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionAuthorityServiceTest {

    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final SessionAuthorityService authority = new SessionAuthorityService(sessions, clock);
    private final UUID accountId = UUID.randomUUID();

    @Test
    void usableSessionAuthenticatesItsAccount() {
        LoginSession session = storedSession();

        assertThat(authority.findUsableSession(accountId, session.id()))
                .contains(new AuthenticatedAccount(accountId, session.id()));
    }

    @Test
    void sessionPresentedForAnotherAccountIsNotAccepted() {
        LoginSession session = storedSession();

        assertThat(authority.findUsableSession(UUID.randomUUID(), session.id())).isEmpty();
    }

    @Test
    void unknownSessionIsNotAccepted() {
        assertThat(authority.findUsableSession(accountId, UUID.randomUUID())).isEmpty();
    }

    @Test
    void sessionAtItsMaximumLifetimeIsNotAccepted() {
        LoginSession session = storedSession();

        clock.setInstant(session.expiresAt().minusSeconds(1));
        assertThat(authority.findUsableSession(accountId, session.id())).isPresent();

        clock.setInstant(session.expiresAt());
        assertThat(authority.findUsableSession(accountId, session.id())).isEmpty();
    }

    private LoginSession storedSession() {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), accountId, clock.instant());
        sessions.create(session, RefreshTokenHash.of("refresh-" + session.id()));
        return session;
    }
}
