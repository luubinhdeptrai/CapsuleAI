package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class SessionRefreshApiIT extends IdentityIntegrationTestSupport {

    private static final String REFRESH = "/api/v1/auth/refresh";
    private static final String LOGOUT = "/api/v1/auth/logout";

    @Test
    void expiredAccessIsRenewedForTheSameAccountWithoutCredentials() throws Exception {
        IssuedTokens login = registerAndLogIn("person@example.com");
        getProtectedProbe(login.accessToken()).andExpect(status().isOk());

        clock.advance(Duration.ofMinutes(15));
        getProtectedProbe(login.accessToken()).andExpect(status().isUnauthorized());

        IssuedTokens renewed = renew(login.refreshToken());

        assertThat(renewed.accountId()).isEqualTo(login.accountId());
        getProtectedProbe(renewed.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(login.accountId().toString()));
        assertThat(countRows("identity.identity_account")).isOne();
    }

    @Test
    void renewalConsumesTheOldRefreshTokenAndIssuesReplacements() throws Exception {
        IssuedTokens login = registerAndLogIn("rotate@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        clock.advance(Duration.ofMinutes(20));

        IssuedTokens renewed = tokensFrom(refresh(login.refreshToken())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessTokenExpiresAt").value("2026-01-05T08:35:00Z"))
                .andExpect(jsonPath("$.sessionExpiresAt").value("2026-02-04T08:00:00Z")));

        assertThat(renewed.refreshToken()).isNotEqualTo(login.refreshToken());
        assertThat(renewed.accessToken()).isNotEqualTo(login.accessToken());
        assertThat(isConsumed(login.refreshToken())).isTrue();
        assertThat(isConsumed(renewed.refreshToken())).isFalse();
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM identity.identity_refresh_token
                        WHERE session_id = :id AND consumed_at IS NULL
                        """)
                .param("id", sessionId).query(Long.class).single()).isOne();
        assertThat(sessionIdOf(renewed.accessToken())).isEqualTo(sessionId);
    }

    @Test
    void consumedTokenReuseRevokesOnlyThatSessionIncludingItsNewestTokens() throws Exception {
        registerAccount("reuse@example.com", PASSWORD);
        IssuedTokens deviceA = logIn("reuse@example.com", PASSWORD);
        IssuedTokens deviceB = logIn("reuse@example.com", PASSWORD);
        UUID sessionA = sessionIdOf(deviceA.accessToken());
        IssuedTokens renewedA = renew(deviceA.refreshToken());

        refresh(deviceA.refreshToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        assertThat(revocationReason(sessionA)).isEqualTo("REFRESH_TOKEN_REUSE");
        refresh(renewedA.refreshToken()).andExpect(status().isUnauthorized());
        getProtectedProbe(renewedA.accessToken()).andExpect(status().isUnauthorized());

        IssuedTokens renewedB = renew(deviceB.refreshToken());
        getProtectedProbe(renewedB.accessToken()).andExpect(status().isOk());
    }

    @Test
    void sessionMaximumIsAbsoluteAcrossRotations() throws Exception {
        IssuedTokens login = registerAndLogIn("maximum@example.com");
        Instant established = MutableClock.DEFAULT_START;
        Instant sessionEnd = established.plus(Duration.ofDays(30));

        String refreshToken = login.refreshToken();
        for (int day : new int[] {10, 20}) {
            clock.setInstant(established.plus(Duration.ofDays(day)));
            IssuedTokens renewed = tokensFrom(refresh(refreshToken)
                    .andExpect(jsonPath("$.sessionExpiresAt").value(sessionEnd.toString())));
            refreshToken = renewed.refreshToken();
        }

        clock.setInstant(sessionEnd.minusSeconds(1));
        IssuedTokens lastRenewal = tokensFrom(refresh(refreshToken)
                .andExpect(jsonPath("$.accessTokenExpiresAt").value(sessionEnd.toString())));
        getProtectedProbe(lastRenewal.accessToken()).andExpect(status().isOk());

        clock.setInstant(sessionEnd);
        refresh(lastRenewal.refreshToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        getProtectedProbe(lastRenewal.accessToken()).andExpect(status().isUnauthorized());
        assertThat(countRows("identity.identity_account")).isOne();
    }

    @Test
    void loggedOutSessionCannotBeRenewed() throws Exception {
        IssuedTokens login = registerAndLogIn("logged-out@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
                .andExpect(status().isNoContent());

        refresh(login.refreshToken()).andExpect(status().isUnauthorized());

        assertThat(revocationReason(sessionId)).isEqualTo("LOGOUT");
        assertThat(isConsumed(login.refreshToken())).isFalse();
    }

    @Test
    void logoutAfterAccessExpiryRenewsFirstThenConfirmsRevocation() throws Exception {
        registerAccount("expired-logout@example.com", PASSWORD);
        IssuedTokens deviceA = logIn("expired-logout@example.com", PASSWORD);
        IssuedTokens deviceB = logIn("expired-logout@example.com", PASSWORD);
        UUID sessionA = sessionIdOf(deviceA.accessToken());
        clock.advance(Duration.ofMinutes(30));

        // The expired access token alone still cannot log out, and that 401 revokes nothing ...
        mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + deviceA.accessToken()))
                .andExpect(status().isUnauthorized());
        assertThat(jdbc.sql("SELECT revoked_at IS NULL FROM identity.identity_session WHERE id = :id")
                .param("id", sessionA).query(Boolean.class).single()).isTrue();

        // ... the final flow renews first and then logs out with the new access token.
        IssuedTokens renewed = renew(deviceA.refreshToken());
        mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + renewed.accessToken()))
                .andExpect(status().isNoContent());

        assertThat(revocationReason(sessionA)).isEqualTo("LOGOUT");
        refresh(renewed.refreshToken()).andExpect(status().isUnauthorized());
        getProtectedProbe(renewed.accessToken()).andExpect(status().isUnauthorized());

        // Device B's separate session is untouched: it can still renew and be used.
        getProtectedProbe(renew(deviceB.refreshToken()).accessToken()).andExpect(status().isOk());
    }

    @Test
    void loginAfterAnUnusableSessionRestoresTheSameAccount() throws Exception {
        IssuedTokens login = registerAndLogIn("again@example.com");
        renew(login.refreshToken());
        refresh(login.refreshToken()).andExpect(status().isUnauthorized());

        IssuedTokens fresh = logIn("again@example.com", PASSWORD);

        assertThat(fresh.accountId()).isEqualTo(login.accountId());
        getProtectedProbe(fresh.accessToken()).andExpect(status().isOk());
    }

    @Test
    void renewalIsPublicAndIgnoresAStaleAuthorizationHeader() throws Exception {
        IssuedTokens login = registerAndLogIn("stale@example.com");
        clock.advance(Duration.ofMinutes(16));

        tokensFrom(postJson(REFRESH, Map.of("refreshToken", login.refreshToken()), login.accessToken()));
    }

    @Test
    void unknownRefreshTokenIsRejectedWithoutRevokingAnything() throws Exception {
        registerAndLogIn("unknown-token@example.com");

        refresh("this-token-was-never-issued")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        assertThat(jdbc.sql("SELECT count(*) FROM identity.identity_session WHERE revoked_at IS NOT NULL")
                .query(Long.class).single()).isZero();
    }

    @Test
    void missingRefreshTokenIsAValidationError() throws Exception {
        postJson(REFRESH, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("refreshToken"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
    }

    @Test
    void tokensNeverAppearInLogs(CapturedOutput output) throws Exception {
        IssuedTokens login = registerAndLogIn("logs@example.com");
        IssuedTokens renewed = renew(login.refreshToken());
        refresh(login.refreshToken()).andExpect(status().isUnauthorized());

        assertThat(output.getAll())
                .doesNotContain(login.refreshToken())
                .doesNotContain(renewed.refreshToken())
                .doesNotContain(renewed.accessToken());
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return postJson(REFRESH, Map.of("refreshToken", refreshToken));
    }

    private IssuedTokens renew(String refreshToken) throws Exception {
        return tokensFrom(refresh(refreshToken));
    }

    private boolean isConsumed(String refreshToken) {
        return jdbc.sql("SELECT consumed_at IS NOT NULL FROM identity.identity_refresh_token WHERE token_hash = :hash")
                .param("hash", RefreshTokenHash.of(refreshToken).value())
                .query(Boolean.class)
                .single();
    }

    private String revocationReason(UUID sessionId) {
        return jdbc.sql("SELECT revocation_reason FROM identity.identity_session WHERE id = :id")
                .param("id", sessionId)
                .query(String.class)
                .single();
    }
}