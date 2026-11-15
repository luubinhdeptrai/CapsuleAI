package com.capsuleai.wardrobe.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003: a staged photo's lifecycle invariants (S3-IMG-03/04/08). */
class PhotoUploadTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final String SHA = "a".repeat(64);

    @Test
    void aNewIntentIsReceivingWithAnOpaqueServerKeyAndExpiresAfterTheTtl() {
        UUID id = UUID.randomUUID();
        PhotoUpload intent = PhotoUpload.receiving(id, UUID.randomUUID(), UUID.randomUUID(), SHA, NOW,
                Duration.ofHours(24));

        assertThat(intent.status()).isEqualTo(PhotoUploadStatus.RECEIVING);
        assertThat(intent.objectKey()).isEqualTo("photos/" + id + ".jpg").doesNotContain(SHA);
        assertThat(intent.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(intent.garmentId()).isNull();
    }

    @Test
    void aStoredPhotoNeedsItsMetadataAndOnlyAnAttachedPhotoNamesAGarment() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> upload(id, PhotoUploadStatus.READY, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> upload(id, PhotoUploadStatus.ATTACHED, 800, 600, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> upload(id, PhotoUploadStatus.READY, 800, 600, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(upload(id, PhotoUploadStatus.ATTACHED, 800, 600, UUID.randomUUID()).asGarmentPhoto().width())
                .isEqualTo(800);
    }

    @Test
    void onlyAReadyUnexpiredPhotoIsUsable() {
        UUID id = UUID.randomUUID();
        PhotoUpload ready = upload(id, PhotoUploadStatus.READY, 800, 600, null);

        assertThat(ready.isUsableAt(NOW.plusSeconds(60))).isTrue();
        assertThat(ready.isUsableAt(ready.expiresAt())).as("expiry is exclusive").isFalse();
        assertThat(upload(id, PhotoUploadStatus.RECEIVING, null, null, null).isUsableAt(NOW)).isFalse();
        assertThat(upload(id, PhotoUploadStatus.DISCARDED, null, null, null).isUsableAt(NOW)).isFalse();
        assertThat(upload(id, PhotoUploadStatus.ATTACHED, 800, 600, UUID.randomUUID()).isUsableAt(NOW)).isFalse();
    }

    @Test
    void aMalformedHashOrANonPositiveLifetimeIsRejected() {
        assertThatThrownBy(() -> PhotoUpload.receiving(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "NOT-A-HASH", NOW, Duration.ofHours(1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PhotoUpload.receiving(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                SHA, NOW, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }

    private static PhotoUpload upload(UUID id, PhotoUploadStatus status, Integer width, Integer height,
                                      UUID garmentId) {
        String contentType = width == null ? null : PhotoUpload.STORED_CONTENT_TYPE;
        return new PhotoUpload(id, UUID.randomUUID(), UUID.randomUUID(), SHA, PhotoUpload.objectKeyFor(id), status,
                contentType, width, height, NOW, NOW.plus(Duration.ofHours(24)), garmentId);
    }
}
