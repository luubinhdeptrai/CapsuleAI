package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-001 through the real protected HTTP API, filter chain and PostgreSQL. */
class AddGarmentApiIT extends WardrobeIntegrationTestSupport {

    @ParameterizedTest
    @EnumSource(GarmentCategory.class)
    void theConfirmedMinimumSavesOneImagelessGarmentInEveryCategory(GarmentCategory category) throws Exception {
        IssuedTokens tokens = registerAndLogIn("minimum@example.com");

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), minimumGarment(category.name(), "BLACK"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.category").value(category.name()))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.subtype").value(nullValue()))
                .andExpect(jsonPath("$.materials").isEmpty())
                .andExpect(jsonPath("$.pattern").value(nullValue()))
                .andExpect(jsonPath("$.styles").isEmpty())
                .andExpect(jsonPath("$.style").doesNotExist())
                .andExpect(jsonPath("$.fit").value(nullValue()))
                .andExpect(jsonPath("$.length").value(nullValue()))
                .andExpect(jsonPath("$.brand").value(nullValue()))
                .andExpect(jsonPath("$.price").value(nullValue()))
                .andExpect(jsonPath("$.provenance").value("USER_ENTERED"))
                .andExpect(jsonPath("$.recommendationEligibility").value("NOT_EVALUATED"))
                .andExpect(jsonPath("$.image").doesNotExist());

        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void aFullyDescribedTopKeepsEveryAcceptedValueExactly() throws Exception {
        IssuedTokens tokens = registerAndLogIn("tops@example.com");
        Map<String, Object> body = garment("TOPS", "NAVY",
                "subtype", "TANKS_AND_CAMIS",
                "materials", List.of("SPANDEX_ELASTANE", "COTTON"),
                "pattern", "STRIPED",
                "styles", List.of("FORMAL", "BUSINESS_CASUAL", "FORMAL"),
                "fit", "OVERSIZED",
                "neckline", "SCOOP_U",
                "sleeveLength", "THREE_QUARTER_SLEEVE",
                "length", "CROPPED",
                "brand", " Thời Trang Việt ",
                "price", Map.of("amount", "299000", "currency", "VND"));

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtype").value("TANKS_AND_CAMIS"))
                .andExpect(jsonPath("$.materials[0]").value("COTTON"))
                .andExpect(jsonPath("$.materials[1]").value("SPANDEX_ELASTANE"))
                .andExpect(jsonPath("$.pattern").value("STRIPED"))
                .andExpect(jsonPath("$.styles.length()").value(2))
                .andExpect(jsonPath("$.styles[0]").value("BUSINESS_CASUAL"))
                .andExpect(jsonPath("$.styles[1]").value("FORMAL"))
                .andExpect(jsonPath("$.fit").value("OVERSIZED"))
                .andExpect(jsonPath("$.neckline").value("SCOOP_U"))
                .andExpect(jsonPath("$.sleeveLength").value("THREE_QUARTER_SLEEVE"))
                .andExpect(jsonPath("$.length").value("CROPPED"))
                .andExpect(jsonPath("$.silhouette").value(nullValue()))
                .andExpect(jsonPath("$.brand").value(" Thời Trang Việt "))
                .andExpect(jsonPath("$.price.amount").value("299000"))
                .andExpect(jsonPath("$.price.currency").value("VND"));
    }

    @Test
    void aFullyDescribedPairOfPantsUsesThePantsLengthAndSilhouette() throws Exception {
        IssuedTokens tokens = registerAndLogIn("pants@example.com");

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), garment("PANTS", "KHAKI",
                "subtype", "TROUSERS", "length", "LONG", "silhouette", "WIDE_LEG",
                "price", Map.of("amount", "12.50", "currency", "USD")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length").value("LONG"))
                .andExpect(jsonPath("$.silhouette").value("WIDE_LEG"))
                .andExpect(jsonPath("$.fit").value(nullValue()))
                .andExpect(jsonPath("$.price.amount").value("12.50"));
    }

    @Test
    void inapplicableDetailsAreIdentifiedForCorrectionAndNothingIsSaved() throws Exception {
        IssuedTokens tokens = registerAndLogIn("shoes@example.com");

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("SHOES", "WHITE", "fit", "SLIM", "silhouette", "FLARE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fit"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("NOT_APPLICABLE"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("silhouette"));

        assertThat(countGarmentsOf(tokens.accountId())).isZero();
    }

    @Test
    void aMissingOrUnconfirmedMinimumCreatesNoGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("draft@example.com");

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), Map.of("materials", List.of("WOOL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(3));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                Map.of("confirmed", false, "category", "TOPS", "color", "RED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].code").value("NOT_CONFIRMED"));

        assertThat(countGarmentsOf(tokens.accountId())).isZero();
    }

    @Test
    void unsupportedValuesAreNotSilentlyReplaced() throws Exception {
        IssuedTokens tokens = registerAndLogIn("vocabulary@example.com");

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("PANTS", "BLUE", "subtype", "T_SHIRTS", "styles", List.of("CASUAL", "ETC"), "price",
                        Map.of("amount", "100")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("subtype"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("NOT_IN_CATEGORY"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("styles[1]"))
                .andExpect(jsonPath("$.fieldErrors[1].code").value("UNSUPPORTED_VALUE"))
                .andExpect(jsonPath("$.fieldErrors[2].field").value("price"))
                .andExpect(jsonPath("$.fieldErrors[2].code").value("INCOMPLETE_PRICE"));
    }

    @Test
    void aRetryWithTheSameStylesInAnotherOrderIsTheSameAddition() throws Exception {
        IssuedTokens tokens = registerAndLogIn("styles-retry@example.com");
        String key = UUID.randomUUID().toString();

        addGarment(tokens.accessToken(), key, garment("OUTERWEAR", "CAMEL", "styles", List.of("CLASSIC", "LUXURY")))
                .andExpect(status().isCreated());
        addGarment(tokens.accessToken(), key, garment("OUTERWEAR", "CAMEL", "styles", List.of("LUXURY", "CLASSIC")))
                .andExpect(status().isOk());
        addGarment(tokens.accessToken(), key, garment("OUTERWEAR", "CAMEL", "styles", List.of("CLASSIC")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));

        assertThat(jdbc.sql("SELECT style FROM wardrobe.wardrobe_garment_style s "
                        + "JOIN wardrobe.wardrobe_garment g ON g.id = s.garment_id "
                        + "WHERE g.owner_account_id = :owner ORDER BY style")
                .param("owner", tokens.accountId()).query(String.class).list()).containsExactly("CLASSIC", "LUXURY");
    }

    @Test
    void aZeroPriceIsSavedAsAKnownZero() throws Exception {
        IssuedTokens tokens = registerAndLogIn("gift@example.com");

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), garment("BAGS", "BLACK",
                "price", Map.of("amount", "0", "currency", "VND")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price.amount").value("0"))
                .andExpect(jsonPath("$.price.currency").value("VND"));
    }

    @Test
    void aRetryOfTheSameAdditionReturnsTheAlreadyAcceptedGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("retry@example.com");
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = garment("SKIRTS", "PINK", "subtype", "MIDI_SKIRTS");

        String first = addGarment(tokens.accessToken(), key, body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String retry = addGarment(tokens.accessToken(), key, body)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(retry).isEqualTo(first);
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void reusingAKeyForADifferentGarmentIsRejectedAndChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("reuse@example.com");
        String key = UUID.randomUUID().toString();
        addGarment(tokens.accessToken(), key, minimumGarment("BAGS", "BLACK")).andExpect(status().isCreated());

        addGarment(tokens.accessToken(), key, minimumGarment("BAGS", "BROWN"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));

        assertThat(jdbc.sql("SELECT color FROM wardrobe.wardrobe_garment WHERE owner_account_id = :owner")
                .param("owner", tokens.accountId()).query(String.class).list()).containsExactly("BLACK");
    }

    @Test
    void twoSeparateAdditionsOfIdenticalLookingGarmentsAreBothKept() throws Exception {
        IssuedTokens tokens = registerAndLogIn("twins@example.com");

        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));

        assertThat(countGarmentsOf(tokens.accountId())).isEqualTo(2);
    }

    @Test
    void anAdditionWithoutAnIdempotencyKeyIsRejected() throws Exception {
        IssuedTokens tokens = registerAndLogIn("no-key@example.com");

        addGarment(tokens.accessToken(), null, minimumGarment("TOPS", "WHITE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("Idempotency-Key"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));

        assertThat(countGarmentsOf(tokens.accountId())).isZero();
    }

    @Test
    void theOwnerIsAlwaysTheAuthenticatedAccountAndKeysAreScopedPerOwner() throws Exception {
        IssuedTokens alice = registerAndLogIn("alice@example.com");
        IssuedTokens bob = registerAndLogIn("bob@example.com");
        String sharedKey = UUID.randomUUID().toString();
        Map<String, Object> body = garment("HEADWEAR", "BEIGE", "ownerAccountId", bob.accountId().toString());

        addGarment(alice.accessToken(), sharedKey, body).andExpect(status().isCreated());
        addGarment(bob.accessToken(), sharedKey, minimumGarment("HEADWEAR", "BEIGE")).andExpect(status().isCreated());

        assertThat(countGarmentsOf(alice.accountId())).isOne();
        assertThat(countGarmentsOf(bob.accountId())).isOne();
    }

    @Test
    void unusableAccessIsDeniedAndSavesNothing() throws Exception {
        IssuedTokens loggedOut = registerAndLogIn("logged-out@example.com");
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loggedOut.accessToken()))
                .andExpect(status().isNoContent());
        IssuedTokens resetRevoked = registerAndLogIn("reset@example.com");
        completePasswordReset(requestPasswordResetToken("reset@example.com"), "a brand new passphrase")
                .andExpect(status().isNoContent());
        for (IssuedTokens revokedButUnexpired : List.of(loggedOut, resetRevoked)) {
            expectAuthenticationRequired(revokedButUnexpired.accessToken());
        }

        IssuedTokens expired = registerAndLogIn("expired@example.com");
        clock.advance(Duration.ofMinutes(15));
        expectAuthenticationRequired(expired.accessToken());

        mockMvc.perform(post(GARMENTS).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,\"category\":\"TOPS\",\"color\":\"RED\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(countRows("wardrobe.wardrobe_garment")).isZero();
    }

    private void expectAuthenticationRequired(String accessToken) throws Exception {
        addGarment(accessToken, UUID.randomUUID().toString(), minimumGarment("TOPS", "RED"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void malformedJsonIsReportedAsSuch() throws Exception {
        IssuedTokens tokens = registerAndLogIn("malformed@example.com");

        mockMvc.perform(post(GARMENTS)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .header(IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
