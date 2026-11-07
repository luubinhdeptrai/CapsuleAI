package com.capsuleai.identity.application;

/**
 * Input of a Forgot Password request: the email exactly as the user typed it, and the
 * network address of the client, used only for attempt limiting (FR-AUTH-019).
 */
public record RequestPasswordResetCommand(String email, String clientAddress) {
}