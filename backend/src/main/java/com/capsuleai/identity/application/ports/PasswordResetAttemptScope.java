package com.capsuleai.identity.application.ports;

/**
 * What a password-reset attempt is counted against (FR-AUTH-019).
 *
 * <p>The subject of {@link #REQUEST_PER_EMAIL} is the normalized email address; the
 * subject of the other scopes is the client network address. Counting a request
 * against its email happens whether or not that email is registered.
 */
public enum PasswordResetAttemptScope {

    /** Forgot Password requests for one normalized email address (mailbox-flooding protection). */
    REQUEST_PER_EMAIL,

    /** Forgot Password requests from one client network address. */
    REQUEST_PER_CLIENT,

    /** Link checks and reset completions from one client network address. */
    VERIFICATION_PER_CLIENT
}