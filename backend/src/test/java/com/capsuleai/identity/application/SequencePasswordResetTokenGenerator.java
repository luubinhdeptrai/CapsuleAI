package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetTokenGenerator;

/** Predictable reset secrets for application tests: reset-token-1, reset-token-2, ... */
final class SequencePasswordResetTokenGenerator implements PasswordResetTokenGenerator {

    private int issued;

    @Override
    public synchronized String newToken() {
        issued++;
        return "reset-token-" + issued;
    }
}
