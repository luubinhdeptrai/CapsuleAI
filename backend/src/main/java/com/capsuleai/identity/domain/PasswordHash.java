package com.capsuleai.identity.domain;

/**
 * An encoded, salted, one-way password hash. Never the password itself.
 */
public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("An encoded password hash is required");
        }
    }

    @Override
    public String toString() {
        return "PasswordHash[PROTECTED]";
    }
}