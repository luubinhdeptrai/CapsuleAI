package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.SessionTokens;
import java.util.UUID;

/** JSON body returned by login (and later by renewal). toString() masks the tokens. */
public record TokenResponse(
        String tokenType,
        String accessToken,
        String accessTokenExpiresAt,
        String refreshToken,
        String sessionExpiresAt,
        UUID accountId) {

    static TokenResponse from(SessionTokens tokens) {
        return new TokenResponse(
                "Bearer",
                tokens.accessToken(),
                tokens.accessTokenExpiresAt().toString(),
                tokens.refreshToken(),
                tokens.sessionExpiresAt().toString(),
                tokens.accountId());
    }

    @Override
    public String toString() {
        return "TokenResponse[tokenType=" + tokenType + ", accessToken=[PROTECTED], accessTokenExpiresAt="
                + accessTokenExpiresAt + ", refreshToken=[PROTECTED], sessionExpiresAt=" + sessionExpiresAt
                + ", accountId=" + accountId + "]";
    }
}