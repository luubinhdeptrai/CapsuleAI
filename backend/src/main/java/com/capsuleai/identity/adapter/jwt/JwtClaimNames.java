package com.capsuleai.identity.adapter.jwt;

/**
 * Names of Identity's custom access-token claims: the shared wire format of
 * the token issuer (adapter.out.security) and the token reader
 * (adapter.in.security). Neither adapter imports the other; both use this.
 *
 * <p>Internal to Identity and deliberately not part of the published
 * {@code api}: other modules receive {@code AuthenticatedAccount}, never JWT claims.
 */
public final class JwtClaimNames {

    /** Claim carrying the login-session id. */
    public static final String SESSION_ID = "sid";

    private JwtClaimNames() {
    }
}
