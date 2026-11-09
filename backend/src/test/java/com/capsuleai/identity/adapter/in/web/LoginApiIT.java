package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class LoginApiIT extends IdentityIntegrationTestSupport {

    @Test
    void loginIssuesBearerTokensForTheRegisteredAccount() throws Exception {
        UUID accountId = registerAccount("person@example.com", PASSWORD);

        postJson(LOGIN, Map.of("email", "  Person@EXAMPLE.com ", "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.accessTokenExpiresAt").value("2026-01-05T08:15:00Z"))
                .andExpect(jsonPath("$.sessionExpiresAt").value("2026-02-04T08:00:00Z"));
    }

    @Test
    void loginReachesTheCorrectAccountThroughTheProtectedPath() throws Exception {
        UUID accountId = registerAccount("owner@example.com", PASSWORD);

        IssuedTokens tokens = logIn("owner@example.com", PASSWORD);

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()));
    }

    @Test
    void storesSessionAuthorityAndOnlyAHashOfTheRefreshToken() throws Exception {
        IssuedTokens tokens = registerAndLogIn("hash@example.com");

        Map<String, Object> session = jdbc.sql("SELECT account_id FROM identity.identity_session").query().singleRow();
        assertThat(session.get("account_id")).isEqualTo(tokens.accountId());
        String storedHash = jdbc.sql("SELECT token_hash FROM identity.identity_refresh_token").query(String.class).single();
        assertThat(storedHash)
                .isEqualTo(RefreshTokenHash.of(tokens.refreshToken()).value())
                .isNotEqualTo(tokens.refreshToken());
    }

    @Test
    void exactPasswordWithSpacesAndUnicodeAuthenticatesButChangedVariantsDoNot() throws Exception {
        String exact = "  M\u1EADt kh\u1EA9u r\u1EA5t d\u00E0i  ";
        registerAccount("unicode@example.com", exact);

        logIn("unicode@example.com", exact);

        for (String variant : List.of(
                exact.strip(),
                exact.replace(" ", ""),
                Normalizer.normalize(exact, Normalizer.Form.NFD))) {
            postJson(LOGIN, Map.of("email", "unicode@example.com", "password", variant))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        assertThat(countRows("identity.identity_session")).isOne();
    }

    @Test
    void wrongPasswordAndUnknownEmailAreIndistinguishable() throws Exception {
        registerAccount("known@example.com", PASSWORD);

        String wrongPassword = postJson(LOGIN, Map.of("email", "known@example.com", "password", "not the password!"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String unknownEmail = postJson(LOGIN, Map.of("email", "unknown@example.com", "password", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(JsonPath.<String>read(wrongPassword, "$.code")).isEqualTo("INVALID_CREDENTIALS");
        assertThat(wrongPassword).isEqualTo(unknownEmail);
        assertThat(countRows("identity.identity_session")).isZero();
    }

    @Test
    void incompleteCredentialsAreValidationErrors() throws Exception {
        postJson(LOGIN, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')].code", hasItem("REQUIRED")));
    }

    @Test
    void loginAttemptsDoNotChangeTheAccount() throws Exception {
        registerAccount("stable@example.com", PASSWORD);
        Map<String, Object> before = jdbc.sql("SELECT * FROM identity.identity_account").query().singleRow();

        postJson(LOGIN, Map.of("email", "stable@example.com", "password", "wrong password 123"))
                .andExpect(status().isUnauthorized());
        logIn("stable@example.com", PASSWORD);

        assertThat(jdbc.sql("SELECT * FROM identity.identity_account").query().singleRow()).isEqualTo(before);
    }

    @Test
    void passwordsAndTokensNeverAppearInLogs(CapturedOutput output) throws Exception {
        String password = "Distinctive-Login-Secret-9902";
        registerAccount("logs@example.com", password);

        IssuedTokens tokens = logIn("logs@example.com", password);

        assertThat(output.getAll())
                .doesNotContain(password)
                .doesNotContain(tokens.accessToken())
                .doesNotContain(tokens.refreshToken());
    }

    @Test
    void staleAuthorizationHeaderDoesNotBlockLogin() throws Exception {
        registerAccount("stale@example.com", PASSWORD);

        postJson(LOGIN, Map.of("email", "stale@example.com", "password", PASSWORD), "expired-or-garbage-token")
                .andExpect(status().isOk());
    }

    @Test
    void registrationRemainsPublicNowThatSecurityIsActive() throws Exception {
        register("public@example.com", PASSWORD).andExpect(status().isCreated());
    }
}
