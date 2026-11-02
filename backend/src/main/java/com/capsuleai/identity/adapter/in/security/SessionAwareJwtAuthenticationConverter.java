package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.adapter.out.security.JwtAccessTokenIssuer;
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Turns an already signature- and expiry-checked JWT into an authenticated
 * account, but ONLY if persistent session authority says the session is usable
 * now (FR-AUTH-009, ADA-008). A valid signature alone is never enough.
 */
public final class SessionAwareJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SessionAuthority sessionAuthority;

    public SessionAwareJwtAuthenticationConverter(SessionAuthority sessionAuthority) {
        this.sessionAuthority = Objects.requireNonNull(sessionAuthority, "sessionAuthority");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID accountId = requiredUuid(jwt.getSubject());
        UUID sessionId = requiredUuid(jwt.getClaimAsString(JwtAccessTokenIssuer.SESSION_ID_CLAIM));
        AuthenticatedAccount account = sessionAuthority.findUsableSession(accountId, sessionId)
                .orElseThrow(() -> new InvalidBearerTokenException("The login session is no longer usable"));
        return new AuthenticatedAccountToken(account);
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