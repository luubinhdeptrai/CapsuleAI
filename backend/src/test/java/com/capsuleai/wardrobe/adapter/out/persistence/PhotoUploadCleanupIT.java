package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 lifecycle on PostgreSQL (S3-IMG-04/08): expiry, in-flight fence, retried deletion, attached safety. */
class PhotoUploadCleanupIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private PhotoUploadCleanupService cleanupService;

    @Autowired
    private PhotoUploadRepository photoUploads;

    @Autowired
    private WardrobeTransactions transactions;

    @Test
    void anUnconfirmedPhotoExpiresAndItsObjectIsDeletedOnlyWhenDue() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-expiry@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));

        clock.advance(Duration.ofHours(24).minusSeconds(1));
        assertThat(cleanupService.run().expired()).isZero();
        clock.advance(Duration.ofSeconds(1));
        assertThat(cleanupService.run().deleted()).isOne();

        assertThat(photoUploadStatus(photo)).isEqualTo("DISCARDED");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(photo))).isFalse();
        assertThat(jdbc.sql("SELECT object_deleted_at IS NOT NULL FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photo).query(Boolean.class).single()).isTrue();
    }

    @Test
    void anObjectOfAPhotoCancelledWhileReceivingIsKeptUntilTheInFlightFence() {
        UUID owner = UUID.randomUUID();
        PhotoUpload receiving = PhotoUpload.receiving(UUID.randomUUID(), owner, UUID.randomUUID(), "f".repeat(64),
                clock.instant().truncatedTo(ChronoUnit.MICROS), Duration.ofHours(24));
        transactions.inTransaction(() -> photoUploads.addIfUploadKeyUnused(receiving));
        transactions.inTransaction(() -> photoUploads.cancel(owner, receiving.id(), clock.instant(),
                Duration.ofMinutes(5)));
        photoStorage.put(receiving.objectKey(), new byte[] {1}); // the store finished after the cancel

        assertThat(cleanupService.run().deleted()).isZero();
        clock.advance(Duration.ofMinutes(5));
        assertThat(cleanupService.run().deleted()).isOne();
        assertThat(photoStorage.contains(receiving.objectKey())).isFalse();
    }

    @Test
    void aFailedDeletionIsRetriedAfterTheRetryDelayAndThenRecorded() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-retry@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + photo)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())).andExpect(status().isNoContent());
        photoStorage.failDeletes(true);

        assertThat(cleanupService.run().failed()).isOne();
        photoStorage.failDeletes(false);
        assertThat(cleanupService.run().deleted()).as("leased until the retry delay").isZero();
        clock.advance(Duration.ofMinutes(10));
        assertThat(cleanupService.run().deleted()).isOne();

        assertThat(jdbc.sql("SELECT cleanup_attempts FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photo).query(Integer.class).single()).isEqualTo(2);
    }

    @Test
    void attachedPhotosAreNeverExpiredOrDeleted() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-attached@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("HEADWEAR", "GREEN", "photoUploadId", photo.toString())).andExpect(status().isCreated());

        clock.advance(Duration.ofDays(30));
        var result = cleanupService.run();

        assertThat(result.expired()).isZero();
        assertThat(result.deleted()).isZero();
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(photo))).isTrue();
    }
}
