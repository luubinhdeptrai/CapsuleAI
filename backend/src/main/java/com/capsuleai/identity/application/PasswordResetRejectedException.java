package com.capsuleai.identity.application;

/**
 * The presented reset interaction cannot change the password: it is unknown,
 * expired, already used or replaced (ERR-AUTH-002). Nothing was changed.
 * Deliberately does not say which case applied.
 */
public final class PasswordResetRejectedException extends RuntimeException {

    public PasswordResetRejectedException() {
        super("The password-reset interaction is not usable");
    }
}