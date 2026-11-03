package com.capsuleai.identity.domain;

/** What a renewal attempt must do (FR-AUTH-013, FR-AUTH-014, BRULE-AUTH-006). */
public enum RenewalDecision {

    /** Consume the presented token and issue replacements. */
    ROTATE,

    /** The presented token was already consumed: revoke the affected session; renew nothing. */
    REVOKE_SESSION_FOR_REUSE,

    /** The session is revoked or past its maximum lifetime: renew nothing, change nothing. */
    REJECT
}