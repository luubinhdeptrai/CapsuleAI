package com.capsuleai.identity.adapter.out.security;

import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;

/** 256-bit random refresh tokens, Base64url-encoded without padding (43 characters). */
public final class SecureRandomRefreshTokenGenerator implements RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}