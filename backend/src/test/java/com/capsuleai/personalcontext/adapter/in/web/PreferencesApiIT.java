package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-PROF-001 through the real filter chain, application and PostgreSQL. */
class PreferencesApiIT extends PersonalContextIntegrationTestSupport {

    /** AC-01/AC-04: unset is absent context and blocks nothing else. */
    @Test
    void aNewAccountHasNoDeclaredContextAndCanStillUseTheWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-new@example.com");

        viewPreferences(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.styles.length()").value(0))
                .andExpect(jsonPath("$.commonNeeds.length()").value(6))
                .andExpect(jsonPath("$.commonNeeds", hasKey("TRAVEL")))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(nullValue()))
                .andExpect(jsonPath("$.updatedAt").value(nullValue()));
        mockMvc.perform(post("/api/v1/wardrobe/garments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,\"category\":\"TOPS\",\"color\":\"WHITE\"}"))
                .andExpect(status().isCreated());
    }

    /** AC-01/AC-02/AC-05. */
    @Test
    void aFirstDeclarationIsAcceptedAndReadBack() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-first@example.com");

        revisePreferences(tokens.accessToken(), body(null, List.of("SMART_CASUAL", "MINIMAL"),
                Map.of("WORK_PROFESSIONAL", 3, "SPORT_ACTIVE", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.styles[0]").value("MINIMAL"))
                .andExpect(jsonPath("$.styles[1]").value("SMART_CASUAL"));

        viewPreferences(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.commonNeeds.WORK_PROFESSIONAL").value(3))
                .andExpect(jsonPath("$.commonNeeds.SPORT_ACTIVE").value(0))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(nullValue()));
    }

    /** S3-PROF-02/03: untouched values stay; explicit zero stays zero. */
    @Test
    void partialRevisionsKeepUntouchedValuesAndExplicitZeroStaysZero() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-partial@example.com");
        revisePreferences(tokens.accessToken(), body(null, List.of("CLASSIC"), Map.of("TRAVEL", 0, "WORK_PROFESSIONAL", 2)))
                .andExpect(status().isOk());

        revisePreferences(tokens.accessToken(), body("1", null, Map.of("WORK_PROFESSIONAL", 3), "styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.styles[0]").value("CLASSIC"))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(0))
                .andExpect(jsonPath("$.commonNeeds.WORK_PROFESSIONAL").value(3));
    }

    /** S3-PROF-03: a cleared need is unset; cleared styles are none. */
    @Test
    void clearingANeedMakesItUnsetAndClearingStylesLeavesNone() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-clear@example.com");
        revisePreferences(tokens.accessToken(), body(null, List.of("FORMAL"), Map.of("SCHOOL_UNIVERSITY", 1)))
                .andExpect(status().isOk());
        Map<String, Object> clearSchool = new LinkedHashMap<>();
        clearSchool.put("SCHOOL_UNIVERSITY", null);

        revisePreferences(tokens.accessToken(), body("1", List.of(), clearSchool))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles.length()").value(0))
                .andExpect(jsonPath("$.commonNeeds.SCHOOL_UNIVERSITY").value(nullValue()));
    }

    /** AC-02, UC-005 E1. */
    @Test
    void unsupportedValuesAreExplainedAndNothingIsSaved() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-invalid@example.com");

        revisePreferences(tokens.accessToken(), body(null, List.of("MINIMAL", "BOHO"), Map.of("TRAVEL", 7)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("styles[1]"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("commonNeeds.TRAVEL"));
        viewPreferences(tokens.accessToken()).andExpect(jsonPath("$.revision").value(nullValue()));
    }

    /** S3-PROF-04: two devices, and a duplicated first declaration. */
    @Test
    void aStaleRevisionOrASecondFirstDeclarationIsAConflict() throws Exception {
        IssuedTokens phone = registerAndLogIn("prefs-devices@example.com");
        IssuedTokens laptop = logIn("prefs-devices@example.com", PASSWORD);
        revisePreferences(phone.accessToken(), body(null, List.of("CASUAL"), Map.of())).andExpect(status().isOk());

        revisePreferences(laptop.accessToken(), body(null, List.of("FORMAL"), Map.of()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        revisePreferences(phone.accessToken(), body("1", null, Map.of("TRAVEL", 2), "styles")).andExpect(status().isOk());
        revisePreferences(laptop.accessToken(), body("1", null, Map.of("TRAVEL", 3), "styles"))
                .andExpect(status().isConflict());
        viewPreferences(laptop.accessToken())
                .andExpect(jsonPath("$.styles[0]").value("CASUAL"))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(2));
    }

    /** AC-06, UC-005 E3: private to the account; no usable session, no change. */
    @Test
    void preferencesArePrivateToTheAccountAndNeedAUsableSession() throws Exception {
        IssuedTokens alice = registerAndLogIn("prefs-alice@example.com");
        IssuedTokens bob = registerAndLogIn("prefs-bob@example.com");
        revisePreferences(alice.accessToken(), body(null, List.of("STREETWEAR"), Map.of())).andExpect(status().isOk());

        viewPreferences(bob.accessToken()).andExpect(jsonPath("$.revision").value(nullValue()));
        mockMvc.perform(get(PREFERENCES)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + alice.accessToken()))
                .andExpect(status().is2xxSuccessful());
        revisePreferences(alice.accessToken(), body("1", List.of(), Map.of())).andExpect(status().isUnauthorized());

        IssuedTokens again = logIn("prefs-alice@example.com", PASSWORD);
        viewPreferences(again.accessToken()).andExpect(jsonPath("$.styles[0]").value("STREETWEAR"));
    }

    /** BRULE-GAR-004: garment Style codes are not profile styles. */
    @Test
    void garmentStylesAreNotProfileStyles() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-garment-style@example.com");

        revisePreferences(tokens.accessToken(), body(null, List.of("STREET", "MINIMALIST"), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    /** AC-06, S3-PROF-04: a lost response; the retry with its original revision cannot apply twice. */
    @Test
    void aCommittedRevisionRepeatedWithItsOriginalRevisionIsNeverAppliedTwice() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-lost@example.com");
        revisePreferences(tokens.accessToken(), body(null, List.of(), Map.of("TRAVEL", 1))).andExpect(status().isOk());
        Map<String, Object> intended = body("1", null, Map.of("TRAVEL", 2), "styles");

        revisePreferences(tokens.accessToken(), intended).andExpect(status().isOk());
        revisePreferences(tokens.accessToken(), intended).andExpect(status().isConflict());
        viewPreferences(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(2));
    }

    /** A request body; names listed in {@code omitted} are left out entirely (not sent as null). */
    private static Map<String, Object> body(String expectedRevision, List<String> styles, Map<String, ?> needs,
                                            String... omitted) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("expectedRevision", expectedRevision);
        body.put("styles", styles);
        body.put("commonNeeds", needs);
        for (String name : omitted) {
            body.remove(name);
        }
        return body;
    }
}
