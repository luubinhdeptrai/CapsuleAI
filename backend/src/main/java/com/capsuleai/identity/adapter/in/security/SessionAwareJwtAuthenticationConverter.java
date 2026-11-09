package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Turns an already signature- and expiry-checked JWT into an authenticated
 * account, but ONLY if persistent session authority says the session is usable
 * now (FR-AUTH-009, ADA-008). A valid signature alone is never enough.
 *
 * <p>Fail closed in every outcome other than "usable": an unusable session is an
 * invalid token (401); an unreachable database is an authority outage (503); any
 * other exception propagates (500).
 */
public final class SessionAwareJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(SessionAwareJwtAuthenticationConverter.class);

    private final SessionAuthority sessionAuthority;

    public SessionAwareJwtAuthenticationConverter(SessionAuthority sessionAuthority) {
        this.sessionAuthority = Objects.requireNonNull(sessionAuthority, "sessionAuthority");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID accountId = requiredUuid(jwt.getSubject());
        UUID sessionId = requiredUuid(jwt.getClaimAsString(JwtClaimNames.SESSION_ID));
        AuthenticatedAccount authenticatedAccount = findUsableSession(accountId, sessionId)
                .orElseThrow(() -> new InvalidBearerTokenException("The login session is no longer usable"));
        return new AuthenticatedAccountToken(authenticatedAccount);
    }

    /**
     * Only a confirmed "database cannot be reached" failure is translated. Any
     * other exception (for example invalid SQL) is a programming error and is
     * left to propagate as a 500. In both cases no authentication is created.
     */
    private Optional<AuthenticatedAccount> findUsableSession(UUID accountId, UUID sessionId) {
        try {
            return sessionAuthority.findUsableSession(accountId, sessionId);
        } catch (DataAccessResourceFailureException | TransientDataAccessResourceException databaseUnreachable) {
            log.error("Session authority check failed because the database is unreachable", databaseUnreachable);
            throw new SessionAuthorityUnavailableException(databaseUnreachable);
        }
    }

    private static UUID requiredUuid(String claimValue) {
        if (claimValue == null) {
            throw new InvalidBearerTokenException("The access token is missing a required claim");
        }
        try {
            return UUID.fromString(claimValue);
        } catch (IllegalArgumentException malformed) {
            throw new InvalidBearerTokenException("The access token has a malformed claim");
        }
    }
}