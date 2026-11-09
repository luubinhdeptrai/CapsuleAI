package com.capsuleai.identity.adapter.in.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Persistent session authority could not be checked because the database is
 * unreachable. Authentication fails (fail closed); the entry point answers 503
 * rather than 401, because the client's credentials were not found invalid.
 *
 * <p>Deliberately not an {@code AuthenticationServiceException}: Spring
 * Security rethrows that type as a server error instead of calling the entry point.
 */
public final class SessionAuthorityUnavailableException extends AuthenticationException {

    public SessionAuthorityUnavailableException(Throwable cause) {
        super("Session authority is temporarily unavailable", cause);
    }
}
