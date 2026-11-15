package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 through the real filter chain, application, PostgreSQL and real JPEG/PNG decoding. */
class PhotoUploadApiIT extends WardrobeIntegrationTestSupport {

    /** AC-02/AC-03, S3-IMG-03/05: staging is reviewable private work, not a garment. */
    @Test
    void aGenuinePhotoIsStagedAsReviewableWorkAndCreatesNoGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-stage@example.com");
        String response = uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(),
                TestImages.jpegWithExif(1200, 1600, 1), "IMG_0001.jpg", "image/jpeg")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.width").value(1200))
                .andExpect(jsonPath("$.height").value(1600))
                .andExpect(jsonPath("$.garmentId").value(org.hamcrest.Matchers.nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        UUID id = UUID.fromString(JsonPath.read(response, "$.id"));

        byte[] reviewed = mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, allOf(containsString("no-store"),
                        containsString("private"))))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(TestImages.containsAscii(reviewed, "GPSLatitude")).isFalse();
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
    }

    /** AC-02, S3-IMG-06: the bytes decide, not the file name or the declared type. */
    @Test
    void theFileNameAndDeclaredTypeAreIgnored() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-sniff@example.com");

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.jpeg(800, 800), "photo.png",
                "image/png").andExpect(status().isCreated());
        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(),
                "not a photo".getBytes(StandardCharsets.UTF_8), "photo.jpg", "image/jpeg")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_UNSUPPORTED_FORMAT"));
    }

    /** AC-02, FR-AI-012: 511 rejected, 512 accepted. */
    @Test
    void theShortestSideBoundaryIs512Pixels() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-boundary@example.com");

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.png(511, 700), "a.png", "image/png")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_TOO_SMALL"));
        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.png(512, 700), "b.png", "image/png")
                .andExpect(status().isCreated());
    }

    /** AC-02, FR-AI-012, OPEN-S3-09: above 15,000,000 bytes is rejected before decoding. */
    @Test
    void aPhotoAboveTheSizeLimitIsRejected() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-large@example.com");

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), new byte[15_000_001], "big.jpg", "image/jpeg")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_TOO_LARGE"));
        assertThat(countRows("wardrobe.wardrobe_photo_upload")).isZero();
    }

    /** S3-IMG-02: a lost upload response is recovered by repeating it; a key cannot carry two photos. */
    @Test
    void aRetryWithTheSameKeyReturnsTheSameStagedPhotoAndADifferentPhotoIsRefused() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-retry@example.com");
        String key = UUID.randomUUID().toString();
        byte[] photo = TestImages.jpeg(900, 900);
        String first = uploadPhoto(tokens.accessToken(), key, photo, "a.jpg", "image/jpeg")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        uploadPhoto(tokens.accessToken(), key, photo, "a.jpg", "image/jpeg")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(JsonPath.<String>read(first, "$.id")));
        uploadPhoto(tokens.accessToken(), key, TestImages.jpeg(901, 900), "b.jpg", "image/jpeg")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_KEY_REUSED"));
        assertThat(photoStorage.objectCount()).isOne();
    }

    @Test
    void aMissingPhotoOrKeyIsAFieldError() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-missing@example.com");

        uploadPhoto(tokens.accessToken(), null, TestImages.jpeg(800, 800), "a.jpg", "image/jpeg")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("Idempotency-Key"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
        mockMvc.perform(post(PHOTO_UPLOADS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .header(IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("photo"));
    }

    /** AC-03, FR-AI-004/009: cancelled work is unusable; cancelling again is harmless. */
    @Test
    void cancellingMakesThePhotoUnusableAndCancellingAgainIsFine() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-cancel@example.com");
        UUID id = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String bearer = "Bearer " + tokens.accessToken();

        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCARDED"));
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNoContent());
    }

    /** AC-07, S3-IMG-05: another account cannot see, review or cancel the photo; it learns nothing. */
    @Test
    void anotherAccountCannotSeeReviewOrCancelAStagedPhoto() throws Exception {
        IssuedTokens owner = registerAndLogIn("photo-owner@example.com");
        IssuedTokens stranger = registerAndLogIn("photo-stranger@example.com");
        UUID id = stagePhotoAndReturnId(owner.accessToken(), TestImages.jpeg(800, 800));
        String bearer = "Bearer " + stranger.accessToken();

        for (var request : java.util.List.of(get(PHOTO_UPLOADS + "/" + id), get(PHOTO_UPLOADS + "/" + id + "/content"),
                delete(PHOTO_UPLOADS + "/" + id), get(PHOTO_UPLOADS + "/not-a-uuid"))) {
            mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearer))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_FOUND"));
        }
        assertThat(photoUploadStatus(id)).isEqualTo("READY");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(id))).isTrue();
    }

    /** AC-07: no usable session, no upload and no read. */
    @Test
    void aMissingTokenOrLoggedOutSessionCannotUploadOrReadPhotos() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-logout@example.com");
        UUID id = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().is2xxSuccessful());

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.jpeg(800, 800), "a.jpg", "image/jpeg")
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content")).andExpect(status().isUnauthorized());
        assertThat(countRows("wardrobe.wardrobe_photo_upload")).isOne();
    }
}
