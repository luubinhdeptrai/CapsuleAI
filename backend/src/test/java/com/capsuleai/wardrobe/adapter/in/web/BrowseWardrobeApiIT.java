package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-WAR-001 through the real protected HTTP API, filter chain and PostgreSQL, page by page (AC-12). */
class BrowseWardrobeApiIT extends WardrobeIntegrationTestSupport {

    @Test
    void aGenuinelyEmptyWardrobeIsASuccessfulEmptyList() throws Exception {
        IssuedTokens tokens = registerAndLogIn("empty@example.com");

        browse(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments").isArray())
                .andExpect(jsonPath("$.garments").isEmpty())
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    @Test
    void garmentsInEveryCategoryAreListedNewestFirstWithIdentifyingInformation() throws Exception {
        IssuedTokens tokens = registerAndLogIn("all-categories@example.com");
        GarmentCategory[] categories = GarmentCategory.values();
        UUID[] ids = new UUID[categories.length];
        for (int index = 0; index < categories.length; index++) {
            ids[index] = addGarmentAndReturnId(tokens.accessToken(), minimumGarment(categories[index].name(), "OLIVE"));
            clock.advance(Duration.ofSeconds(1));
        }

        ResultActions overview = browse(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(categories.length));
        for (int position = 0; position < categories.length; position++) {
            int newestFirst = categories.length - 1 - position;
            overview.andExpect(jsonPath("$.garments[" + position + "].id").value(ids[newestFirst].toString()))
                    .andExpect(jsonPath("$.garments[" + position + "].category")
                            .value(categories[newestFirst].name()))
                    .andExpect(jsonPath("$.garments[" + position + "].color").value("OLIVE"));
        }
    }

    @Test
    void aMinimumOnlyImagelessGarmentStaysIdentifiableWithoutAnyInventedValue() throws Exception {
        IssuedTokens tokens = registerAndLogIn("minimum-only@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("HEADWEAR", "BEIGE"));

        browse(tokens.accessToken())
                .andExpect(jsonPath("$.garments[0].id").value(id.toString()))
                .andExpect(jsonPath("$.garments[0].category").value("HEADWEAR"))
                .andExpect(jsonPath("$.garments[0].color").value("BEIGE"))
                .andExpect(jsonPath("$.garments[0].subtype").value(nullValue()))
                .andExpect(jsonPath("$.garments[0].brand").value(nullValue()))
                .andExpect(jsonPath("$.garments[0].recommendationEligibility").value("NOT_EVALUATED"))
                .andExpect(jsonPath("$.garments[0].image").doesNotExist());
    }

    @Test
    void subtypeAndBrandAreShownWhenSupplied() throws Exception {
        IssuedTokens tokens = registerAndLogIn("described@example.com");
        addGarmentAndReturnId(tokens.accessToken(),
                garment("DRESSES_JUMPSUITS", "BURGUNDY", "subtype", "JACKET_DRESS", "brand", "Zara"));

        browse(tokens.accessToken())
                .andExpect(jsonPath("$.garments[0].subtype").value("JACKET_DRESS"))
                .andExpect(jsonPath("$.garments[0].brand").value("Zara"));
    }

    @Test
    void eachAccountSeesOnlyItsOwnGarments() throws Exception {
        IssuedTokens alice = registerAndLogIn("alice@example.com");
        IssuedTokens bob = registerAndLogIn("bob@example.com");
        UUID alicesGarment = addGarmentAndReturnId(alice.accessToken(), minimumGarment("TOPS", "RED"));
        addGarmentAndReturnId(bob.accessToken(), minimumGarment("PANTS", "BLUE"));

        browse(alice.accessToken())
                .andExpect(jsonPath("$.garments.length()").value(1))
                .andExpect(jsonPath("$.garments[0].id").value(alicesGarment.toString()));
    }

    @Test
    void aRemovedGarmentIsNotPresentedAsACurrentPossession() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removed@example.com");
        UUID kept = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SKIRTS", "BLACK"));
        UUID removed = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SKIRTS", "WHITE"));
        // Controlled fixture: Sprint 2 has no removal feature (PBI-016), only the stored meaning.
        markRemoved(removed);

        browse(tokens.accessToken())
                .andExpect(jsonPath("$.garments.length()").value(1))
                .andExpect(jsonPath("$.garments[0].id").value(kept.toString()));
    }

    @Test
    void browsingChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("read-only@example.com");
        addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "NAVY", "materials", List.of("WOOL"), "styles", List.of("CLASSIC", "MINIMALIST")));
        List<Map<String, Object>> before = snapshot();

        browse(tokens.accessToken()).andExpect(status().isOk());
        browse(tokens.accessToken()).andExpect(status().isOk());

        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    void aLargeWardrobeIsBrowsedPageByPageNewestFirstWithoutGapsOrRepeats() throws Exception {
        IssuedTokens tokens = registerAndLogIn("large@example.com");
        List<String> newestFirst = addGarments(tokens.accessToken(), 7);

        List<String> seen = new ArrayList<>();
        String cursor = null;
        int pages = 0;
        do {
            String body = body(page(tokens.accessToken(), "3", cursor));
            seen.addAll(JsonPath.read(body, "$.garments[*].id"));
            cursor = JsonPath.read(body, "$.nextCursor");
            pages++;
        } while (cursor != null);

        assertThat(seen).containsExactlyElementsOf(newestFirst);
        assertThat(pages).isEqualTo(3);
    }

    @Test
    void theDefaultPageHoldsTwentyGarmentsAndAPageHoldsAtMostOneHundred() throws Exception {
        IssuedTokens tokens = registerAndLogIn("default-page@example.com");
        addGarments(tokens.accessToken(), 21);

        browse(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(20))
                .andExpect(jsonPath("$.nextCursor").isString());
        page(tokens.accessToken(), "100", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(21))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    @Test
    void garmentsAddedOrRemovedBetweenPagesAreNeitherRepeatedNorSkipped() throws Exception {
        IssuedTokens tokens = registerAndLogIn("changing@example.com");
        List<String> newestFirst = addGarments(tokens.accessToken(), 4);
        String firstPage = body(page(tokens.accessToken(), "2", null));
        assertThat(ids(firstPage)).containsExactly(newestFirst.get(0), newestFirst.get(1));

        String addedMeanwhile = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("BAGS", "RED")).toString();
        markRemoved(UUID.fromString(newestFirst.get(2)));

        String secondPage = body(page(tokens.accessToken(), "2", JsonPath.read(firstPage, "$.nextCursor")));
        assertThat(ids(secondPage)).containsExactly(newestFirst.get(3));
        assertThat((Object) JsonPath.read(secondPage, "$.nextCursor")).isNull();

        String reloaded = body(page(tokens.accessToken(), "2", null));
        assertThat(ids(reloaded)).containsExactly(addedMeanwhile, newestFirst.get(0));
    }

    @Test
    void garmentsSavedAtTheSameInstantArePagedInTheStableIdOrder() throws Exception {
        IssuedTokens tokens = registerAndLogIn("same-instant@example.com");
        for (int count = 0; count < 3; count++) {
            // The test clock does not move, so all three share created_at and only the id orders them.
            addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "WHITE"));
        }
        List<String> databaseOrder = jdbc.sql("""
                        SELECT id::text FROM wardrobe.wardrobe_garment
                        WHERE owner_account_id = :owner ORDER BY created_at DESC, id DESC
                        """)
                .param("owner", tokens.accountId()).query(String.class).list();

        List<String> seen = new ArrayList<>();
        String cursor = null;
        do {
            String body = body(page(tokens.accessToken(), "1", cursor));
            seen.addAll(ids(body));
            cursor = JsonPath.read(body, "$.nextCursor");
        } while (cursor != null);

        assertThat(seen).hasSize(3).containsExactlyElementsOf(databaseOrder);
    }

    @Test
    void anUnusablePageRequestIsRejectedAndNeverShownAsAnEmptyWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bad-page@example.com");
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "RED"));

        for (String limit : List.of("0", "-1", "101")) {
            page(tokens.accessToken(), limit, null)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("limit"))
                    .andExpect(jsonPath("$.fieldErrors[0].code").value("OUT_OF_RANGE"))
                    .andExpect(jsonPath("$.garments").doesNotExist());
        }
        page(tokens.accessToken(), "ten", "not-a-cursor")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("limit"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_FORMAT"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("cursor"))
                .andExpect(jsonPath("$.fieldErrors[1].code").value("INVALID_FORMAT"))
                .andExpect(jsonPath("$.garments").doesNotExist());
    }

    @Test
    void anotherAccountsCursorNeverRevealsThatAccountsGarments() throws Exception {
        IssuedTokens alice = registerAndLogIn("alice@example.com");
        IssuedTokens bob = registerAndLogIn("bob@example.com");
        List<String> bobs = addGarments(bob.accessToken(), 2);
        List<String> alices = addGarments(alice.accessToken(), 3);
        String alicesCursor = JsonPath.read(body(page(alice.accessToken(), "1", null)), "$.nextCursor");

        String bobsView = body(page(bob.accessToken(), "100", alicesCursor));

        assertThat(ids(bobsView)).containsExactlyElementsOf(bobs).doesNotContainAnyElementsOf(alices);
    }

    /** Well-formed and canonical, but no stored garment can have such a creation time. */
    @Test
    void aCursorWithAnImpossibleTimeIsRejectedLikeAnyOtherUnusableCursor() throws Exception {
        IssuedTokens tokens = registerAndLogIn("impossible-cursor@example.com");
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "RED"));

        for (String time : List.of("+294277-01-01T00:00:00Z", "-4714-01-01T00:00:00Z",
                "+1000000000-12-31T23:59:59.999999999Z")) {
            page(tokens.accessToken(), "5", cursorAt(time))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("cursor"))
                    .andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_FORMAT"))
                    .andExpect(jsonPath("$.garments").doesNotExist());
        }
    }

    /** The bounds of the accepted range reach PostgreSQL and behave like any other position. */
    @Test
    void cursorsAtTheEdgesOfTheAcceptedTimeRangeStillWork() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edge-cursor@example.com");
        UUID garment = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "RED"));

        page(tokens.accessToken(), "5", cursorAt("9999-12-31T23:59:59.999999999Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[0].id").value(garment.toString()))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
        page(tokens.accessToken(), "5", cursorAt("0000-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments").isEmpty())
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    /** The case an offset gets wrong: the garment the position was taken from is gone. */
    @Test
    void removingTheGarmentACursorPointsAtSkipsNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removed-at-cursor@example.com");
        List<String> newestFirst = addGarments(tokens.accessToken(), 4);
        String firstPage = body(page(tokens.accessToken(), "2", null));
        assertThat(ids(firstPage)).containsExactly(newestFirst.get(0), newestFirst.get(1));

        markRemoved(UUID.fromString(newestFirst.get(1)));

        String secondPage = body(page(tokens.accessToken(), "2", JsonPath.read(firstPage, "$.nextCursor")));
        assertThat(ids(secondPage)).containsExactly(newestFirst.get(2), newestFirst.get(3));
        assertThat((Object) JsonPath.read(secondPage, "$.nextCursor")).isNull();
    }

    @Test
    void aMalformedOrTamperedTokenIsRefusedAndRevealsNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("tampered@example.com");
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "RED"));
        String token = tokens.accessToken();
        String tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");

        for (String invalid : List.of("not-a-jwt", tampered)) {
            expectAuthenticationRequired(browse(invalid));
        }
    }

    @Test
    void unusableAccessIsNeverReportedAsAnEmptyWardrobe() throws Exception {
        IssuedTokens loggedOut = registerAndLogIn("logged-out@example.com");
        addGarmentAndReturnId(loggedOut.accessToken(), minimumGarment("SHOES", "RED"));
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loggedOut.accessToken()))
                .andExpect(status().isNoContent());
        IssuedTokens resetRevoked = registerAndLogIn("reset@example.com");
        completePasswordReset(requestPasswordResetToken("reset@example.com"), "a brand new passphrase")
                .andExpect(status().isNoContent());

        for (IssuedTokens revoked : List.of(loggedOut, resetRevoked)) {
            expectAuthenticationRequired(browse(revoked.accessToken()));
        }
        IssuedTokens expired = registerAndLogIn("expired@example.com");
        clock.advance(Duration.ofMinutes(15));
        expectAuthenticationRequired(browse(expired.accessToken()));
        expectAuthenticationRequired(mockMvc.perform(get(GARMENTS)));
    }

    @Test
    void afterLoggingInAgainTheSameAccountSeesItsWardrobe() throws Exception {
        IssuedTokens first = registerAndLogIn("returning@example.com");
        UUID garmentId = addGarmentAndReturnId(first.accessToken(), minimumGarment("BAGS", "TEAL"));
        clock.advance(Duration.ofMinutes(15));

        IssuedTokens again = logIn("returning@example.com", PASSWORD);

        browse(again.accessToken()).andExpect(jsonPath("$.garments[0].id").value(garmentId.toString()));
    }

    private ResultActions browse(String accessToken) throws Exception {
        return mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private ResultActions page(String accessToken, String limit, String cursor) throws Exception {
        MockHttpServletRequestBuilder request = get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        if (limit != null) {
            request = request.queryParam("limit", limit);
        }
        if (cursor != null) {
            request = request.queryParam("cursor", cursor);
        }
        return mockMvc.perform(request);
    }

    private static String cursorAt(String time) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("v1|" + time + "|" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
    }

    private static String body(ResultActions response) throws Exception {
        return response.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static List<String> ids(String body) {
        return JsonPath.read(body, "$.garments[*].id");
    }

    /** Adds minimum garments one second apart; returns their ids newest first. */
    private List<String> addGarments(String accessToken, int count) throws Exception {
        List<String> newestFirst = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            newestFirst.addFirst(addGarmentAndReturnId(accessToken, minimumGarment("TOPS", "LIGHT_GRAY")).toString());
            clock.advance(Duration.ofSeconds(1));
        }
        return newestFirst;
    }

    private static void expectAuthenticationRequired(ResultActions response) throws Exception {
        response.andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.garments").doesNotExist());
    }

    private void markRemoved(UUID garmentId) {
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at WHERE id = :id").param("id", garmentId).update();
    }

    private List<Map<String, Object>> snapshot() {
        return jdbc.sql("""
                        SELECT g.*, (SELECT string_agg(material, ',' ORDER BY material)
                                     FROM wardrobe.wardrobe_garment_material m WHERE m.garment_id = g.id) AS materials,
                               (SELECT string_agg(style, ',' ORDER BY style)
                                     FROM wardrobe.wardrobe_garment_style s WHERE s.garment_id = g.id) AS styles
                        FROM wardrobe.wardrobe_garment g ORDER BY g.id
                        """)
                .query()
                .listOfRows();
    }
}
