package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import com.capsuleai.identity.application.ports.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Answers the published {@link SessionAuthority} question from persistent session state. */
public final class SessionAuthorityService implements SessionAuthority {

    private final SessionRepository sessions;
    private final Clock clock;

    public SessionAuthorityService(SessionRepository sessions, Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public Optional<AuthenticatedAccount> findUsableSession(UUID accountId, UUID sessionId) {
        Instant now = clock.instant();
        return sessions.findById(sessionId)
                .filter(session -> session.belongsTo(accountId))
                .filter(session -> session.isUsableAt(now))
                .map(session -> new AuthenticatedAccount(session.accountId(), session.id()));
    }
}