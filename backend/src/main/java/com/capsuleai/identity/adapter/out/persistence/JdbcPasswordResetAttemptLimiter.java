package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * PostgreSQL attempt counters (FR-AUTH-019). One row per scope, hashed subject and window;
 * the upsert increments it atomically, so concurrent attempts each get a distinct count
 * without an application lock. The subject (an email address or client address) is stored
 * only as its SHA-256 hash, and rows are deleted a day after their window starts.
 */
public final class JdbcPasswordResetAttemptLimiter implements PasswordResetAttemptLimiter {

    private final JdbcClient jdbc;

    public JdbcPasswordResetAttemptLimiter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int recordAttempt(PasswordResetAttemptScope scope, String subject, Instant windowStart) {
        return jdbc.sql("""
                        INSERT INTO identity.identity_password_reset_attempt AS a
                               (scope, subject_hash, window_start, attempts)
                        VALUES (:scope, :subjectHash, :windowStart, 1)
                        ON CONFLICT (scope, subject_hash, window_start)
                        DO UPDATE SET attempts = a.attempts + 1
                        RETURNING a.attempts
                        """)
                .param("scope", scope.name())
                .param("subjectHash", sha256(subject))
                .param("windowStart", JdbcTime.toDatabase(windowStart))
                .query(Integer.class)
                .single();
    }

    @Override
    public int forgetWindowsStartedBefore(Instant cutoff) {
        return jdbc.sql("DELETE FROM identity.identity_password_reset_attempt WHERE window_start < :cutoff")
                .param("cutoff", JdbcTime.toDatabase(cutoff))
                .update();
    }

    static String sha256(String subject) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(subject.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", impossible);
        }
    }
}