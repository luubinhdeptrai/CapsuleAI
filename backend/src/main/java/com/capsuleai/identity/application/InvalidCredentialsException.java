package com.capsuleai.identity.application;

/**
 * The email/password pair did not authenticate an account.
 * Deliberately does not say whether the email or the password was wrong.
 */
public final class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}