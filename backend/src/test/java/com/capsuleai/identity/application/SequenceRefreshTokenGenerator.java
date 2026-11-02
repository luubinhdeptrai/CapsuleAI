package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.RefreshTokenGenerator;

/** Deterministic, predictable refresh tokens for application tests (never use outside tests). */
final class SequenceRefreshTokenGenerator implements RefreshTokenGenerator {

    private int next;

    @Override
    public synchronized String newToken() {
        next++;
        return "refresh-token-" + next;
    }
}
