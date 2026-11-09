package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-WAR-002 through the real protected HTTP API, filter chain and PostgreSQL. */
class GarmentDetailApiIT extends WardrobeIntegrationTestSupport {

    @ParameterizedTest
    @EnumSource(GarmentCategory.class)
    void everyGlobalPropertyIsReturnedExactlyForEveryCategory(GarmentCategory category) throws Exception {
        IssuedTokens tokens = registerAndLogIn("global@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment(category.name(), "COLORFUL",
                "materials", List.of("WOOL", "CASHMERE", "WOOL"),
                "pattern", "HOUNDSTOOTH",
                "styles", List.of("FEMININE", "CLASSIC", "FEMININE"),
                "brand", "Brand Ẩn Danh",
                "price", Map.of("amount", "12.50", "currency", "USD")));

        inspect(tokens.accessToken(), id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.category").value(category.name()))
                .andExpect(jsonPath("$.color").value("COLORFUL"))
                .andExpect(jsonPath("$.materials.length()").value(2))
                .andExpect(jsonPath("$.materials[0]").value("WOOL"))
                .andExpect(jsonPath("$.materials[1]").value("CASHMERE"))
                .andExpect(jsonPath("$.pattern").value("HOUNDSTOOTH"))
                .andExpect(jsonPath("$.styles.length()").value(2))
                .andExpect(jsonPath("$.styles[0]").value("CLASSIC"))
                .andExpect(jsonPath("$.styles[1]").value("FEMININE"))
                .andExpect(jsonPath("$.brand").value("Brand Ẩn Danh"))
                .andExpect(jsonPath("$.price.amount").value("12.50"))
                .andExpect(jsonPath("$.price.currency").value("USD"))
                .andExpect(jsonPath("$.provenance").value("USER_ENTERED"));
    }

    @Test
    void aTopShowsItsTopsDetailsAndNoPantsDetail() throws Exception {
        IssuedTokens tokens = registerAndLogIn("top-detail@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE",
                "subtype", "POLO_SHIRTS", "fit", "SLIM", "neckline", "SHIRT_COLLAR",
                "sleeveLength", "SHORT_SLEEVE", "length", "WAIST"));

        inspect(tokens.accessToken(), id)
                .andExpect(jsonPath("$.subtype").value("POLO_SHIRTS"))
                .andExpect(jsonPath("$.fit").value("SLIM"))
                .andExpect(jsonPath("$.neckline").value("SHIRT_COLLAR"))
                .andExpect(jsonPath("$.sleeveLength").value("SHORT_SLEEVE"))
                .andExpect(jsonPath("$.length").value("WAIST"))
                .andExpect(jsonPath("$.silhouette").value(nullValue()));
    }

    @Test
    void pantsShowTheirLengthAndSilhouetteAndNoTopsDetail() throws Exception {
        IssuedTokens tokens = registerAndLogIn("pants-detail@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("PANTS", "DARK_GRAY",
                "subtype", "JEANS", "length", "SHORT", "silhouette", "BOOTCUT"));

        inspect(tokens.accessToken(), id)
                .andExpect(jsonPath("$.length").value("SHORT"))
                .andExpect(jsonPath("$.silhouette").value("BOOTCUT"))
                .andExpect(jsonPath("$.fit").value(nullValue()))
                .andExpect(jsonPath("$.neckline").value(nullValue()))
                .andExpect(jsonPath("$.sleeveLength").value(nullValue()));
    }

    @Test
    void aMinimumOnlyGarmentKeepsEveryAbsentValueAbsent() throws Exception {
        IssuedTokens tokens = registerAndLogIn("minimum-detail@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("BAGS", "BLACK"));

        inspect(tokens.accessToken(), id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtype").value(nullValue()))
                .andExpect(jsonPath("$.materials").isEmpty())
                .andExpect(jsonPath("$.pattern").value(nullValue()))
                .andExpect(jsonPath("$.styles").isEmpty())
                .andExpect(jsonPath("$.style").doesNotExist())
                .andExpect(jsonPath("$.brand").value(nullValue()))
                .andExpect(jsonPath("$.price").value(nullValue()))
                .andExpect(jsonPath("$.image").doesNotExist())
                .andExpect(jsonPath("$.recommendationEligibility").value("NOT_EVALUATED"))
                .andExpect(jsonPath("$.recordedUse.availability").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.recordedUse.count").doesNotExist())
                .andExpect(jsonPath("$.recordedUse.lastWornAt").doesNotExist())
                .andExpect(jsonPath("$.recordedUse.wearCount").doesNotExist());
    }

    @Test
    void theDetailOfAGarmentChosenFromTheOverviewIsThatGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("handoff@example.com");
        addGarmentAndReturnId(tokens.accessToken(), garment("SHOES", "RED", "subtype", "SNEAKERS"));
        String overview = mockMvc.perform(get(GARMENTS)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String selectedId = JsonPath.read(overview, "$.garments[0].id");

        inspect(tokens.accessToken(), UUID.fromString(selectedId))
                .andExpect(jsonPath("$.id").value(selectedId))
                .andExpect(jsonPath("$.subtype").value("SNEAKERS"));
    }

    @Test
    void anotherAccountsGarmentLooksExactlyLikeAnUnknownOne() throws Exception {
        IssuedTokens alice = registerAndLogIn("alice@example.com");
        IssuedTokens bob = registerAndLogIn("bob@example.com");
        UUID bobsGarment = addGarmentAndReturnId(bob.accessToken(), garment("TOPS", "RED", "brand", "Private"));

        String forBobsId = inspect(alice.accessToken(), bobsGarment)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String forUnknownId = inspect(alice.accessToken(), UUID.randomUUID())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(withoutInstance(forBobsId)).isEqualTo(withoutInstance(forUnknownId));
        assertThat(forBobsId).doesNotContain("Private").doesNotContain(bob.accountId().toString());
    }

    /** A 410 here would confirm that another account once owned this garment. */
    @Test
    void anotherAccountsRemovedGarmentIsNotFoundRatherThanGone() throws Exception {
        IssuedTokens alice = registerAndLogIn("alice-removed@example.com");
        IssuedTokens bob = registerAndLogIn("bob-removed@example.com");
        UUID bobsGarment = addGarmentAndReturnId(bob.accessToken(), minimumGarment("BAGS", "RED"));
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at WHERE id = :id")
                .param("id", bobsGarment).update();

        String forRemoved = inspect(alice.accessToken(), bobsGarment)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String forUnknown = inspect(alice.accessToken(), UUID.randomUUID())
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(withoutInstance(forRemoved)).isEqualTo(withoutInstance(forUnknown));
    }

    /** Includes {@code 1-1-1-1-1}, which {@code UUID.fromString} alone would accept. */
    @Test
    void everyMalformedIdShapeIsNotFoundRatherThanAClientOrServerError() throws Exception {
        IssuedTokens tokens = registerAndLogIn("malformed-shapes@example.com");
        String valid = UUID.randomUUID().toString();

        for (String malformed : List.of("1-1-1-1-1", valid + "0", valid.substring(1), valid.replace("-", ""),
                valid.substring(0, 35) + "g", "x".repeat(500), "-", "null")) {
            mockMvc.perform(get(GARMENTS + "/" + malformed)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"));
        }
    }

    @Test
    void valuesAtTheirEdgesAreReadBackExactlyAsTheyWereAccepted() throws Exception {
        IssuedTokens tokens = registerAndLogIn("edge-values@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY",
                "subtype", "TANKS_AND_CAMIS",
                "styles", List.of("STREET", "CASUAL", "STREET"),
                "brand", "  Maison  Ébène ",
                "price", Map.of("amount", "0", "currency", "VND")));

        inspect(tokens.accessToken(), id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtype").value("TANKS_AND_CAMIS"))
                .andExpect(jsonPath("$.styles.length()").value(2))
                .andExpect(jsonPath("$.styles[0]").value("CASUAL"))
                .andExpect(jsonPath("$.styles[1]").value("STREET"))
                .andExpect(jsonPath("$.brand").value("  Maison  Ébène "))
                .andExpect(jsonPath("$.price.amount").value("0"))
                .andExpect(jsonPath("$.price.currency").value("VND"));
    }

    @Test
    void aMalformedOrTamperedTokenExposesNoGarmentDetail() throws Exception {
        IssuedTokens tokens = registerAndLogIn("tampered-detail@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "RED"));
        String token = tokens.accessToken();
        String tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");

        for (String invalid : List.of("not-a-jwt", tampered)) {
            expectAuthenticationRequired(inspect(invalid, id));
        }
    }

    @Test
    void aMalformedIdIsNotFound() throws Exception {
        IssuedTokens tokens = registerAndLogIn("malformed-id@example.com");

        mockMvc.perform(get(GARMENTS + "/not-a-garment-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"));
    }

    @Test
    void aRemovedGarmentIsNoLongerCurrentRatherThanMissingOrStillOwned() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removed-detail@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("OUTERWEAR", "NAVY"));
        // Controlled fixture: Sprint 2 has no removal feature (PBI-016), only the stored meaning.
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at WHERE id = :id").param("id", id).update();

        inspect(tokens.accessToken(), id)
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("GARMENT_NO_LONGER_CURRENT"))
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    @Test
    void inspectingChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("read-only-detail@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "materials", List.of("SILK")));
        List<Map<String, Object>> before = jdbc.sql("SELECT * FROM wardrobe.wardrobe_garment ORDER BY id").query().listOfRows();

        inspect(tokens.accessToken(), id).andExpect(status().isOk());

        assertThat(jdbc.sql("SELECT * FROM wardrobe.wardrobe_garment ORDER BY id").query().listOfRows()).isEqualTo(before);
        assertThat(countRows("wardrobe.wardrobe_garment_material")).isOne();
    }

    @Test
    void unusableAccessExposesNoGarmentDetail() throws Exception {
        IssuedTokens loggedOut = registerAndLogIn("logged-out-detail@example.com");
        UUID id = addGarmentAndReturnId(loggedOut.accessToken(), minimumGarment("SHOES", "RED"));
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loggedOut.accessToken()))
                .andExpect(status().isNoContent());
        expectAuthenticationRequired(inspect(loggedOut.accessToken(), id));

        IssuedTokens expired = logIn("logged-out-detail@example.com", PASSWORD);
        clock.advance(Duration.ofMinutes(15));
        expectAuthenticationRequired(inspect(expired.accessToken(), id));
        expectAuthenticationRequired(mockMvc.perform(get(GARMENTS + "/" + id)));
    }

    private ResultActions inspect(String accessToken, UUID garmentId) throws Exception {
        return mockMvc.perform(get(GARMENTS + "/" + garmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private static void expectAuthenticationRequired(ResultActions response) throws Exception {
        response.andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    /** The problem's instance is the requested path, which differs by id; everything else must match. */
    private static String withoutInstance(String problemJson) {
        return problemJson.replaceAll("\"instance\":\"[^\"]*\",?", "");
    }
}
