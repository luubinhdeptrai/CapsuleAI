package com.capsuleai.identity.adapter.in.web;

/**
 * The one response to every accepted Forgot Password request, whether or not an
 * account exists (FR-AUTH-003). It never claims that a password changed.
 */
public record PasswordResetRequestedResponse(String code, String detail) {

    static final PasswordResetRequestedResponse FOR_EVERY_EMAIL = new PasswordResetRequestedResponse(
            "PASSWORD_RESET_REQUESTED",
            "If an account exists for this email, password-reset instructions have been sent.");
}