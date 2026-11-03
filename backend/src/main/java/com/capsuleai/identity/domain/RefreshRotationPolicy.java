package com.capsuleai.identity.domain;

import java.time.Instant;

/**
 * Decides what a renewal with a presented refresh token must do
 * (FR-AUTH-010, FR-AUTH-013, FR-AUTH-014, BRULE-AUTH-006).
 */
public final class RefreshRotationPolicy {

    private RefreshRotationPolicy() {
    }

    public static RenewalDecision decide(LoginSession session, RefreshToken presented, Instant now) {
        if (!presented.sessionId().equals(session.id())) {
            throw new IllegalArgumentException("The refresh token does not belong to this session");
        }
        if (!session.isUsableAt(now)) {
            return RenewalDecision.REJECT;
        }
        if (presented.isConsumed()) {
            return RenewalDecision.REVOKE_SESSION_FOR_REUSE;
        }
        return RenewalDecision.ROTATE;
    }
}