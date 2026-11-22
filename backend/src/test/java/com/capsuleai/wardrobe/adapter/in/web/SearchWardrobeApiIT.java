package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-WAR-003 through the real filter chain and PostgreSQL: matching, active conditions, the four result
 * states, criteria-bound paging, validation, live view after removal, no mutation and owner isolation
 * (AC-01–AC-06).
 */
class SearchWardrobeApiIT extends WardrobeIntegrationTestSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void aSearchReturnsMatchingCurrentGarmentsWithTheNormalizedConditions() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-basic@example.com");
        UUID uniqloTop = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "Uniqlo"));
        addGarmentAndReturnId(tokens.accessToken(), garment("SHOES", "BLACK", "brand", "Uniqlo"));

        search(tokens.accessToken(), Map.of("q", "  UNIQLO ", "category", "TOPS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(1))
                .andExpect(jsonPath("$.garments[0].id").value(uniqloTop.toString()))
                .andExpect(jsonPath("$.garments[0].recommendationEligibility").value("NOT_EVALUATED"))
                .andExpect(jsonPath("$.resultState").value("HAS_RESULTS"))
                .andExpect(jsonPath("$.conditions.q").value("UNIQLO"))
                .andExpect(jsonPath("$.conditions.categories[0]").value("TOPS"))
                .andExpect(jsonPath("$.conditions.colors").isEmpty());
    }

    @Test
    void aVietnameseAliasFindsTheGarmentAndTheConditionIsEchoedAsTyped() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-vietnamese@example.com");
        UUID tShirt = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE", "subtype", "T_SHIRTS"));

        search(tokens.accessToken(), Map.of("q", " Áo Phông "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(1))
                .andExpect(jsonPath("$.garments[0].id").value(tShirt.toString()))
                .andExpect(jsonPath("$.resultState").value("HAS_RESULTS"))
                .andExpect(jsonPath("$.conditions.q").value("Áo Phông"));
        search(tokens.accessToken(), Map.of("q", "ao phong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments").isEmpty())
                .andExpect(jsonPath("$.resultState").value("NO_MATCH"));
    }

    @Test
    void noMatchIsDistinguishedFromAGenuinelyEmptyWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-states@example.com");
        search(tokens.accessToken(), Map.of("q", "navy"))
                .andExpect(jsonPath("$.resultState").value("EMPTY_WARDROBE"));
        search(tokens.accessToken(), Map.of())
                .andExpect(jsonPath("$.resultState").value("EMPTY_WARDROBE"))
                .andExpect(jsonPath("$.conditions.q").value(nullValue()));

        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));

        search(tokens.accessToken(), Map.of("q", "navy"))
                .andExpect(jsonPath("$.garments").isEmpty())
                .andExpect(jsonPath("$.resultState").value("NO_MATCH"));
        search(tokens.accessToken(), Map.of("category", "TOPS"))
                .andExpect(jsonPath("$.resultState").value("NO_MATCH"));
    }

    @Test
    void aFilteredCursorContinuesOnlyForTheSameConditions() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-cursor@example.com");
        UUID older = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        clock.advance(Duration.ofSeconds(1));
        UUID newer = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        String firstPage = body(mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                .queryParam("category", "TOPS").queryParam("color", "NAVY").queryParam("limit", "1")));
        assertThat((String) JsonPath.read(firstPage, "$.garments[0].id")).isEqualTo(newer.toString());
        String cursor = JsonPath.read(firstPage, "$.nextCursor");
        assertThat(cursor).startsWith("djJ8");

        mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                        .queryParam("color", "NAVY").queryParam("category", "TOPS").queryParam("limit", "1")
                        .queryParam("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[0].id").value(older.toString()))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
        mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                        .queryParam("q", "linen").queryParam("cursor", cursor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("cursor"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("CONDITIONS_CHANGED"));
    }

    @Test
    void theUnfilteredOverviewKeepsItsVersionOneCursorAndRefusesAFilteredOne() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-v1@example.com");
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        clock.advance(Duration.ofSeconds(1));
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));

        String unfiltered = body(mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                .queryParam("limit", "1")));
        assertThat((String) JsonPath.read(unfiltered, "$.nextCursor")).startsWith("djF8");
        String filteredCursor = JsonPath.read(body(mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                .queryParam("q", "navy").queryParam("limit", "1"))), "$.nextCursor");

        mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken()).queryParam("cursor", filteredCursor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].code").value("CONDITIONS_CHANGED"));
    }

    @Test
    void invalidConditionsAreAllReportedAndNothingIsRead() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-invalid@example.com");

        search(tokens.accessToken(), Map.of("q", "a".repeat(101), "category", "SOCKS", "color", "navy"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'q')].code").value("OUT_OF_RANGE"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'category')].code").value("UNSUPPORTED_VALUE"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'color')].code").value("UNSUPPORTED_VALUE"))
                .andExpect(jsonPath("$.garments").doesNotExist());
    }

    @Test
    void aGarmentRemovedBetweenPagesIsAbsentAndAnEmptyLaterPageIsTheEnd() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-live@example.com");
        UUID older = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        clock.advance(Duration.ofSeconds(1));
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "NAVY"));
        String cursor = JsonPath.read(body(mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                .queryParam("q", "navy").queryParam("limit", "1"))), "$.nextCursor");
        mockMvc.perform(authorized(post(GARMENTS + "/" + older + "/removal"), tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(Map.of("confirmed", true, "expectedRevision", "1"))))
                .andExpect(status().isOk());

        mockMvc.perform(authorized(get(GARMENTS), tokens.accessToken())
                        .queryParam("q", "navy").queryParam("limit", "1").queryParam("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments").isEmpty())
                .andExpect(jsonPath("$.resultState").value("END_OF_RESULTS"))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    @Test
    void searchingChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-api-readonly@example.com");
        addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "Uniqlo"));
        Map<String, Object> before = garmentState(tokens.accountId());

        search(tokens.accessToken(), Map.of("q", "uniqlo")).andExpect(status().isOk());
        search(tokens.accessToken(), Map.of("color", "NAVY")).andExpect(status().isOk());
        search(tokens.accessToken(), Map.of("q", "nothing-matches")).andExpect(status().isOk());

        assertThat(garmentState(tokens.accountId())).isEqualTo(before);
    }

    @Test
    void anotherAccountsGarmentsNeverMatch() throws Exception {
        IssuedTokens owner = registerAndLogIn("search-api-owner@example.com");
        IssuedTokens other = registerAndLogIn("search-api-other@example.com");
        addGarmentAndReturnId(other.accessToken(), minimumGarment("TOPS", "NAVY"));
        addGarmentAndReturnId(owner.accessToken(), minimumGarment("SHOES", "BLACK"));

        search(owner.accessToken(), Map.of("q", "navy"))
                .andExpect(jsonPath("$.garments").isEmpty())
                .andExpect(jsonPath("$.resultState").value("NO_MATCH"));
    }

    private ResultActions search(String accessToken, Map<String, String> parameters) throws Exception {
        MockHttpServletRequestBuilder request = authorized(get(GARMENTS), accessToken);
        parameters.forEach(request::queryParam);
        return mockMvc.perform(request);
    }

    private static MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request, String accessToken) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private static String body(ResultActions response) throws Exception {
        return response.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private Map<String, Object> garmentState(UUID owner) {
        return jdbc.sql("""
                        SELECT count(*) AS garments, sum(revision) AS revisions,
                               count(*) FILTER (WHERE removed_at IS NOT NULL) AS removed
                        FROM wardrobe.wardrobe_garment WHERE owner_account_id = :owner
                        """)
                .param("owner", owner).query().singleRow();
    }
}
