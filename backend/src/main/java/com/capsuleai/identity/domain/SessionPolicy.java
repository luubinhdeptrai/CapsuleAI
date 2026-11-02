package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;

/** Fixed session policy (SRS §3.1.1, FR-AUTH-010). */
public final class SessionPolicy {

    public static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    public static final Duration SESSION_MAXIMUM_LIFETIME = Duration.ofDays(30);

    private SessionPolicy() {
    }

    /** An access token lives 15 minutes, but never longer than its login session. */
    public static Instant accessTokenExpiresAt(Instant issuedAt, Instant sessionExpiresAt) {
        Instant byLifetime = issuedAt.plus(ACCESS_TOKEN_LIFETIME);
        return byLifetime.isBefore(sessionExpiresAt) ? byLifetime : sessionExpiresAt;
    }
}