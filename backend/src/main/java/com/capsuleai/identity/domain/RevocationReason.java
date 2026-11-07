package com.capsuleai.identity.domain;

/** Why a login session stopped being usable. Stored by name in the database. */
public enum RevocationReason {

    /** The user ended this session (US-AUTH-004, FR-AUTH-007). */
    LOGOUT,

    /** A consumed refresh token was presented again (US-AUTH-003, FR-AUTH-014). */
    REFRESH_TOKEN_REUSE,

    /** An accepted password reset revoked every active session of the account (US-AUTH-005, FR-AUTH-004). */
    PASSWORD_RESET
}