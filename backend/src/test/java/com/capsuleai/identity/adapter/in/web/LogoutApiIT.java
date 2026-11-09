package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogoutApiIT extends IdentityIntegrationTestSupport {

    private static final String LOGOUT = "/api/v1/auth/logout";

    @Test
    void logoutEndsTheCurrentSessionEvenThoughItsAccessTokenIsUnexpired() throws Exception {
        IssuedTokens tokens = registerAndLogIn("person@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        logOut(tokens.accessToken()).andExpect(status().isNoContent());

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        Map<String, Object> row = jdbc.sql("""
                        SELECT revoked_at, revocation_reason FROM identity.identity_session WHERE id = :id
                        """)
                .param("id", sessionId)
                .query()
                .singleRow();
        assertThat(row.get("revoked_at")).isNotNull();
        assertThat(row.get("revocation_reason")).isEqualTo("LOGOUT");
    }

    @Test
    void logoutLeavesAnotherSessionOfTheSameAccountUsable() throws Exception {
        registerAccount("two-devices@example.com", PASSWORD);
        IssuedTokens deviceA = logIn("two-devices@example.com", PASSWORD);
        IssuedTokens deviceB = logIn("two-devices@example.com", PASSWORD);

        logOut(deviceA.accessToken()).andExpect(status().isNoContent());

        getProtectedProbe(deviceA.accessToken()).andExpect(status().isUnauthorized());
        getProtectedProbe(deviceB.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(deviceB.accountId().toString()));
        assertThat(jdbc.sql("SELECT count(*) FROM identity.identity_session WHERE revoked_at IS NULL")
                .query(Long.class).single()).isOne();
    }

    @Test
    void repeatingLogoutReportsTheSessionAsAlreadyUnusable() throws Exception {
        IssuedTokens tokens = registerAndLogIn("retry@example.com");
        logOut(tokens.accessToken()).andExpect(status().isNoContent());

        logOut(tokens.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        assertThat(countRevokedSessions()).isOne();
    }

    @Test
    void logoutWithAnExpiredAccessTokenIsRejectedAndTargetsNoSession() throws Exception {
        IssuedTokens tokens = registerAndLogIn("expired@example.com");
        clock.advance(Duration.ofMinutes(15));

        logOut(tokens.accessToken()).andExpect(status().isUnauthorized());

        assertThat(countRevokedSessions()).isZero();
    }

    @Test
    void logoutKeepsTheAccountAndAllowsLoggingInAgain() throws Exception {
        IssuedTokens first = registerAndLogIn("keep@example.com");
        Map<String, Object> accountBefore = jdbc.sql("SELECT * FROM identity.identity_account").query().singleRow();

        logOut(first.accessToken()).andExpect(status().isNoContent());

        assertThat(jdbc.sql("SELECT * FROM identity.identity_account").query().singleRow()).isEqualTo(accountBefore);
        IssuedTokens again = logIn("keep@example.com", PASSWORD);
        assertThat(again.accountId()).isEqualTo(first.accountId());
        getProtectedProbe(again.accessToken()).andExpect(status().isOk());
    }

    @Test
    void logoutWithoutAuthenticationIsRejectedAndChangesNothing() throws Exception {
        registerAndLogIn("anonymous@example.com");

        mockMvc.perform(post(LOGOUT)).andExpect(status().isUnauthorized());

        assertThat(countRevokedSessions()).isZero();
    }

    @Test
    void oneAccountsLogoutDoesNotAffectAnotherAccount() throws Exception {
        IssuedTokens first = registerAndLogIn("first@example.com");
        IssuedTokens second = registerAndLogIn("second@example.com");

        logOut(first.accessToken()).andExpect(status().isNoContent());

        getProtectedProbe(first.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accountId").doesNotExist());
        getProtectedProbe(second.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(second.accountId().toString()));
    }

    private ResultActions logOut(String accessToken) throws Exception {
        return mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private long countRevokedSessions() {
        return jdbc.sql("SELECT count(*) FROM identity.identity_session WHERE revoked_at IS NOT NULL")
                .query(Long.class)
                .single();
    }
}