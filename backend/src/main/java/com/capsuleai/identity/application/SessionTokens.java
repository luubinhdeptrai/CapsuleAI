package com.capsuleai.identity.application;

import java.time.Instant;
import java.util.UUID;

/** Credentials issued for a login session. toString() masks both tokens. */
public record SessionTokens(
        UUID accountId,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant sessionExpiresAt) {

    @Override
    public String toString() {
        return "SessionTokens[accountId=" + accountId
                + ", accessToken=[PROTECTED], accessTokenExpiresAt=" + accessTokenExpiresAt
                + ", refreshToken=[PROTECTED], sessionExpiresAt=" + sessionExpiresAt + "]";
    }
}