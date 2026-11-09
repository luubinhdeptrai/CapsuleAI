package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;

/** Fixed password-reset policy (SRS §3.1.1, FR-AUTH-017, BRULE-AUTH-004). */
public final class PasswordResetPolicy {

    /** A reset interaction can change the password only strictly before this much time has passed. */
    public static final Duration INTERACTION_LIFETIME = Duration.ofMinutes(30);

    /**
     * A consumed, replaced or expired interaction is deleted this long after it ended
     * (Product Owner decision OPEN-S2-07, DATA-AUTH-006).
     */
    public static final Duration RECORD_RETENTION = Duration.ofDays(30);

    private PasswordResetPolicy() {
    }

    public static Instant expiresAt(Instant issuedAt) {
        return issuedAt.plus(INTERACTION_LIFETIME);
    }
}