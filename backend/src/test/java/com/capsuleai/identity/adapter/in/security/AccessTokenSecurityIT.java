package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.configuration.AccessTokenProperties;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccessTokenSecurityIT extends IdentityIntegrationTestSupport {

    /** Wire name of the login-session claim; kept literal so the test pins the token contract. */
    private static final String SESSION_ID_CLAIM = "sid";

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Autowired
    private AccessTokenProperties accessTokenProperties;

    @Test
    void usableAccessTokenReachesAProtectedOperationAsTheCorrectAccount() throws Exception {
        IssuedTokens tokens = registerAndLogIn("owner@example.com");

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(tokens.accountId().toString()));
    }

    @Test
    void eachAccountIsResolvedOnlyToItself() throws Exception {
        IssuedTokens first = registerAndLogIn("first@example.com");
        IssuedTokens second = registerAndLogIn("second@example.com");

        getProtectedProbe(first.accessToken())
                .andExpect(jsonPath("$.accountId").value(first.accountId().toString()));
        getProtectedProbe(second.accessToken())
                .andExpect(jsonPath("$.accountId").value(second.accountId().toString()));
    }

    @Test
    void requestWithoutAnAccessTokenIsRejectedWithoutContent() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.accountId").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-jwt", "eyJhbGciOiJIUzI1NiJ9.e30.not-a-valid-signature"})
    void malformedAccessTokensAreRejected(String token) throws Exception {
        getProtectedProbe(token)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        IssuedTokens tokens = registerAndLogIn("forged@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        String forged = sign(randomKey(), "capsuleai-identity", tokens.accountId(), sessionId);

        getProtectedProbe(forged).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFromAnotherIssuerIsRejectedEvenWithTheRealKey() throws Exception {
        IssuedTokens tokens = registerAndLogIn("issuer@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        String foreign = sign(accessTokenProperties.signingKey(), "someone-else", tokens.accountId(), sessionId);

        getProtectedProbe(foreign).andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenExpiresExactlyFifteenMinutesAfterIssue() throws Exception {
        IssuedTokens tokens = registerAndLogIn("expiry@example.com");

        clock.advance(Duration.ofMinutes(15).minusSeconds(1));
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());

        clock.advance(Duration.ofSeconds(1));
        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void validlySignedTokenWithoutPersistedSessionIsRejected() throws Exception {
        UUID accountId = registerAccount("no-session@example.com", PASSWORD);
        Instant now = clock.instant();

        String token = accessTokenIssuer.issue(accountId, UUID.randomUUID(), now, now.plus(Duration.ofMinutes(15)));

        getProtectedProbe(token).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionPresentedWithAnotherAccountIsRejected() throws Exception {
        IssuedTokens victim = registerAndLogIn("victim@example.com");
        IssuedTokens attacker = registerAndLogIn("attacker@example.com");
        UUID victimSession = sessionIdOf(victim.accessToken());
        Instant now = clock.instant();

        String mixed = accessTokenIssuer.issue(attacker.accountId(), victimSession, now, now.plus(Duration.ofMinutes(15)));

        getProtectedProbe(mixed).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionAuthorityIsReadFromPostgreSqlOnEveryRequest() throws Exception {
        IssuedTokens tokens = registerAndLogIn("db-authority@example.com");
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());

        // Shorten the stored session to one second and move past it.
        jdbc.sql("UPDATE identity_session SET expires_at = established_at + INTERVAL '1 second'").update();
        clock.advance(Duration.ofSeconds(1));

        // The JWT alone is still valid for ~15 minutes, but persistent authority says no.
        getProtectedProbe(tokens.accessToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownRoutesAreProtectedByDefault() throws Exception {
        mockMvc.perform(get("/api/v1/wardrobe/garments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    private static SecretKey randomKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    private String sign(SecretKey key, String issuer, UUID accountId, UUID sessionId) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(accountId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .claim(SESSION_ID_CLAIM, sessionId.toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return NimbusJwtEncoder.withSecretKey(key)
                .algorithm(MacAlgorithm.HS256)
                .build()
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
