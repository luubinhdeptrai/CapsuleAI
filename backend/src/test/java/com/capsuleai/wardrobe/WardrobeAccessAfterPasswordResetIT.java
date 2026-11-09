package com.capsuleai.wardrobe;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 2 cross-story evidence (plan §8, §13): accepted creation, browsing and
 * detail; then an accepted password reset denies every earlier credential on the
 * real wardrobe operations, and new-password login reaches the unchanged wardrobe
 * (US-AUTH-005 AC-04/AC-05, US-GAR-001 AC-13, US-WAR-001 AC-09, US-WAR-002 AC-11).
 */
class WardrobeAccessAfterPasswordResetIT extends WardrobeIntegrationTestSupport {

    private static final String NEW_PASSWORD = "the passphrase after recovery";

    @Test
    void aResetRevokesEveryEarlierCredentialAndPreservesTheWardrobe() throws Exception {
        UUID accountId = registerAccount("recovering@example.com", PASSWORD);
        IssuedTokens phone = logIn("recovering@example.com", PASSWORD);
        IssuedTokens laptop = logIn("recovering@example.com", PASSWORD);
        UUID jacket = addGarmentAndReturnId(phone.accessToken(), garment("OUTERWEAR", "BLACK",
                "subtype", "BIKER_JACKETS", "materials", List.of("LEATHER"), "styles", List.of("STREET", "CLASSIC"),
                "price", Map.of("amount", "4500000", "currency", "VND")));
        UUID sneakers = addGarmentAndReturnId(laptop.accessToken(), minimumGarment("SHOES", "WHITE"));
        String overviewBefore = overview(laptop.accessToken());
        String jacketBefore = detail(phone.accessToken(), jacket);
        String sneakersBefore = detail(laptop.accessToken(), sneakers);
        IssuedTokens otherAccount = registerAndLogIn("unrelated@example.com");
        UUID othersBag = addGarmentAndReturnId(otherAccount.accessToken(), minimumGarment("BAGS", "RED"));
        String othersBagBefore = detail(otherAccount.accessToken(), othersBag);

        completePasswordReset(requestPasswordResetToken("recovering@example.com"), NEW_PASSWORD)
                .andExpect(status().isNoContent());

        for (IssuedTokens revoked : List.of(phone, laptop)) {
            mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + revoked.accessToken()))
                    .andExpect(status().isUnauthorized());
            mockMvc.perform(get(GARMENTS + "/" + jacket)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + revoked.accessToken()))
                    .andExpect(status().isUnauthorized());
            addGarment(revoked.accessToken(), UUID.randomUUID().toString(), minimumGarment("TOPS", "RED"))
                    .andExpect(status().isUnauthorized());
            postJson("/api/v1/auth/refresh", Map.of("refreshToken", revoked.refreshToken()))
                    .andExpect(status().isUnauthorized());
        }

        postJson("/api/v1/auth/login", Map.of("email", "recovering@example.com", "password", PASSWORD))
                .andExpect(status().isUnauthorized());
        assertThat(countGarmentsOf(accountId)).as("revoked credentials created nothing").isEqualTo(2);

        IssuedTokens afterReset = logIn("recovering@example.com", NEW_PASSWORD);
        assertThat(afterReset.accountId()).isEqualTo(accountId);
        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + afterReset.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(2))
                .andExpect(jsonPath("$.garments[*].id").value(
                        containsInAnyOrder(jacket.toString(), sneakers.toString())));
        assertThat(overview(afterReset.accessToken())).isEqualTo(overviewBefore);
        assertThat(detail(afterReset.accessToken(), jacket)).isEqualTo(jacketBefore);
        assertThat(detail(afterReset.accessToken(), sneakers)).isEqualTo(sneakersBefore);
        assertThat(detail(otherAccount.accessToken(), othersBag)).isEqualTo(othersBagBefore);
        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + otherAccount.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments.length()").value(1));
    }

    private String overview(String accessToken) throws Exception {
        return mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String detail(String accessToken, UUID garmentId) throws Exception {
        return mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
