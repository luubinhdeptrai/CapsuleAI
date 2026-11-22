package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.api.DeclaredPersonalContext;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-PROF-002 through the real filter chain and PostgreSQL: absence by default, declare/revise/clear, conflicts,
 * validation, independence from preferences and the published contract, no category restriction, and access
 * control (AC-01–AC-06).
 */
class OptionalContextApiIT extends PersonalContextIntegrationTestSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private CurrentPersonalContext currentPersonalContext;

    @Test
    void anAccountThatDeclaredNothingHasAbsentOptionalContext() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-absent@example.com");

        viewOptionalContext(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.genderContext").value(nullValue()))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()))
                .andExpect(jsonPath("$.updatedAt").value(nullValue()));
    }

    @Test
    void optionalContextCanBeDeclaredRevisedAndCleared() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-lifecycle@example.com");

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "NON_BINARY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.genderContext").value("NON_BINARY"))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "bodyShape", "HOURGLASS"))
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.genderContext").value("NON_BINARY"))
                .andExpect(jsonPath("$.bodyShape").value("HOURGLASS"));
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "2", "genderContext", null))
                .andExpect(jsonPath("$.revision").value("3"))
                .andExpect(jsonPath("$.genderContext").value(nullValue()))
                .andExpect(jsonPath("$.bodyShape").value("HOURGLASS"));

        assertThat(jdbc.sql("SELECT gender_context IS NULL FROM personal_context.personal_context_optional_context "
                        + "WHERE owner_account_id = :owner")
                .param("owner", tokens.accountId()).query(Boolean.class).single())
                .as("a cleared value is gone from persistence").isTrue();
    }

    @Test
    void aStaleRevisionIsAConflictAndChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-stale@example.com");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "bodyShape", "ROUND"));

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "bodyShape", "TRIANGLE"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        viewOptionalContext(tokens.accessToken()).andExpect(jsonPath("$.bodyShape").value("ROUND"));
    }

    @Test
    void unsupportedValuesAreRejectedWithoutEchoingThem() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-invalid@example.com");

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "woman",
                "bodyShape", "SLIM"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'genderContext')].code").value("UNSUPPORTED_VALUE"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'bodyShape')].code").value("UNSUPPORTED_VALUE"))
                .andExpect(jsonPath("$.detail").value(not(containsString("woman"))));
        viewOptionalContext(tokens.accessToken()).andExpect(jsonPath("$.revision").value(nullValue()));
    }

    @Test
    void theExpectedRevisionIsRequired() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-required@example.com");

        reviseOptionalContext(tokens.accessToken(), Map.of("genderContext", "MAN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expectedRevision"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
    }

    @Test
    void optionalContextNeverChangesPreferencesOrThePublishedContext() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-independent@example.com");
        revisePreferences(tokens.accessToken(), json("expectedRevision", null, "styles", List.of("MINIMAL")))
                .andExpect(status().isOk());
        Optional<DeclaredPersonalContext> publishedBefore = currentPersonalContext.find(tokens.accountId());

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "WOMAN",
                "bodyShape", "RECTANGLE")).andExpect(status().isOk());

        viewPreferences(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.styles[0]").value("MINIMAL"));
        assertThat(currentPersonalContext.find(tokens.accountId())).isEqualTo(publishedBefore);
        assertThat(currentPersonalContext.find(tokens.accountId()).orElseThrow().styleCodes())
                .containsExactly("MINIMAL");
    }

    @Test
    void everyGarmentCategoryStaysCreatableWithDeclaredContext() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-soft@example.com");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "MAN",
                "bodyShape", "TRIANGLE")).andExpect(status().isOk());

        for (String category : List.of("TOPS", "DRESSES_JUMPSUITS", "PANTS", "SKIRTS", "OUTERWEAR", "SHOES", "BAGS",
                "HEADWEAR")) {
            mockMvc.perform(post("/api/v1/wardrobe/garments")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.writeValueAsString(Map.of("confirmed", true, "category", category,
                                    "color", "BLACK"))))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void noOtherSessionOrAccountCanReadOrChangeIt() throws Exception {
        IssuedTokens owner = registerAndLogIn("optional-owner@example.com");
        IssuedTokens other = registerAndLogIn("optional-other@example.com");
        reviseOptionalContext(owner.accessToken(), json("expectedRevision", null, "genderContext", "NON_BINARY"));

        viewOptionalContext(other.accessToken()).andExpect(jsonPath("$.genderContext").value(nullValue()));
        mockMvc.perform(get(OPTIONAL_CONTEXT)).andExpect(status().isUnauthorized());
        postJson("/api/v1/auth/logout", Map.of(), owner.accessToken()).andExpect(status().is2xxSuccessful());
        viewOptionalContext(owner.accessToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void anEmptyFirstChangeAndANoOpWriteNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-noop@example.com");

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", null,
                "bodyShape", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.genderContext").value(nullValue()))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));
        assertThat(countRows("personal_context.personal_context_optional_context"))
                .as("an empty first change creates no record").isZero();

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "bodyShape", "ROUND"))
                .andExpect(status().isOk());
        String stored = storedState(tokens.accountId());
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "genderContext", null,
                "bodyShape", "ROUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.bodyShape").value("ROUND"));
        assertThat(storedState(tokens.accountId())).as("a no-op keeps the revision and the update time")
                .isEqualTo(stored);
    }

    @Test
    void clearingBothValuesKeepsTheRevisionLineSoAStaleFirstDeclarationStillConflicts() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-clear-both@example.com");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "WOMAN",
                "bodyShape", "RECTANGLE")).andExpect(status().isOk());

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "genderContext", null,
                "bodyShape", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.genderContext").value(nullValue()))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));

        viewOptionalContext(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.genderContext").value(nullValue()))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));
        assertThat(storedState(tokens.accountId())).as("no copy of a cleared value remains").startsWith("2|null|null|");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "MAN"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
    }

    @Test
    void malformedValuesAndBodiesAreRejectedAndChangeNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-malformed@example.com");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "NON_BINARY"))
                .andExpect(status().isOk());

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "genderContext", 3))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("genderContext"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("UNSUPPORTED_VALUE"));
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "bodyShape", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("bodyShape"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("UNSUPPORTED_VALUE"));
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", 1, "genderContext", "MAN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expectedRevision"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_FORMAT"));
        mockMvc.perform(patch(OPTIONAL_CONTEXT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expectedRevision\":\"1\",\"genderContext\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        viewOptionalContext(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.genderContext").value("NON_BINARY"))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));
    }

    /** M7 automated: declared, rejected and conflicting values never reach the application log. */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void declaredAndRejectedValuesNeverReachTheLog(CapturedOutput output) throws Exception {
        LoggerFactory.getLogger(OptionalContextApiIT.class).info("optional-context log capture is active");
        IssuedTokens tokens = registerAndLogIn("optional-log@example.com");

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "NON_BINARY",
                "bodyShape", "INVERTED_TRIANGLE")).andExpect(status().isOk());
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "genderContext", "agender-typo",
                "bodyShape", "HOURGLASS")).andExpect(status().isBadRequest());
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "bodyShape", "TRIANGLE"))
                .andExpect(status().isConflict());
        viewOptionalContext(tokens.accessToken()).andExpect(status().isOk());

        assertThat(output.getAll())
                .as("the capture must see this class's own log line, or the check below proves nothing")
                .contains("optional-context log capture is active")
                .doesNotContain("NON_BINARY")
                .doesNotContain("TRIANGLE")
                .doesNotContain("agender-typo")
                .doesNotContain("HOURGLASS");
    }

    /** revision|gender_context|body_shape|updated_at of the owner's stored record. */
    private String storedState(UUID owner) {
        return jdbc.sql("""
                        SELECT revision || '|' || coalesce(gender_context, 'null') || '|' || coalesce(body_shape, 'null')
                               || '|' || updated_at
                        FROM personal_context.personal_context_optional_context WHERE owner_account_id = :owner
                        """)
                .param("owner", owner).query(String.class).single();
    }
}
