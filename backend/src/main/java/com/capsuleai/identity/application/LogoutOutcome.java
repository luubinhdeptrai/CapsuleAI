package com.capsuleai.identity.application;

/** Result of a logout request for the current session. */
public enum LogoutOutcome {

    /** This request revoked the session. */
    REVOKED,

    /**
     * Nothing was revoked: the session was already revoked (for example by a
     * concurrent request) or is not this account's session.
     */
    NOTHING_TO_REVOKE
}