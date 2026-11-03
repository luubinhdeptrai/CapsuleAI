package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class RegistrationApiIT extends IdentityIntegrationTestSupport {

    @Test
    void createsOneAccountAndReturnsOnlyNonSecretInformation(CapturedOutput output) throws Exception {
        String password = "Distinctive-Registration-Secret-4417";

        postJson(REGISTRATIONS, registrationBody("  Person@Example.COM ", password, password, null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").isNotEmpty())
                .andExpect(jsonPath("$.email").value("person@example.com"))
                .andExpect(jsonPath("$.displayName").value(nullValue()))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(content().string(not(containsString(password))));

        Map<String, Object> row = jdbc.sql("SELECT email, password_hash FROM identity_account")
                .query()
                .singleRow();
        assertThat(row.get("email")).isEqualTo("person@example.com");
        assertThat((String) row.get("password_hash"))
                .startsWith("{pbkdf2-sha256-v1}")
                .doesNotContain(password);
        assertThat(jdbc.sql("SELECT created_at FROM identity_account").query(OffsetDateTime.class).single()
                .toInstant()).isEqualTo(clock.instant());
        assertThat(output.getAll()).doesNotContain(password);
    }

    @Test
    void keepsAnOptionalDisplayNameWithVietnameseDiacritics() throws Exception {
        String name = "Nguy\u1EC5n Th\u1ECB \u00C1nh";

        String body = postJson(REGISTRATIONS, registrationBody("anh@example.com", PASSWORD, PASSWORD, name))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(JsonPath.<String>read(body, "$.displayName")).isEqualTo(name);
        assertThat(jdbc.sql("SELECT display_name FROM identity_account").query(String.class).single())
                .isEqualTo(name);
    }

    @Test
    void explainsEveryMissingRequiredFieldAndCreatesNoAccount() throws Exception {
        postJson(REGISTRATIONS, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmPassword')].code", hasItem("REQUIRED")));

        assertThat(countRows("identity_account")).isZero();
    }

    @Test
    void mismatchedConfirmationIsCorrectableByResubmitting() throws Exception {
        postJson(REGISTRATIONS, registrationBody("fix@example.com", PASSWORD, PASSWORD + " typo", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("MISMATCH"));
        assertThat(countRows("identity_account")).isZero();

        register("fix@example.com", PASSWORD).andExpect(status().isCreated());
        assertThat(countRows("identity_account")).isOne();
    }

    @ParameterizedTest
    @CsvSource({"11, TOO_SHORT", "129, TOO_LONG"})
    void rejectsPasswordsOutsideTheLengthPolicy(int length, String expectedCode) throws Exception {
        register("length@example.com", "p".repeat(length))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value(expectedCode));

        assertThat(countRows("identity_account")).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {12, 128})
    void acceptsPasswordsAtTheLengthBoundaries(int length) throws Exception {
        register("boundary-" + length + "@example.com", "p".repeat(length))
                .andExpect(status().isCreated());
    }

    @Test
    void duplicateNormalizedEmailIsRejectedAndTheExistingAccountIsUnchanged() throws Exception {
        register("person@example.com", PASSWORD).andExpect(status().isCreated());
        Map<String, Object> before = jdbc.sql("SELECT * FROM identity_account").query().singleRow();

        register("  PERSON@Example.COM ", "a different password 2026")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));

        assertThat(jdbc.sql("SELECT * FROM identity_account").query().singleRow()).isEqualTo(before);
    }

    @Test
    void retryAfterAnAcceptedRegistrationCannotCreateADuplicate() throws Exception {
        // Simulates a lost 201 response: the client sends the identical request again.
        register("retry@example.com", PASSWORD).andExpect(status().isCreated());

        register("retry@example.com", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));

        assertThat(countRows("identity_account")).isOne();
    }

    @Test
    void providerAliasStringsRemainDistinctIdentities() throws Exception {
        register("person@example.com", PASSWORD).andExpect(status().isCreated());
        register("person+wardrobe@example.com", PASSWORD).andExpect(status().isCreated());

        assertThat(countRows("identity_account")).isEqualTo(2);
    }

    @Test
    void malformedJsonIsRejectedWithoutCreatingAnAccount() throws Exception {
        mockMvc.perform(post(REGISTRATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        assertThat(countRows("identity_account")).isZero();
    }
}