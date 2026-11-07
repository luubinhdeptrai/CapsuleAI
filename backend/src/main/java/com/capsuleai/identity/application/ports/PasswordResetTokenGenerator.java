package com.capsuleai.identity.application.ports;

/** Creates new, unguessable password-reset secrets. */
public interface PasswordResetTokenGenerator {

    String newToken();
}