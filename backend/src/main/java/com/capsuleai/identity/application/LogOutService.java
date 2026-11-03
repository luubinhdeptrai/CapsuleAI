package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * US-AUTH-004 — revokes ONLY the session the request authenticated with
 * (FR-AUTH-007, BRULE-AUTH-006). Other sessions and all account data are untouched.
 */
public final class LogOutService {

    private final SessionRepository sessions;
    private final Clock clock;

    public LogOutService(SessionRepository sessions, Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public LogoutOutcome logOut(AuthenticatedAccount current) {
        boolean revoked = sessions.revoke(
                current.sessionId(),
                current.accountId(),
                RevocationReason.LOGOUT,
                clock.instant().truncatedTo(ChronoUnit.SECONDS));
        return revoked ? LogoutOutcome.REVOKED : LogoutOutcome.NOTHING_TO_REVOKE;
    }
}