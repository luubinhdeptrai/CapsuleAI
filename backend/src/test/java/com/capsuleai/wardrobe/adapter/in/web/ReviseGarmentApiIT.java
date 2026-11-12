package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-GAR-002 through the real filter chain, application and PostgreSQL: confirmed presence-aware
 * edits, shared validation, optimistic revisions, owner isolation and safe recovery.
 */
class ReviseGarmentApiIT extends WardrobeIntegrationTestSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** AC-01/AC-03/AC-05/AC-06. */
    @Test
    void aConfirmedEditIsReturnedAndReadBackThroughDetailAndOverview() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-basic@example.com");
        String created = addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("TOPS", "WHITE", "subtype", "T_SHIRTS", "materials", List.of("COTTON"), "brand", "Uniqlo"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revision").value("1"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        UUID id = UUID.fromString(JsonPath.read(created, "$.id"));

        String revised = revise(tokens.accessToken(), id, edit("1", "color", "NAVY",
                "materials", List.of("COTTON", "LINEN"), "pattern", "STRIPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.color").value("NAVY"))
                .andExpect(jsonPath("$.pattern").value("STRIPED"))
                .andExpect(jsonPath("$.materials", containsInAnyOrder("COTTON", "LINEN")))
                .andExpect(jsonPath("$.subtype").value("T_SHIRTS"))
                .andExpect(jsonPath("$.brand").value("Uniqlo"))
                .andExpect(jsonPath("$.provenance").value("USER_ENTERED"))
                .andExpect(jsonPath("$.recommendationEligibility").value("NOT_EVALUATED"))
                .andExpect(jsonPath("$.createdAt").value(JsonPath.<String>read(created, "$.createdAt")))
                .andExpect(jsonPath("$.revision").value("2"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(detail(tokens.accessToken(), id)).isEqualTo(revised);
        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[0].color").value("NAVY"));
    }

    /** AC-01/AC-03/AC-04: clearing optional values leaves them absent; the saving minimum stays. */
    @Test
    void clearingOptionalValuesLeavesThemAbsentWhileTheMinimumStays() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-clear@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("OUTERWEAR", "CAMEL",
                "subtype", "TRENCH_COATS", "pattern", "SOLID", "styles", List.of("CLASSIC"), "brand", "Burberry",
                "price", Map.of("amount", "2500000", "currency", "VND")));

        revise(tokens.accessToken(), id, edit("1", "subtype", null, "pattern", null, "styles", List.of(),
                "brand", null, "price", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("OUTERWEAR"))
                .andExpect(jsonPath("$.color").value("CAMEL"))
                .andExpect(jsonPath("$.subtype").value(nullValue()))
                .andExpect(jsonPath("$.pattern").value(nullValue()))
                .andExpect(jsonPath("$.styles.length()").value(0))
                .andExpect(jsonPath("$.brand").value(nullValue()))
                .andExpect(jsonPath("$.price").value(nullValue()));
        assertThat(countRows("wardrobe.wardrobe_garment_style")).isZero();
    }

    /** AC-01/AC-02, BRULE-GAR-010: known zero, exact Brand, incomplete pairs rejected. */
    @Test
    void aKnownZeroPriceAndExactBrandAreKeptAndAnIncompletePriceIsRejected() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-price@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("BAGS", "BLACK"));

        revise(tokens.accessToken(), id, edit("1", "price", Map.of("amount", "0", "currency", "VND"),
                "brand", "  Maison  Ébène "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.amount").value("0"))
                .andExpect(jsonPath("$.price.currency").value("VND"))
                .andExpect(jsonPath("$.brand").value("  Maison  Ébène "));

        revise(tokens.accessToken(), id, edit("2", "price", Map.of("amount", "5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("price"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("INCOMPLETE_PRICE"));
        assertThat(JsonPath.<String>read(detail(tokens.accessToken(), id), "$.price.amount")).isEqualTo("0");
        assertThat(JsonPath.<String>read(detail(tokens.accessToken(), id), "$.revision")).isEqualTo("2");
    }

    /** AC-02/AC-03, S3-GAR-03: nothing is cleared automatically after a Category change. */
    @Test
    void aCategoryChangeLeavingAnIncompatibleValueIsRejectedUntilTheSameEditClearsIt() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-category@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "BLUE",
                "subtype", "T_SHIRTS", "fit", "SLIM"));

        revise(tokens.accessToken(), id, edit("1", "category", "PANTS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'subtype')].code").value("NOT_IN_CATEGORY"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'fit')].code").value("NOT_APPLICABLE"));
        assertThat(JsonPath.<String>read(detail(tokens.accessToken(), id), "$.category")).isEqualTo("TOPS");

        revise(tokens.accessToken(), id, edit("1", "category", "PANTS", "subtype", "JEANS", "fit", null,
                "silhouette", "WIDE_LEG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("PANTS"))
                .andExpect(jsonPath("$.subtype").value("JEANS"))
                .andExpect(jsonPath("$.fit").value(nullValue()))
                .andExpect(jsonPath("$.silhouette").value("WIDE_LEG"))
                .andExpect(jsonPath("$.length").value(nullValue()))
                .andExpect(jsonPath("$.revision").value("2"));
    }

    /** S3-GAR-02: two editors of one revision; the second is told to reread, not merged. */
    @Test
    void aStaleRevisionIsAConflictAndTheRereadShowsTheWinningValues() throws Exception {
        IssuedTokens phone = registerAndLogIn("edit-two-devices@example.com");
        IssuedTokens laptop = logIn("edit-two-devices@example.com", PASSWORD);
        UUID id = addGarmentAndReturnId(phone.accessToken(), minimumGarment("SHOES", "WHITE"));

        revise(phone.accessToken(), id, edit("1", "color", "RED")).andExpect(status().isOk());
        revise(laptop.accessToken(), id, edit("1", "color", "BLUE"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"))
                .andExpect(jsonPath("$.color").doesNotExist());

        String reread = detail(laptop.accessToken(), id);
        assertThat(JsonPath.<String>read(reread, "$.color")).isEqualTo("RED");
        assertThat(JsonPath.<String>read(reread, "$.revision")).isEqualTo("2");
    }

    /** AC-07, S3-GAR-04: a committed edit whose response was lost cannot be applied twice by a retry. */
    @Test
    void anAcceptedEditRepeatedWithItsOriginalRevisionIsNeverAppliedTwice() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-lost-response@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE", "materials", List.of("COTTON")));
        Map<String, Object> intended = edit("1", "materials", List.of("WOOL"));

        revise(tokens.accessToken(), id, intended).andExpect(status().isOk());
        revise(tokens.accessToken(), id, intended)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));

        String reread = detail(tokens.accessToken(), id);
        assertThat(JsonPath.<List<String>>read(reread, "$.materials")).containsExactly("WOOL");
        assertThat(JsonPath.<String>read(reread, "$.revision")).isEqualTo("2");
        assertThat(countRows("wardrobe.wardrobe_garment_material")).isOne();
    }

    /** OPEN-S3-06: confirming the accepted values again is not a new revision. */
    @Test
    void anEditThatChangesNothingKeepsTheRevision() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-noop@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE", "styles", List.of("CASUAL")));

        revise(tokens.accessToken(), id, edit("1", "color", "WHITE", "styles", List.of("CASUAL", "CASUAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"));
    }

    /** AC-08: another account's garment is indistinguishable from an unknown one and stays unchanged. */
    @Test
    void anotherAccountsGarmentIsNotFoundAndStaysUnchanged() throws Exception {
        IssuedTokens alice = registerAndLogIn("edit-alice@example.com");
        IssuedTokens bob = registerAndLogIn("edit-bob@example.com");
        UUID alicesGarment = addGarmentAndReturnId(alice.accessToken(), minimumGarment("HEADWEAR", "GREEN"));

        revise(bob.accessToken(), alicesGarment, edit("1", "color", "RED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"));
        revise(bob.accessToken(), UUID.randomUUID(), edit("1", "color", "RED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"));
        revise(bob.accessToken(), "not-a-uuid", edit("1", "color", "RED"))
                .andExpect(status().isNotFound());

        assertThat(JsonPath.<String>read(detail(alice.accessToken(), alicesGarment), "$.color")).isEqualTo("GREEN");
    }

    /** AC-08, UC-009 E3: a removed selection is explained and not edited. */
    @Test
    void aRemovedGarmentIsNoLongerCurrentAndIsNotEdited() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-removed@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SKIRTS", "BLACK"));
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at WHERE id = :id").param("id", id).update();

        revise(tokens.accessToken(), id, edit("1", "color", "RED"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("GARMENT_NO_LONGER_CURRENT"));

        assertThat(jdbc.sql("SELECT color FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(String.class).single()).isEqualTo("BLACK");
    }

    /** AC-08, UC-009 E4: no usable session, no edit. */
    @Test
    void aMissingTokenOrALoggedOutSessionCannotEdit() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-logout@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "KHAKI"));

        mockMvc.perform(patch(GARMENTS + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(edit("1", "color", "RED"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().is2xxSuccessful());
        revise(tokens.accessToken(), id, edit("1", "color", "RED"))
                .andExpect(status().isUnauthorized());

        assertThat(jdbc.sql("SELECT color FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(String.class).single()).isEqualTo("KHAKI");
    }

    /** S3-GAR-04: creation keys stay stable after edits; a replay returns the current garment, once. */
    @Test
    void replayingTheOriginalCreationAfterAnEditReturnsTheCurrentGarmentOnce() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-replay@example.com");
        String key = UUID.randomUUID().toString();
        Map<String, Object> original = garment("TOPS", "WHITE", "subtype", "SHIRTS");
        String created = addGarment(tokens.accessToken(), key, original)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        UUID id = UUID.fromString(JsonPath.read(created, "$.id"));
        revise(tokens.accessToken(), id, edit("1", "color", "BLACK")).andExpect(status().isOk());

        addGarment(tokens.accessToken(), key, original)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.revision").value("2"));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /** AC-05, FR-WAR-003: unconfirmed or unusable requests change nothing. */
    @Test
    void anUnconfirmedOrUnusableEditChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-unconfirmed@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "IVORY"));
        Map<String, Object> unconfirmed = edit("1", "color", "RED");
        unconfirmed.put("confirmed", false);
        Map<String, Object> withoutRevision = edit("1", "color", "RED");
        withoutRevision.remove("expectedRevision");

        revise(tokens.accessToken(), id, unconfirmed)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("confirmed"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("NOT_CONFIRMED"));
        revise(tokens.accessToken(), id, withoutRevision)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expectedRevision"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
        revise(tokens.accessToken(), id, edit("abc", "color", "RED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_FORMAT"));
        revise(tokens.accessToken(), id, edit("1", "color", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("color"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));

        String reread = detail(tokens.accessToken(), id);
        assertThat(JsonPath.<String>read(reread, "$.color")).isEqualTo("IVORY");
        assertThat(JsonPath.<String>read(reread, "$.revision")).isEqualTo("1");
    }

    /** US-WAR-001 regression: edits change neither creation time nor the overview order. */
    @Test
    void editingKeepsTheOverviewOrder() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edit-order@example.com");
        UUID older = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BROWN"));
        clock.advance(Duration.ofSeconds(1));
        UUID newer = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "WHITE"));

        revise(tokens.accessToken(), older, edit("1", "color", "BLACK")).andExpect(status().isOk());

        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[0].id").value(newer.toString()))
                .andExpect(jsonPath("$.garments[1].id").value(older.toString()))
                .andExpect(jsonPath("$.garments[1].color").value("BLACK"));
    }

    private ResultActions revise(String accessToken, Object garmentId, Map<String, ?> body) throws Exception {
        return mockMvc.perform(patch(GARMENTS + "/" + garmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }

    private String detail(String accessToken, UUID garmentId) throws Exception {
        return mockMvc.perform(get(GARMENTS + "/" + garmentId).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** confirmed: true, the expected revision, then name/value pairs; a value may be null (= clear). */
    private static Map<String, Object> edit(String expectedRevision, Object... namesAndValues) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("confirmed", true);
        body.put("expectedRevision", expectedRevision);
        for (int index = 0; index < namesAndValues.length; index += 2) {
            body.put((String) namesAndValues[index], namesAndValues[index + 1]);
        }
        return body;
    }
}
