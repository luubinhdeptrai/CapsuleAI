package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshRotationPolicy;
import com.capsuleai.identity.domain.RefreshToken;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.domain.SessionPolicy;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * US-AUTH-003 — renews access with a rotating refresh token inside the login
 * session's absolute 30-day lifetime. A reused (consumed) token revokes the
 * affected session (FR-AUTH-005/006/010/013/014, BRULE-AUTH-006).
 */
public final class RefreshSessionService {

    private final SessionRepository sessions;
    private final RefreshTokenGenerator refreshTokens;
    private final AccessTokenIssuer accessTokens;
    private final IdentityTransactions transactions;
    private final Clock clock;

    public RefreshSessionService(SessionRepository sessions,
                                 RefreshTokenGenerator refreshTokens,
                                 AccessTokenIssuer accessTokens,
                                 IdentityTransactions transactions,
                                 Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.refreshTokens = Objects.requireNonNull(refreshTokens, "refreshTokens");
        this.accessTokens = Objects.requireNonNull(accessTokens, "accessTokens");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws RequestValidationException      if no refresh token was supplied
     * @throws AuthenticationRequiredException if the session cannot be renewed; a
     *         detected reuse has already been committed as a revocation when thrown
     */
    public SessionTokens renew(RenewSessionCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            throw new RequestValidationException(
                    List.of(new FieldViolation("refreshToken", ValueProblem.REQUIRED)));
        }
        RefreshTokenHash presented = RefreshTokenHash.of(command.refreshToken());
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        String replacementToken = refreshTokens.newToken();

        // The transaction commits successful rotations AND reuse revocations.
        // A refusal is signalled by an empty result, never by throwing inside the
        // transaction, because an exception would roll the revocation back.
        Optional<SessionTokens> renewed =
                transactions.inTransaction(() -> renewInsideTransaction(presented, replacementToken, now));

        return renewed.orElseThrow(AuthenticationRequiredException::new);
    }

    private Optional<SessionTokens> renewInsideTransaction(
            RefreshTokenHash presented, String replacementToken, Instant now) {
        Optional<LockedRefreshCredential> locked = sessions.lockByRefreshToken(presented);
        if (locked.isEmpty()) {
            return Optional.empty();
        }
        LoginSession session = locked.get().session();
        RefreshToken token = locked.get().token();

        return switch (RefreshRotationPolicy.decide(session, token, now)) {
            case ROTATE -> Optional.of(rotate(session, token, replacementToken, now));
            case REVOKE_SESSION_FOR_REUSE -> {
                sessions.revoke(session.id(), session.accountId(), RevocationReason.REFRESH_TOKEN_REUSE, now);
                yield Optional.empty();
            }
            case REJECT -> Optional.empty();
        };
    }

    private SessionTokens rotate(LoginSession session, RefreshToken current, String replacementToken, Instant now) {
        // Consume first: the database allows only one current token per session.
        sessions.consumeRefreshToken(current.id(), now);
        sessions.addRefreshToken(session.id(), RefreshTokenHash.of(replacementToken), now);

        Instant accessTokenExpiresAt = SessionPolicy.accessTokenExpiresAt(now, session.expiresAt());
        String accessToken = accessTokens.issue(session.accountId(), session.id(), now, accessTokenExpiresAt);

        return new SessionTokens(
                session.accountId(), accessToken, accessTokenExpiresAt, replacementToken, session.expiresAt());
    }
}