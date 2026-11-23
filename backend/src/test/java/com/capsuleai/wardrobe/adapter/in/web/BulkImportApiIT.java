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
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-BULK-001 through the real filter chain, PostgreSQL and the in-memory photo storage: identified queues,
 * independent item outcomes, no upload-only ownership, explicit per-item confirmation, safe retries, expired
 * photos, cross-account and cross-item protection, cancellation, stop and the admission limits (AC-01–AC-07).
 */
class BulkImportApiIT extends WardrobeIntegrationTestSupport {

    private record StartedBatch(String id, List<String> itemIds) {
    }

    @Test
    void startingABatchQueuesIdentifiedItemsAndARepeatedStartReturnsTheSameBatch() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-start@example.com");
        String key = UUID.randomUUID().toString();

        String first = startBulkImport(tokens.accessToken(), key, Map.of("itemCount", 3))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.itemCount").value(3))
                .andExpect(jsonPath("$.pendingCount").value(3))
                .andExpect(jsonPath("$.stoppedAt").value(nullValue()))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].position").value(1))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("AWAITING_PHOTO"))))
                .andExpect(jsonPath("$.items[*].photoState", everyItem(is("NONE"))))
                .andExpect(jsonPath("$.items[0].garmentId").value(nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        startBulkImport(tokens.accessToken(), key, Map.of("itemCount", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(JsonPath.<String>read(first, "$.id")));
        startBulkImport(tokens.accessToken(), key, Map.of("itemCount", 4))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        assertThat(countRows("wardrobe.wardrobe_bulk_import_batch")).isOne();
        assertThat(countRows("wardrobe.wardrobe_bulk_import_item")).isEqualTo(3);
    }

    @Test
    void aMissingKeyAndAnUnusableItemCountAreReportedTogether() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-invalid-start@example.com");

        startBulkImport(tokens.accessToken(), null, Map.of("itemCount", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'Idempotency-Key')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'itemCount')].code", hasItem("UNSUPPORTED_VALUE")));
        for (Object unusable : List.of(21, 2.5, "3")) {
            startBulkImport(tokens.accessToken(), UUID.randomUUID().toString(), Map.of("itemCount", unusable))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[?(@.field == 'itemCount')].code", hasItem("UNSUPPORTED_VALUE")));
        }
        startBulkImport(tokens.accessToken(), UUID.randomUUID().toString(), Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'itemCount')].code", hasItem("REQUIRED")));
        assertThat(countRows("wardrobe.wardrobe_bulk_import_batch")).isZero();
    }

    @Test
    void aFourthActiveBatchIsRefusedUntilOneIsStopped() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-limit@example.com");
        StartedBatch first = start(tokens.accessToken(), 1);
        start(tokens.accessToken(), 1);
        start(tokens.accessToken(), 1);

        startBulkImport(tokens.accessToken(), UUID.randomUUID().toString(), Map.of("itemCount", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_LIMIT_REACHED"));
        stop(tokens.accessToken(), first).andExpect(status().isOk());

        start(tokens.accessToken(), 1);
        mockMvc.perform(get(BULK_IMPORTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batches", hasSize(3)));
    }

    @Test
    void onlyAConfirmedItemBecomesAGarmentAndEachItemKeepsItsOwnOutcome() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-mixed@example.com");
        StartedBatch batch = start(tokens.accessToken(), 3);
        UUID firstPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        UUID secondPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));

        link(tokens.accessToken(), batch, 1, "1", firstPhoto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("REVIEWABLE"))
                .andExpect(jsonPath("$.items[0].photoState").value("READY"))
                .andExpect(jsonPath("$.items[0].revision").value("2"));
        link(tokens.accessToken(), batch, 2, "1", secondPhoto).andExpect(status().isOk());
        assertThat(countGarmentsOf(tokens.accountId())).as("an uploaded photo is not an owned garment").isZero();

        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.items[0].garmentId").value(notNullValue()))
                .andExpect(jsonPath("$.items[0].garmentCurrent").value(true))
                .andExpect(jsonPath("$.items[0].photoUploadId").value(nullValue()))
                .andExpect(jsonPath("$.items[1].status").value("REVIEWABLE"))
                .andExpect(jsonPath("$.items[2].status").value("AWAITING_PHOTO"))
                .andExpect(jsonPath("$.acceptedCount").value(1))
                .andExpect(jsonPath("$.pendingCount").value(2));
        assertThat(photoUploadStatus(firstPhoto)).isEqualTo("ATTACHED");
        assertThat(jdbc.sql("SELECT brand FROM wardrobe.wardrobe_garment WHERE owner_account_id = :owner")
                .param("owner", tokens.accountId()).query(String.class).single()).isEqualTo("Uniqlo");

        accept(tokens.accessToken(), batch, 3, "1", reviewed("NAVY"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_ITEM_NOT_READY"));
        Map<String, Object> unconfirmed = reviewed("NAVY");
        unconfirmed.put("confirmed", false);
        accept(tokens.accessToken(), batch, 2, "2", unconfirmed)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code", hasItem("NOT_CONFIRMED")));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void anInvalidMiddleItemFailsAloneAndTheOthersStillSucceed() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-partial@example.com");
        StartedBatch batch = start(tokens.accessToken(), 3);
        for (int position = 1; position <= 3; position++) {
            link(tokens.accessToken(), batch, position, "1",
                    stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768))).andExpect(status().isOk());
        }
        Map<String, Object> invalid = reviewed("NAVY");
        invalid.remove("color");
        invalid.put("subtype", "JEANS");

        accept(tokens.accessToken(), batch, 2, "2", invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'color')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'subtype')].code", hasItem("NOT_IN_CATEGORY")));
        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY")).andExpect(status().isOk());
        accept(tokens.accessToken(), batch, 3, "2", reviewed("WHITE")).andExpect(status().isOk());
        view(tokens.accessToken(), batch)
                .andExpect(jsonPath("$.items[1].status").value("REVIEWABLE"))
                .andExpect(jsonPath("$.items[1].revision").value("2"))
                .andExpect(jsonPath("$.acceptedCount").value(2));

        accept(tokens.accessToken(), batch, 2, "2", reviewed("RED")).andExpect(status().isOk());
        assertThat(countGarmentsOf(tokens.accountId())).isEqualTo(3);
    }

    @Test
    void aRetriedConfirmationReturnsTheSameGarmentAndOtherValuesAreRefused() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-retry@example.com");
        StartedBatch batch = start(tokens.accessToken(), 1);
        link(tokens.accessToken(), batch, 1, "1", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768)));
        String accepted = accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String garmentId = JsonPath.read(accepted, "$.items[0].garmentId");

        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].garmentId").value(garmentId));
        accept(tokens.accessToken(), batch, 1, "2", reviewed("RED"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_CLOSED"));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void aReplayAfterTheGarmentWasRemovedShowsItIsNoLongerCurrentAndCreatesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-removed@example.com");
        StartedBatch batch = start(tokens.accessToken(), 1);
        link(tokens.accessToken(), batch, 1, "1", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768)));
        String accepted = accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String garmentId = JsonPath.read(accepted, "$.items[0].garmentId");
        postBulkImportCommand(tokens.accessToken(), GARMENTS + "/" + garmentId + "/removal",
                Map.of("confirmed", true, "expectedRevision", "1"))
                .andExpect(status().isOk());

        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.items[0].garmentId").value(garmentId))
                .andExpect(jsonPath("$.items[0].garmentCurrent").value(false));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void anExpiredPhotoIsShownAndRefusedUntilANewPhotoIsLinked() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-expired-photo@example.com");
        StartedBatch batch = start(tokens.accessToken(), 1);
        UUID expiringPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        link(tokens.accessToken(), batch, 1, "1", expiringPhoto).andExpect(status().isOk());

        clock.advance(Duration.ofHours(25));
        // Access tokens live 15 minutes (Identity), so the user signs in again after the clock has moved.
        String laterAccessToken = logIn("bulk-expired-photo@example.com", PASSWORD).accessToken();
        view(laterAccessToken, batch).andExpect(jsonPath("$.items[0].photoState").value("EXPIRED"));
        accept(laterAccessToken, batch, 1, "2", reviewed("NAVY"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));

        UUID freshPhoto = stagePhotoAndReturnId(laterAccessToken, TestImages.jpeg(1024, 768));
        link(laterAccessToken, batch, 1, "2", freshPhoto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].photoState").value("READY"))
                .andExpect(jsonPath("$.items[0].revision").value("3"));
        assertThat(photoUploadStatus(expiringPhoto)).as("the replaced stage is discarded").isEqualTo("DISCARDED");
        accept(laterAccessToken, batch, 1, "3", reviewed("NAVY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("ACCEPTED"));
    }

    @Test
    void aStagedPhotoBelongsToOneItemWhileTwoStagingsOfTheSameImageAreSeparateIntents() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-same-image@example.com");
        StartedBatch batch = start(tokens.accessToken(), 2);
        byte[] sameImage = TestImages.jpeg(1024, 768);
        UUID firstStaging = stagePhotoAndReturnId(tokens.accessToken(), sameImage);
        link(tokens.accessToken(), batch, 1, "1", firstStaging).andExpect(status().isOk());

        link(tokens.accessToken(), batch, 2, "1", firstStaging)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
        link(tokens.accessToken(), batch, 2, "1", stagePhotoAndReturnId(tokens.accessToken(), sameImage))
                .andExpect(status().isOk());
        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY")).andExpect(status().isOk());
        accept(tokens.accessToken(), batch, 2, "2", reviewed("NAVY")).andExpect(status().isOk());

        assertThat(countGarmentsOf(tokens.accountId())).isEqualTo(2);
    }

    @Test
    void noOtherAccountCanSeeOrChangeABatchOrLendItAPhoto() throws Exception {
        IssuedTokens owner = registerAndLogIn("bulk-owner@example.com");
        IssuedTokens other = registerAndLogIn("bulk-other@example.com");
        StartedBatch batch = start(owner.accessToken(), 1);
        UUID othersPhoto = stagePhotoAndReturnId(other.accessToken(), TestImages.jpeg(1024, 768));

        view(other.accessToken(), batch)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_NOT_FOUND"));
        link(other.accessToken(), batch, 1, "1", othersPhoto).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_NOT_FOUND"));
        accept(other.accessToken(), batch, 1, "1", reviewed("NAVY")).andExpect(status().isNotFound());
        stop(other.accessToken(), batch).andExpect(status().isNotFound());
        mockMvc.perform(get(BULK_IMPORTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + other.accessToken()))
                .andExpect(jsonPath("$.batches", hasSize(0)));

        link(owner.accessToken(), batch, 1, "1", othersPhoto)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_FOUND"));
        mockMvc.perform(get(BULK_IMPORTS + "/not-a-uuid").header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.accessToken()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(BULK_IMPORTS + "/" + batch.id())).andExpect(status().isUnauthorized());
        mockMvc.perform(post(BULK_IMPORTS + "/" + batch.id() + "/stop")).andExpect(status().isUnauthorized());
    }

    @Test
    void stoppingCancelsEveryUnacceptedItemAndKeepsAcceptedOnes() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-stop@example.com");
        StartedBatch batch = start(tokens.accessToken(), 3);
        link(tokens.accessToken(), batch, 1, "1", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768)));
        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY")).andExpect(status().isOk());
        UUID linkedPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        link(tokens.accessToken(), batch, 2, "1", linkedPhoto).andExpect(status().isOk());

        postBulkImportCommand(tokens.accessToken(), BULK_IMPORTS + "/" + batch.id() + "/stop", Map.of("confirmed", false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code", hasItem("NOT_CONFIRMED")));
        String stopped = stop(tokens.accessToken(), batch)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STOPPED"))
                .andExpect(jsonPath("$.stoppedAt").value(notNullValue()))
                .andExpect(jsonPath("$.items[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.items[1].status").value("CANCELED"))
                .andExpect(jsonPath("$.items[2].status").value("CANCELED"))
                .andExpect(jsonPath("$.pendingCount").value(0))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(photoUploadStatus(linkedPhoto)).isEqualTo("DISCARDED");
        assertThat(countGarmentsOf(tokens.accountId())).isOne();

        stop(tokens.accessToken(), batch)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stoppedAt").value(JsonPath.<String>read(stopped, "$.stoppedAt")));
        accept(tokens.accessToken(), batch, 2, "3", reviewed("NAVY"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_CLOSED"));
        mockMvc.perform(get(BULK_IMPORTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(jsonPath("$.batches", hasSize(0)));
        view(tokens.accessToken(), batch).andExpect(jsonPath("$.status").value("STOPPED"));
    }

    @Test
    void anExpiredBatchTakesNoNewWorkButKeepsItsOutcomes() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-expired-batch@example.com");
        StartedBatch batch = start(tokens.accessToken(), 2);
        link(tokens.accessToken(), batch, 1, "1", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768)));
        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY")).andExpect(status().isOk());

        clock.advance(Duration.ofDays(7));
        String laterAccessToken = logIn("bulk-expired-batch@example.com", PASSWORD).accessToken();

        view(laterAccessToken, batch)
                .andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.items[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.items[0].garmentCurrent").value(true));
        link(laterAccessToken, batch, 2, "1", stagePhotoAndReturnId(laterAccessToken, TestImages.jpeg(1024, 768)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_CLOSED"));
        mockMvc.perform(get(BULK_IMPORTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + laterAccessToken))
                .andExpect(jsonPath("$.batches", hasSize(0)));
    }

    @Test
    void cancellingAnItemDiscardsItsPhotoIsSafeToRepeatAndNeverTouchesAnAcceptedItem() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-cancel@example.com");
        StartedBatch batch = start(tokens.accessToken(), 2);
        UUID linkedPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        link(tokens.accessToken(), batch, 1, "1", linkedPhoto).andExpect(status().isOk());

        cancel(tokens.accessToken(), batch, 1, "1")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        cancel(tokens.accessToken(), batch, 1, "2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("CANCELED"))
                .andExpect(jsonPath("$.items[0].photoState").value("NONE"));
        assertThat(photoUploadStatus(linkedPhoto)).isEqualTo("DISCARDED");
        cancel(tokens.accessToken(), batch, 1, "2").andExpect(status().isOk());

        link(tokens.accessToken(), batch, 2, "1", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768)));
        accept(tokens.accessToken(), batch, 2, "2", reviewed("NAVY")).andExpect(status().isOk());
        cancel(tokens.accessToken(), batch, 2, "3")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_CLOSED"));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /**
     * AC-03/AC-04: each category's applicable properties among the 13 are kept exactly. Materials and Styles keep
     * their distinct values, Brand and Price their text and amount. A minimum-only item stays minimum: no value is
     * invented.
     */
    @Test
    void acceptedItemsKeepEveryReviewedPropertyAndNothingThatWasNotGiven() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-properties@example.com");
        StartedBatch batch = start(tokens.accessToken(), 3);
        for (int position = 1; position <= 3; position++) {
            link(tokens.accessToken(), batch, position, "1",
                    stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768))).andExpect(status().isOk());
        }

        String top = garmentIdAfterAccepting(tokens.accessToken(), batch, 1, garment("TOPS", "COLORFUL",
                "subtype", "POLO_SHIRTS", "materials", List.of("WOOL", "CASHMERE", "WOOL"), "pattern", "HOUNDSTOOTH",
                "styles", List.of("FEMININE", "CLASSIC", "FEMININE"), "fit", "SLIM", "neckline", "SHIRT_COLLAR",
                "sleeveLength", "SHORT_SLEEVE", "length", "WAIST", "brand", "Brand Ẩn Danh",
                "price", Map.of("amount", "12.50", "currency", "USD")));
        String pants = garmentIdAfterAccepting(tokens.accessToken(), batch, 2, garment("PANTS", "DARK_GRAY",
                "subtype", "JEANS", "length", "SHORT", "silhouette", "BOOTCUT"));
        String minimum = garmentIdAfterAccepting(tokens.accessToken(), batch, 3, minimumGarment("BAGS", "BLACK"));

        inspect(tokens.accessToken(), top)
                .andExpect(jsonPath("$.category").value("TOPS"))
                .andExpect(jsonPath("$.subtype").value("POLO_SHIRTS"))
                .andExpect(jsonPath("$.color").value("COLORFUL"))
                .andExpect(jsonPath("$.materials", hasSize(2)))
                .andExpect(jsonPath("$.materials[0]").value("WOOL"))
                .andExpect(jsonPath("$.materials[1]").value("CASHMERE"))
                .andExpect(jsonPath("$.pattern").value("HOUNDSTOOTH"))
                .andExpect(jsonPath("$.styles", hasSize(2)))
                .andExpect(jsonPath("$.styles[0]").value("CLASSIC"))
                .andExpect(jsonPath("$.styles[1]").value("FEMININE"))
                .andExpect(jsonPath("$.fit").value("SLIM"))
                .andExpect(jsonPath("$.neckline").value("SHIRT_COLLAR"))
                .andExpect(jsonPath("$.sleeveLength").value("SHORT_SLEEVE"))
                .andExpect(jsonPath("$.length").value("WAIST"))
                .andExpect(jsonPath("$.silhouette").value(nullValue()))
                .andExpect(jsonPath("$.brand").value("Brand Ẩn Danh"))
                .andExpect(jsonPath("$.price.amount").value("12.50"))
                .andExpect(jsonPath("$.price.currency").value("USD"))
                .andExpect(jsonPath("$.provenance").value("USER_ENTERED"));
        inspect(tokens.accessToken(), pants)
                .andExpect(jsonPath("$.subtype").value("JEANS"))
                .andExpect(jsonPath("$.length").value("SHORT"))
                .andExpect(jsonPath("$.silhouette").value("BOOTCUT"))
                .andExpect(jsonPath("$.fit").value(nullValue()))
                .andExpect(jsonPath("$.sleeveLength").value(nullValue()));
        inspect(tokens.accessToken(), minimum)
                .andExpect(jsonPath("$.subtype").value(nullValue()))
                .andExpect(jsonPath("$.materials").isEmpty())
                .andExpect(jsonPath("$.pattern").value(nullValue()))
                .andExpect(jsonPath("$.styles").isEmpty())
                .andExpect(jsonPath("$.brand").value(nullValue()))
                .andExpect(jsonPath("$.price").value(nullValue()));
        assertThat(countGarmentsOf(tokens.accountId())).isEqualTo(3);
    }

    /** S4-INT-02: linking the photo an item already has, even with the revision read before, changes nothing. */
    @Test
    void aRepeatedLinkOfTheSamePhotoChangesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-relink@example.com");
        StartedBatch batch = start(tokens.accessToken(), 1);
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        link(tokens.accessToken(), batch, 1, "1", photo).andExpect(jsonPath("$.items[0].revision").value("2"));

        link(tokens.accessToken(), batch, 1, "1", photo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].revision").value("2"))
                .andExpect(jsonPath("$.items[0].photoUploadId").value(photo.toString()))
                .andExpect(jsonPath("$.items[0].photoState").value("READY"));

        assertThat(photoUploadStatus(photo)).isEqualTo("READY");
    }

    /**
     * Request problems are reported before any lookup, so an unknown batch gives 400 for a malformed request.
     * An unknown item, or an item of the owner's other batch, is 404. A cancelled item cannot be confirmed.
     */
    @Test
    void requestProblemsComeFirstAndUnknownOrFinishedItemsAreRefused() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-order@example.com");
        StartedBatch batch = start(tokens.accessToken(), 1);
        StartedBatch otherBatch = start(tokens.accessToken(), 1);
        String unknownItem = BULK_IMPORTS + "/" + UUID.randomUUID() + "/items/" + UUID.randomUUID();

        Map<String, Object> unconfirmed = reviewed("NAVY");
        unconfirmed.put("confirmed", false);
        unconfirmed.put("expectedRevision", "1");
        postBulkImportCommand(tokens.accessToken(), unknownItem + "/acceptance", unconfirmed)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code", hasItem("NOT_CONFIRMED")));
        postBulkImportCommand(tokens.accessToken(), unknownItem + "/photo", Map.of("expectedRevision", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'photoUploadId')].code", hasItem("REQUIRED")));
        postBulkImportCommand(tokens.accessToken(), unknownItem + "/cancellation", Map.of("expectedRevision", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'expectedRevision')].code", hasItem("INVALID_FORMAT")));
        postBulkImportCommand(tokens.accessToken(), BULK_IMPORTS + "/" + UUID.randomUUID() + "/stop", Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmed')].code", hasItem("REQUIRED")));

        postBulkImportCommand(tokens.accessToken(), BULK_IMPORTS + "/" + batch.id() + "/items/" + UUID.randomUUID()
                + "/cancellation", Map.of("expectedRevision", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_NOT_FOUND"));
        postBulkImportCommand(tokens.accessToken(), BULK_IMPORTS + "/" + batch.id() + "/items/"
                + otherBatch.itemIds().getFirst() + "/cancellation", Map.of("expectedRevision", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_NOT_FOUND"));

        link(tokens.accessToken(), batch, 1, "1", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768)));
        cancel(tokens.accessToken(), batch, 1, "2").andExpect(status().isOk());
        accept(tokens.accessToken(), batch, 1, "3", reviewed("NAVY"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BULK_IMPORT_CLOSED"));
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
    }

    /** NFR-PRIV-001, M10 automated: no submitted value or staged-photo id appears in an error or in the log. */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void neitherErrorsNorLogsRevealSubmittedValuesOrPhotoIds(CapturedOutput output) throws Exception {
        LoggerFactory.getLogger(BulkImportApiIT.class).info("bulk import log capture is active");
        IssuedTokens tokens = registerAndLogIn("bulk-privacy@example.com");
        StartedBatch batch = start(tokens.accessToken(), 2);
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        UUID otherPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        link(tokens.accessToken(), batch, 1, "1", photo).andExpect(status().isOk());

        Map<String, Object> rejected = reviewed("NAVY");
        rejected.put("brand", "Nhãn Riêng " + "x".repeat(100));
        rejected.put("subtype", "JEANS");
        String rejection = accept(tokens.accessToken(), batch, 1, "2", rejected)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String notUsable = link(tokens.accessToken(), batch, 2, "1", photo)
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Map<String, Object> privateBrand = reviewed("NAVY");
        privateBrand.put("brand", "Nhãn Riêng Bí Mật");
        accept(tokens.accessToken(), batch, 1, "2", privateBrand).andExpect(status().isOk());
        link(tokens.accessToken(), batch, 2, "1", otherPhoto).andExpect(status().isOk());

        assertThat(rejection).doesNotContain("Nhãn Riêng").doesNotContain("JEANS");
        assertThat(notUsable).doesNotContain(photo.toString());
        assertThat(output.getAll())
                .as("the capture must see this class's own log line, or the check below proves nothing")
                .contains("bulk import log capture is active")
                .doesNotContain("Nhãn Riêng")
                .doesNotContain(photo.toString())
                .doesNotContain(otherPhoto.toString());
    }

    /**
     * A stage linked to an item but used by the owner for a single garment belongs to that garment. The item then
     * reads EXPIRED, cannot be confirmed with it, and relinking never takes the photo back from the garment.
     */
    @Test
    void aPhotoUsedForASingleGarmentIsNeverTakenBackByTheItem() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-used-elsewhere@example.com");
        StartedBatch batch = start(tokens.accessToken(), 1);
        UUID sharedStage = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024, 768));
        link(tokens.accessToken(), batch, 1, "1", sharedStage).andExpect(status().isOk());
        Map<String, Object> single = minimumGarment("SHOES", "BLACK");
        single.put("photoUploadId", sharedStage.toString());
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), single).andExpect(status().isCreated());

        view(tokens.accessToken(), batch).andExpect(jsonPath("$.items[0].photoState").value("EXPIRED"));
        accept(tokens.accessToken(), batch, 1, "2", reviewed("NAVY"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
        link(tokens.accessToken(), batch, 1, "2", stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].photoState").value("READY"));

        assertThat(photoUploadStatus(sharedStage)).as("the single garment keeps its photo").isEqualTo("ATTACHED");
        accept(tokens.accessToken(), batch, 1, "3", reviewed("NAVY")).andExpect(status().isOk());
        assertThat(countGarmentsOf(tokens.accountId())).isEqualTo(2);
    }

    /** OPEN-S4-30: the resume list shows the owner's active batches, newest first. */
    @Test
    void theResumeListShowsActiveBatchesNewestFirst() throws Exception {
        IssuedTokens tokens = registerAndLogIn("bulk-resume@example.com");
        StartedBatch oldest = start(tokens.accessToken(), 1);
        clock.advance(Duration.ofSeconds(1));
        StartedBatch middle = start(tokens.accessToken(), 2);
        clock.advance(Duration.ofSeconds(1));
        StartedBatch newest = start(tokens.accessToken(), 3);

        mockMvc.perform(get(BULK_IMPORTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batches", hasSize(3)))
                .andExpect(jsonPath("$.batches[0].id").value(newest.id()))
                .andExpect(jsonPath("$.batches[1].id").value(middle.id()))
                .andExpect(jsonPath("$.batches[2].id").value(oldest.id()))
                .andExpect(jsonPath("$.batches[0].items", hasSize(3)));
    }

    private String garmentIdAfterAccepting(String accessToken, StartedBatch batch, int position,
                                           Map<String, Object> reviewedValues) throws Exception {
        String body = accept(accessToken, batch, position, "2", reviewedValues)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[" + (position - 1) + "].status").value("ACCEPTED"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(body, "$.items[" + (position - 1) + "].garmentId");
    }

    private ResultActions inspect(String accessToken, String garmentId) throws Exception {
        return mockMvc.perform(get(GARMENTS + "/" + garmentId).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private StartedBatch start(String accessToken, int itemCount) throws Exception {
        String body = startBulkImport(accessToken, UUID.randomUUID().toString(), Map.of("itemCount", itemCount))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> itemIds = JsonPath.read(body, "$.items[*].id");
        return new StartedBatch(JsonPath.read(body, "$.id"), List.copyOf(itemIds));
    }

    private ResultActions view(String accessToken, StartedBatch batch) throws Exception {
        return mockMvc.perform(get(BULK_IMPORTS + "/" + batch.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private ResultActions link(String accessToken, StartedBatch batch, int position, String expectedRevision,
                               UUID photoUploadId) throws Exception {
        return postBulkImportCommand(accessToken, itemPath(batch, position, "photo"),
                Map.of("expectedRevision", expectedRevision, "photoUploadId", photoUploadId.toString()));
    }

    private ResultActions accept(String accessToken, StartedBatch batch, int position, String expectedRevision,
                                 Map<String, Object> reviewedValues) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>(reviewedValues);
        body.put("expectedRevision", expectedRevision);
        return postBulkImportCommand(accessToken, itemPath(batch, position, "acceptance"), body);
    }

    private ResultActions cancel(String accessToken, StartedBatch batch, int position, String expectedRevision)
            throws Exception {
        return postBulkImportCommand(accessToken, itemPath(batch, position, "cancellation"),
                Map.of("expectedRevision", expectedRevision));
    }

    private ResultActions stop(String accessToken, StartedBatch batch) throws Exception {
        return postBulkImportCommand(accessToken, BULK_IMPORTS + "/" + batch.id() + "/stop", Map.of("confirmed", true));
    }

    private static String itemPath(StartedBatch batch, int position, String command) {
        return BULK_IMPORTS + "/" + batch.id() + "/items/" + batch.itemIds().get(position - 1) + "/" + command;
    }

    /** Reviewed values of one item: confirmed, Category and Color plus optional details (AC-03). */
    private static Map<String, Object> reviewed(String color) {
        return garment("TOPS", color, "materials", List.of("COTTON"), "styles", List.of("CASUAL"), "brand", "Uniqlo");
    }
}
