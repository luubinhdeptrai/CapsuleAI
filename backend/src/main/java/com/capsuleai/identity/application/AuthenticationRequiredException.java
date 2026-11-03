package com.capsuleai.identity.application;

/**
 * The login session cannot be renewed: unknown token, revoked or expired session,
 * or reuse detected. The caller must authenticate again. Deliberately does not
 * say which case applied.
 */
public final class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Authentication is required again");
    }
}