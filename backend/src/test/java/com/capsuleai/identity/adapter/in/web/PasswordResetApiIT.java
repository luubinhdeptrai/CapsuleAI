package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-AUTH-005 through the real HTTP API, filter chain and PostgreSQL. Delivery is
 * the recording substitute from the test configuration, so each test can open the
 * link CapsuleAI "emailed".
 */
@ExtendWith(OutputCaptureExtension.class)
class PasswordResetApiIT extends IdentityIntegrationTestSupport {

    private static final String REFRESH = "/api/v1/auth/refresh";
    private static final String NEW_PASSWORD = "a brand new passphrase";

    @Test
    void registeredAndUnregisteredEmailsReceiveIdenticalResponses() throws Exception {
        registerAccount("known@example.com", PASSWORD);

        MvcResult known = postJson(PASSWORD_RESET_REQUEST, Map.of("email", "known@example.com")).andReturn();
        MvcResult unknown = postJson(PASSWORD_RESET_REQUEST, Map.of("email", "unknown@example.com")).andReturn();

        assertThat(known.getResponse().getStatus()).isEqualTo(202).isEqualTo(unknown.getResponse().getStatus());
        assertThat(known.getResponse().getContentType()).isEqualTo(unknown.getResponse().getContentType());
        assertThat(known.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo(unknown.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("\"code\":\"PASSWORD_RESET_REQUESTED\"")
                .contains("If an account exists for this email");
        assertThat(passwordResetInstructions.sent()).singleElement()
                .satisfies(sent -> assertThat(sent.recipient().value()).isEqualTo("known@example.com"));
    }

    @Test
    void emailCaseAndSurroundingSpacesReachTheRegisteredAccount() throws Exception {
        registerAccount("person@example.com", PASSWORD);

        postJson(PASSWORD_RESET_REQUEST, Map.of("email", "  PERSON@Example.com ")).andExpect(status().isAccepted());

        assertThat(passwordResetInstructions.latestTokenFor("person@example.com")).isPresent();
    }

    @Test
    void aStaleBearerHeaderDoesNotBlockRecovery() throws Exception {
        registerAccount("person@example.com", PASSWORD);

        mockMvc.perform(post(PASSWORD_RESET_REQUEST)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-or-garbage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"person@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void requestingAndOpeningChangeNeitherThePasswordNorTheSessions() throws Exception {
        IssuedTokens tokens = registerAndLogIn("person@example.com");
        String resetToken = requestPasswordResetToken("person@example.com");

        postJson(PASSWORD_RESET_CHECK, Map.of("token", resetToken)).andExpect(status().isNoContent());

        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());
        logIn("person@example.com", PASSWORD);
    }

    @Test
    void anAcceptedResetRequiresTheNewPasswordAndRevokesEverySessionOfTheAccount() throws Exception {
        UUID accountId = registerAccount("person@example.com", PASSWORD);
        IssuedTokens phone = logIn("person@example.com", PASSWORD);
        IssuedTokens laptop = logIn("person@example.com", PASSWORD);
        String resetToken = requestPasswordResetToken("person@example.com");

        completePasswordReset(resetToken, NEW_PASSWORD).andExpect(status().isNoContent());

        postJson(LOGIN, Map.of("email", "person@example.com", "password", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        for (IssuedTokens revoked : new IssuedTokens[] {phone, laptop}) {
            getProtectedProbe(revoked.accessToken()).andExpect(status().isUnauthorized());
            postJson(REFRESH, Map.of("refreshToken", revoked.refreshToken())).andExpect(status().isUnauthorized());
        }
        assertThat(jdbc.sql("SELECT count(*) FROM identity.identity_session WHERE revocation_reason = 'PASSWORD_RESET'")
                .query(Long.class).single()).isEqualTo(2);
        assertThat(logIn("person@example.com", NEW_PASSWORD).accountId()).isEqualTo(accountId);
    }

    @Test
    void anotherAccountKeepsItsSessionsAndPassword() throws Exception {
        registerAccount("person@example.com", PASSWORD);
        IssuedTokens other = registerAndLogIn("other@example.com");

        completePasswordReset(requestPasswordResetToken("person@example.com"), NEW_PASSWORD)
                .andExpect(status().isNoContent());

        getProtectedProbe(other.accessToken()).andExpect(status().isOk());
        logIn("other@example.com", PASSWORD);
    }

    @Test
    void theInteractionIsRejectedFromExactlyThirtyMinutesAfterIssue() throws Exception {
        registerAccount("person@example.com", PASSWORD);
        String resetToken = requestPasswordResetToken("person@example.com");

        clock.advance(Duration.ofMinutes(30).minusSeconds(1));
        postJson(PASSWORD_RESET_CHECK, Map.of("token", resetToken)).andExpect(status().isNoContent());

        clock.advance(Duration.ofSeconds(1));
        postJson(PASSWORD_RESET_CHECK, Map.of("token", resetToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_INVALID"));
        completePasswordReset(resetToken, NEW_PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_INVALID"));
        logIn("person@example.com", PASSWORD);
    }

    @Test
    void aNewIssuanceInvalidatesTheEarlierLinkAndTheNewOneWorks() throws Exception {
        registerAccount("person@example.com", PASSWORD);
        String earlier = requestPasswordResetToken("person@example.com");
        String replacement = requestPasswordResetToken("person@example.com");

        completePasswordReset(earlier, NEW_PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_INVALID"));
        completePasswordReset(replacement, NEW_PASSWORD).andExpect(status().isNoContent());

        logIn("person@example.com", NEW_PASSWORD);
    }

    @Test
    void aReplayedSubmissionAfterSuccessCannotChangeThePasswordAgain() throws Exception {
        registerAccount("person@example.com", PASSWORD);
        String resetToken = requestPasswordResetToken("person@example.com");
        completePasswordReset(resetToken, NEW_PASSWORD).andExpect(status().isNoContent());

        completePasswordReset(resetToken, "an attacker's passphrase")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_INVALID"))
                .andExpect(jsonPath("$.detail").value(containsString("log in with it")));

        logIn("person@example.com", NEW_PASSWORD);
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 129})
    void aPolicyBreakingPasswordIsCorrectedWithoutConsumingTheLink(int length) throws Exception {
        registerAccount("person@example.com", PASSWORD);
        String resetToken = requestPasswordResetToken("person@example.com");

        completePasswordReset(resetToken, "p".repeat(length))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("newPassword"));

        completePasswordReset(resetToken, "p".repeat(12)).andExpect(status().isNoContent());
        logIn("person@example.com", "p".repeat(12));
    }

    @Test
    void theExactUnicodePasswordWithSpacesAuthenticatesAndAlteredFormsDoNot() throws Exception {
        registerAccount("person@example.com", PASSWORD);
        String unicodePassword = "  Mật khẩu mới  ";

        completePasswordReset(requestPasswordResetToken("person@example.com"), unicodePassword)
                .andExpect(status().isNoContent());

        logIn("person@example.com", unicodePassword);
        postJson(LOGIN, Map.of("email", "person@example.com", "password", unicodePassword.strip()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anUnknownLinkChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("person@example.com");

        completePasswordReset("never-issued-secret", NEW_PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_INVALID"));

        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());
        logIn("person@example.com", PASSWORD);
    }

    @Test
    void missingFieldsAreValidationErrors() throws Exception {
        postJson(PASSWORD_RESET_REQUEST, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
        postJson(PASSWORD_RESET_CHECK, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("token"));
        postJson(PASSWORD_RESET_COMPLETE, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    @Test
    void secretsNeverAppearInResponsesOrLogs(CapturedOutput output) throws Exception {
        registerAccount("person@example.com", PASSWORD);
        String resetToken = requestPasswordResetToken("person@example.com");

        String completion = completePasswordReset(resetToken, NEW_PASSWORD)
                .andExpect(content().string(""))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String replay = completePasswordReset(resetToken, NEW_PASSWORD)
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(completion + replay).doesNotContain(resetToken).doesNotContain(NEW_PASSWORD);
        assertThat(output.getAll()).doesNotContain(resetToken).doesNotContain(NEW_PASSWORD);
    }
}
