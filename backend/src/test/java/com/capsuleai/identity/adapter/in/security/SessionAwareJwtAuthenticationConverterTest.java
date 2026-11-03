package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionAwareJwtAuthenticationConverterTest {

    private final UUID accountId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    @Test
    void usableSessionBecomesTheAuthenticatedPrincipal() {
        AuthenticatedAccount expected = new AuthenticatedAccount(accountId, sessionId);

        AbstractAuthenticationToken authentication =
                converter((requestedAccountId, requestedSessionId) -> Optional.of(expected)).convert(jwt());

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(expected);
    }

    @Test
    void sessionAuthorityIsAskedAboutTheTokensAccountAndSession() {
        SessionAuthority usableOnlyForTheTokenClaims = (requestedAccountId, requestedSessionId) ->
                requestedAccountId.equals(accountId) && requestedSessionId.equals(sessionId)
                        ? Optional.of(new AuthenticatedAccount(requestedAccountId, requestedSessionId))
                        : Optional.empty();

        AbstractAuthenticationToken authentication = converter(usableOnlyForTheTokenClaims).convert(jwt());

        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedAccount(accountId, sessionId));
    }

    @Test
    void unusableSessionIsAnInvalidTokenNotAnOutage() {
        assertThatThrownBy(() -> converter((requestedAccountId, requestedSessionId) -> Optional.empty())
                .convert(jwt()))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void tokenWithoutTheSessionClaimIsAnInvalidToken() {
        Jwt withoutSessionClaim = Jwt.withTokenValue("token-value")
                .header("alg", "HS256")
                .subject(accountId.toString())
                .build();

        assertThatThrownBy(() -> converter((requestedAccountId, requestedSessionId) -> Optional.empty())
                .convert(withoutSessionClaim))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void unobtainableDatabaseConnectionIsReportedAsUnavailableAuthority() {
        SessionAuthority unreachableSessionAuthority = (requestedAccountId, requestedSessionId) -> {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection");
        };

        assertThatThrownBy(() -> converter(unreachableSessionAuthority).convert(jwt()))
                .isInstanceOf(SessionAuthorityUnavailableException.class)
                .isNotInstanceOf(AuthenticationServiceException.class)
                .hasCauseInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageNotContaining("JDBC");
    }

    @Test
    void transientResourceFailureIsAlsoReportedAsUnavailableAuthority() {
        SessionAuthority interruptedSessionAuthority = (requestedAccountId, requestedSessionId) -> {
            throw new TransientDataAccessResourceException("Connection reset");
        };

        assertThatThrownBy(() -> converter(interruptedSessionAuthority).convert(jwt()))
                .isInstanceOf(SessionAuthorityUnavailableException.class);
    }

    @Test
    void programmingErrorsAreNotDisguisedAsOutages() {
        SessionAuthority brokenSessionAuthority = (requestedAccountId, requestedSessionId) -> {
            throw new BadSqlGrammarException("find session", "SELECT nonsense", new SQLException("syntax error"));
        };

        assertThatThrownBy(() -> converter(brokenSessionAuthority).convert(jwt()))
                .isInstanceOf(BadSqlGrammarException.class);
    }

    private static SessionAwareJwtAuthenticationConverter converter(SessionAuthority sessionAuthority) {
        return new SessionAwareJwtAuthenticationConverter(sessionAuthority);
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token-value")
                .header("alg", "HS256")
                .subject(accountId.toString())
                .claim(JwtClaimNames.SESSION_ID, sessionId.toString())
                .issuedAt(Instant.parse("2026-01-05T08:00:00Z"))
                .expiresAt(Instant.parse("2026-01-05T08:15:00Z"))
                .build();
    }
}
