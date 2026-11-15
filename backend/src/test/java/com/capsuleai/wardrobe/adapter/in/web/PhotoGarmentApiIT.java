package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 confirmation through the existing creation route: one garment, one photo, safe retries. */
class PhotoGarmentApiIT extends WardrobeIntegrationTestSupport {

    /** AC-04/AC-05, S3-IMG-03. */
    @Test
    void confirmingWithAReadyPhotoCreatesOneGarmentWithThePhotoAttached() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-photo@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1200, 1600));

        String created = addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(photo,
                "subtype", "T_SHIRTS", "brand", "Uniqlo"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.photo.width").value(1200))
                .andExpect(jsonPath("$.photo.height").value(1600))
                .andExpect(jsonPath("$.recommendationEligibility").value("NOT_EVALUATED"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String garmentId = JsonPath.read(created, "$.id");

        byte[] served = mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(served).isEqualTo(photoStorage.content(PhotoUpload.objectKeyFor(photo)));
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
    }

    /** AC-06, FR-AI-010: a lost confirmation response is recovered without a duplicate garment. */
    @Test
    void aLostConfirmationResponseIsRecoveredByRepeatingTheSameRequest() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-retry@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String key = UUID.randomUUID().toString();
        String first = addGarment(tokens.accessToken(), key, withPhoto(photo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        addGarment(tokens.accessToken(), key, withPhoto(photo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(JsonPath.<String>read(first, "$.id")));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /** S3-IMG-04: one key cannot create garments with different photos, or with and without one. */
    @Test
    void reusingTheKeyWithoutThePhotoOrWithAnotherPhotoIsRefused() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-key-reuse@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        UUID otherPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(820, 800));
        String key = UUID.randomUUID().toString();
        addGarment(tokens.accessToken(), key, withPhoto(photo)).andExpect(status().isCreated());

        addGarment(tokens.accessToken(), key, minimumGarment("TOPS", "WHITE"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        addGarment(tokens.accessToken(), key, withPhoto(otherPhoto))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        assertThat(photoUploadStatus(otherPhoto)).isEqualTo("READY");
    }

    /** AC-06/AC-07: an unusable or foreign photo never yields a garment, and never an imageless one. */
    @Test
    void aCancelledOrAnotherAccountsPhotoNeverProducesAGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-unusable@example.com");
        IssuedTokens stranger = registerAndLogIn("confirm-stranger@example.com");
        UUID cancelled = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + cancelled)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())).andExpect(status().isNoContent());
        UUID strangersPhoto = stagePhotoAndReturnId(stranger.accessToken(), TestImages.jpeg(800, 800));

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(cancelled))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(strangersPhoto))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_FOUND"));
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
        assertThat(photoUploadStatus(strangersPhoto)).isEqualTo("READY");
    }

    /** S3-IMG-03/07: an attached photo belongs to one garment and cannot be cancelled. */
    @Test
    void anAttachedPhotoCannotBeUsedForASecondGarmentOrCancelled() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-attached@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(photo)).andExpect(status().isCreated());

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(photo))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + photo)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isConflict());
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /** AC-06: a missing stored photo hides nothing else about the garment. */
    @Test
    void aMissingStoredPhotoAffectsOnlyThePhotoRoute() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-missing-object@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String garmentId = JsonPath.read(addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                        withPhoto(photo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
        photoStorage.lose(PhotoUpload.objectKeyFor(photo));

        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_PHOTO_UNAVAILABLE"));
        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("WHITE"))
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"));
    }

    /** FR-AI-003 regression: imageless creation is unchanged and needs no storage. */
    @Test
    void imagelessCreationIsUnchangedAndHasNoPhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-imageless@example.com");
        photoStorage.failStores(true);

        String garmentId = JsonPath.read(addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                        minimumGarment("SHOES", "BLACK"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photo").value(nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_PHOTO_UNAVAILABLE"));
    }

    /** S3-IMG-07 with US-GAR-002: editing never changes the photo. */
    @Test
    void editingAPhotoBackedGarmentKeepsItsPhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-edit@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String garmentId = JsonPath.read(addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                        withPhoto(photo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");

        mockMvc.perform(patch(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,\"expectedRevision\":\"1\",\"color\":\"NAVY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("NAVY"))
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"));
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
    }

    private static Map<String, Object> withPhoto(UUID photoUploadId, Object... extraNamesAndValues) {
        Map<String, Object> body = garment("TOPS", "WHITE", extraNamesAndValues);
        body.put("photoUploadId", photoUploadId.toString());
        return body;
    }
}
