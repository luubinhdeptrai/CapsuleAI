package com.capsuleai.identity.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Lower-case hex SHA-256 of a refresh token. Only this value is stored (TD-11).
 */
public record RefreshTokenHash(String value) {

    public RefreshTokenHash {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("A lower-case hex SHA-256 digest is required");
        }
    }

    public static RefreshTokenHash of(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            throw new IllegalArgumentException("A refresh token is required");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new RefreshTokenHash(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", impossible);
        }
    }

    @Override
    public String toString() {
        return "RefreshTokenHash[PROTECTED]";
    }
}