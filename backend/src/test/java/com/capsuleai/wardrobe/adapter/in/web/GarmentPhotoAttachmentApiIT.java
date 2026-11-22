package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Provisional US-GAR-006 through the real filter chain and PostgreSQL: P-AC-01 eligibility, P-AC-02
 * confirmation, P-AC-03 atomic association, P-AC-04 same-pair replay, P-AC-05 private availability,
 * P-AC-06 cleanup safety.
 */
class GarmentPhotoAttachmentApiIT extends WardrobeIntegrationTestSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private PhotoUploadCleanupService photoUploadCleanupService;

    @Test
    void attachingAReviewedPhotoToAnImagelessGarmentShowsItInDetailAndServesIt() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-basic@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "NAVY", "brand", "Uniqlo", "materials", List.of("COTTON")));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1600, 1200));

        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", photoUploadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.brand").value("Uniqlo"))
                .andExpect(jsonPath("$.materials[0]").value("COTTON"));

        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());
        assertThat(photoUploadStatus(photoUploadId)).isEqualTo("ATTACHED");
    }

    @Test
    void repeatingTheSameAttachmentAfterALostResponseChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-repeat@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "WHITE"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", photoUploadId))
                .andExpect(status().isOk());

        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", photoUploadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.photo").isMap());
    }

    @Test
    void aGarmentWithAPhotoNeverReceivesAnotherOne() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-twice@example.com");
        UUID firstPhotoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "NAVY", "photoUploadId", firstPhotoId.toString()));
        UUID secondPhotoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(640, 640));

        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", secondPhotoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("GARMENT_ALREADY_HAS_PHOTO"));

        assertThat(photoUploadStatus(secondPhotoId)).isEqualTo("READY");
        assertThat(photoUploadStatus(firstPhotoId)).isEqualTo("ATTACHED");
    }

    @Test
    void aStaleRevisionIsAConflictAndThePhotoStaysReady() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-stale@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "BLUE"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));

        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("4", photoUploadId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        assertThat(photoUploadStatus(photoUploadId)).isEqualTo("READY");
    }

    @Test
    void aCancelledPhotoCannotBeAttached() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-cancelled@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("BAGS", "BLACK"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + photoUploadId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().is2xxSuccessful());

        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", photoUploadId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
    }

    @Test
    void anotherAccountsPhotoOrGarmentIsNotFound() throws Exception {
        IssuedTokens owner = registerAndLogIn("attach-owner@example.com");
        IssuedTokens other = registerAndLogIn("attach-other@example.com");
        UUID ownGarmentId = addGarmentAndReturnId(owner.accessToken(), minimumGarment("TOPS", "WHITE"));
        UUID foreignGarmentId = addGarmentAndReturnId(other.accessToken(), minimumGarment("TOPS", "WHITE"));
        UUID ownPhotoId = stagePhotoAndReturnId(owner.accessToken(), TestImages.jpeg(800, 600));
        UUID foreignPhotoId = stagePhotoAndReturnId(other.accessToken(), TestImages.jpeg(800, 600));

        attachPhoto(owner.accessToken(), ownGarmentId, confirmedAttachment("1", foreignPhotoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_FOUND"));
        attachPhoto(owner.accessToken(), foreignGarmentId, confirmedAttachment("1", ownPhotoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"));
        assertThat(photoUploadStatus(foreignPhotoId)).isEqualTo("READY");
    }

    @Test
    void aRemovedGarmentCannotReceiveAPhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-removed@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SKIRTS", "RED"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        mockMvc.perform(post(GARMENTS + "/" + garmentId + "/removal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(Map.of("confirmed", true, "expectedRevision", "1"))))
                .andExpect(status().isOk());

        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("2", photoUploadId))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("GARMENT_NO_LONGER_CURRENT"));
        assertThat(photoUploadStatus(photoUploadId)).isEqualTo("READY");
    }

    @Test
    void missingFieldsAreRejectedBeforeAnyLookup() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-missing@example.com");

        attachPhoto(tokens.accessToken(), UUID.randomUUID(), Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code").value("REQUIRED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'expectedRevision')].code").value("REQUIRED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'photoUploadId')].code").value("REQUIRED"));
    }

    @Test
    void aMissingObjectAfterAttachmentLimitsOnlyThePhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-missing-object@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("HEADWEAR", "BEIGE"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", photoUploadId))
                .andExpect(status().isOk());
        photoStorage.lose(jdbc.sql("SELECT object_key FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(String.class).single());

        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_PHOTO_UNAVAILABLE"));
        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("HEADWEAR"));
    }

    @Test
    void pendingCleanupNeverTouchesAnAttachedPhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-cleanup@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("OUTERWEAR", "OLIVE"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        attachPhoto(tokens.accessToken(), garmentId, confirmedAttachment("1", photoUploadId))
                .andExpect(status().isOk());
        String objectKey = jdbc.sql("SELECT object_key FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(String.class).single();

        clock.advance(Duration.ofHours(25));
        photoUploadCleanupService.run();

        assertThat(photoUploadStatus(photoUploadId)).isEqualTo("ATTACHED");
        assertThat(photoStorage.contains(objectKey)).isTrue();
    }

    private ResultActions attachPhoto(String accessToken, UUID garmentId, Map<String, ?> body) throws Exception {
        return mockMvc.perform(post(GARMENTS + "/" + garmentId + "/photo-attachment")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }

    private static Map<String, Object> confirmedAttachment(String expectedRevision, UUID photoUploadId) {
        return Map.of("confirmed", true, "expectedRevision", expectedRevision,
                "photoUploadId", photoUploadId.toString());
    }
}
