package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.domain.PasswordHash;
import java.util.ArrayList;
import java.util.List;

/** Fast fake hasher that records the exact values it was asked to hash. */
final class RecordingPasswordHasher implements PasswordHasher {

    private static final String PREFIX = "test-hash:";

    private final List<String> hashedValues = new ArrayList<>();

    @Override
    public synchronized PasswordHash hash(String rawPassword) {
        hashedValues.add(rawPassword);
        return new PasswordHash(PREFIX + rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash storedHash) {
        return storedHash.value().equals(PREFIX + rawPassword);
    }

    synchronized List<String> hashedValues() {
        return List.copyOf(hashedValues);
    }
}