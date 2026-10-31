package com.capsuleai.identity.adapter.out.security;

import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.domain.PasswordHash;
import java.util.Map;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

/**
 * PBKDF2-HMAC-SHA256 password hashing.
 *
 * <p>BCrypt is deliberately NOT used: it ignores everything after the first
 * 72 bytes, which would break exact comparison of long Unicode passwords
 * (BRULE-AUTH-003, US-AUTH-001 AC-05).
 */
public final class Pbkdf2PasswordHasher implements PasswordHasher {

    static final String ENCODING_ID = "pbkdf2-sha256-v1";

    private static final int SALT_LENGTH_BYTES = 16;
    private static final int ITERATIONS = 600_000;

    private final PasswordEncoder encoder;

    public Pbkdf2PasswordHasher() {
        Pbkdf2PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder(
                "", SALT_LENGTH_BYTES, ITERATIONS, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        this.encoder = new DelegatingPasswordEncoder(ENCODING_ID, Map.of(ENCODING_ID, pbkdf2));
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        return new PasswordHash(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash storedHash) {
        return encoder.matches(rawPassword, storedHash.value());
    }
}