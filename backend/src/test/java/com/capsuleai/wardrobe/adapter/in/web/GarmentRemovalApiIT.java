package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.support.TestImages;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-GAR-005 through the real filter chain and PostgreSQL: confirmed, guarded and monotonic removal,
 * immediate exclusion, non-disclosing ownership, private-photo exclusion and no resurrection by replay
 * (AC-01, AC-02, AC-05, AC-06, AC-07).
 */
class GarmentRemovalApiIT extends WardrobeIntegrationTestSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void aConfirmedRemovalExcludesTheGarmentFromTheOverviewDetailAndEdits() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-basic@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "Uniqlo"));

        removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(garmentId.toString()))
                .andExpect(jsonPath("$.outcome").value("REMOVED"))
                .andExpect(jsonPath("$.state").value("REMOVED_FROM_WARDROBE"))
                .andExpect(jsonPath("$.category").value("TOPS"))
                .andExpect(jsonPath("$.color").value("NAVY"))
                .andExpect(jsonPath("$.removedAt").isString())
                .andExpect(jsonPath("$.brand").doesNotExist());

        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments").isEmpty());
        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("GARMENT_NO_LONGER_CURRENT"));
        mockMvc.perform(patch(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(Map.of("confirmed", true, "expectedRevision", "2",
                                "color", "RED"))))
                .andExpect(status().isGone());
    }

    @Test
    void repeatingTheRemovalAfterALostResponseReportsAlreadyRemovedWithTheOriginalTime() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-repeat@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));
        String firstRemovedAt = JsonPath.read(responseBody(
                removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1")).andExpect(status().isOk())),
                "$.removedAt");

        clock.advance(Duration.ofMinutes(5));
        removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("ALREADY_REMOVED"))
                .andExpect(jsonPath("$.removedAt").value(firstRemovedAt));

        assertThat(jdbc.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(Long.class).single()).isEqualTo(2L);
        assertThat(countRows("wardrobe.wardrobe_garment_removal_cleanup")).isOne();
    }

    @Test
    void aStaleRevisionIsAConflictAndTheGarmentStaysCurrent() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-stale@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        mockMvc.perform(patch(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(Map.of("confirmed", true, "expectedRevision", "1",
                                "color", "RED"))))
                .andExpect(status().isOk());

        removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));

        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("RED"));
    }

    @Test
    void anotherAccountsGarmentIsNotFoundAndStaysCurrent() throws Exception {
        IssuedTokens owner = registerAndLogIn("removal-owner@example.com");
        IssuedTokens intruder = registerAndLogIn("removal-intruder@example.com");
        UUID garmentId = addGarmentAndReturnId(owner.accessToken(), minimumGarment("BAGS", "BROWN"));

        removeGarment(intruder.accessToken(), garmentId, confirmedRemoval("1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"));

        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void missingConfirmationAndRevisionAreRejectedBeforeTheGarmentIsLookedUp() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-missing@example.com");

        removeGarment(tokens.accessToken(), UUID.randomUUID(), Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code").value("REQUIRED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'expectedRevision')].code").value("REQUIRED"));
    }

    @Test
    void anUnconfirmedRemovalChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-unconfirmed@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "KHAKI"));

        removeGarment(tokens.accessToken(), garmentId, Map.of("confirmed", false, "expectedRevision", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code").value("NOT_CONFIRMED"));

        assertThat(isStillCurrent(garmentId)).isTrue();
    }

    @Test
    void withoutAUsableSessionNothingIsRemoved() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-session@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SKIRTS", "BLACK"));

        mockMvc.perform(post(GARMENTS + "/" + garmentId + "/removal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(confirmedRemoval("1"))))
                .andExpect(status().isUnauthorized());

        assertThat(isStillCurrent(garmentId)).isTrue();
        assertThat(countRows("wardrobe.wardrobe_garment_removal_cleanup")).isZero();
    }

    @Test
    void thePhotoOfARemovedGarmentIsNoLongerServed() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-photo@example.com");
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "NAVY", "photoUploadId", photoUploadId.toString()));
        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());

        removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1")).andExpect(status().isOk());

        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isGone());
        assertThat(jdbc.sql("SELECT photo_object_key FROM wardrobe.wardrobe_garment_removal_cleanup "
                        + "WHERE garment_id = :id")
                .param("id", garmentId).query(String.class).single()).startsWith("photos/");
    }

    @Test
    void replayingTheOriginalAdditionAfterRemovalIsGoneAndCreatesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-replay@example.com");
        String creationKey = UUID.randomUUID().toString();
        Map<String, Object> addition = garment("OUTERWEAR", "CAMEL", "brand", "Mango");
        UUID garmentId = UUID.fromString(JsonPath.read(responseBody(
                addGarment(tokens.accessToken(), creationKey, addition).andExpect(status().isCreated())), "$.id"));
        removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1")).andExpect(status().isOk());

        addGarment(tokens.accessToken(), creationKey, addition)
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("GARMENT_NO_LONGER_CURRENT"));
        Map<String, Object> changedAddition = new LinkedHashMap<>(addition);
        changedAddition.put("brand", "Zara");
        addGarment(tokens.accessToken(), creationKey, changedAddition)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));

        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void removalRecordsOneCleanupObligationDueAtOnceWithTheThirtyDayDeadline() throws Exception {
        IssuedTokens tokens = registerAndLogIn("removal-deadline@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "NAVY", "materials", List.of("COTTON")));

        removeGarment(tokens.accessToken(), garmentId, confirmedRemoval("1")).andExpect(status().isOk());

        assertThat(jdbc.sql("""
                        SELECT c.purge_deadline - c.removed_at = INTERVAL '30 days'
                               AND c.next_attempt_at = c.removed_at
                               AND c.removed_at = g.removed_at
                        FROM wardrobe.wardrobe_garment_removal_cleanup c
                        JOIN wardrobe.wardrobe_garment g ON g.id = c.garment_id
                        WHERE c.garment_id = :id
                        """)
                .param("id", garmentId).query(Boolean.class).single()).isTrue();
        assertThat(jdbc.sql("SELECT count(*) FROM wardrobe.wardrobe_garment_material WHERE garment_id = :id")
                .param("id", garmentId).query(Long.class).single())
                .as("rich data stays until the cleanup worker minimizes it").isOne();
    }

    private ResultActions removeGarment(String accessToken, UUID garmentId, Map<String, ?> body) throws Exception {
        return mockMvc.perform(post(GARMENTS + "/" + garmentId + "/removal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }

    private static Map<String, Object> confirmedRemoval(String expectedRevision) {
        return Map.of("confirmed", true, "expectedRevision", expectedRevision);
    }

    private static String responseBody(ResultActions response) throws Exception {
        return response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private boolean isStillCurrent(UUID garmentId) {
        return jdbc.sql("SELECT removed_at IS NULL FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(Boolean.class).single();
    }
}
