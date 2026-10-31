package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.PasswordHash;

/**
 * One-way, salted password protection (DATA-AUTH-001).
 * Implementations use the raw value exactly as given, without trimming or normalizing.
 */
public interface PasswordHasher {

    PasswordHash hash(String rawPassword);

    boolean matches(String rawPassword, PasswordHash storedHash);
}