package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003 — explicit confirmation attaches one reviewed photo to exactly one new garment (S3-IMG-03/04). */
class PhotoBackedGarmentCreationTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();

    private final InMemoryGarmentRepository garments = new InMemoryGarmentRepository();
    private final InMemoryPhotoUploadRepository uploads = new InMemoryPhotoUploadRepository();
    private final AddGarmentService service = new AddGarmentService(garments, uploads,
            new ImmediateWardrobeTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aConfirmedGarmentWithAReadyPhotoIsCreatedWithThePhotoAttached() {
        PhotoUpload photo = readyPhoto(OWNER);

        GarmentCreation creation = service.add(command(UUID.randomUUID().toString(), photo.id().toString()));

        assertThat(creation.newlyCreated()).isTrue();
        assertThat(creation.garment().photo()).hasValueSatisfying(attached -> {
            assertThat(attached.photoUploadId()).isEqualTo(photo.id());
            assertThat(attached.width()).isEqualTo(1536);
        });
        PhotoUpload attached = uploads.findOwned(OWNER, photo.id()).orElseThrow();
        assertThat(attached.status()).isEqualTo(PhotoUploadStatus.ATTACHED);
        assertThat(attached.garmentId()).isEqualTo(creation.garment().id());
    }

    @Test
    void repeatingTheSameConfirmationReturnsTheSameGarmentOnce() {
        PhotoUpload photo = readyPhoto(OWNER);
        String key = UUID.randomUUID().toString();
        GarmentCreation first = service.add(command(key, photo.id().toString()));

        GarmentCreation retry = service.add(command(key, photo.id().toString()));

        assertThat(retry.newlyCreated()).isFalse();
        assertThat(retry.garment().id()).isEqualTo(first.garment().id());
        assertThat(garments.all()).hasSize(1);
    }

    @Test
    void theSameKeyWithAnotherPhotoOrWithoutAPhotoIsAKeyReuse() {
        PhotoUpload photo = readyPhoto(OWNER);
        PhotoUpload otherPhoto = readyPhoto(OWNER);
        String key = UUID.randomUUID().toString();
        service.add(command(key, photo.id().toString()));

        assertThatThrownBy(() -> service.add(command(key, otherPhoto.id().toString())))
                .isInstanceOf(IdempotencyKeyReusedException.class);
        assertThatThrownBy(() -> service.add(command(key, null)))
                .isInstanceOf(IdempotencyKeyReusedException.class);
        assertThat(garments.all()).hasSize(1);
        assertThat(uploads.findOwned(OWNER, otherPhoto.id()).orElseThrow().status())
                .isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void anotherOwnersOrAnUnknownPhotoIsNotFoundAndCreatesNothing() {
        PhotoUpload strangersPhoto = readyPhoto(UUID.randomUUID());

        assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), strangersPhoto.id().toString())))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), UUID.randomUUID().toString())))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThat(garments.all()).isEmpty();
    }

    @Test
    void aCancelledExpiredOrAlreadyAttachedPhotoIsNotUsableAndNeverFallsBackToAnImagelessGarment() {
        PhotoUpload cancelled = readyPhoto(OWNER);
        uploads.cancel(OWNER, cancelled.id(), NOW, Duration.ofMinutes(5));
        PhotoUpload expired = photo(OWNER, NOW.minus(Duration.ofHours(25)));
        PhotoUpload attached = readyPhoto(OWNER);
        service.add(command(UUID.randomUUID().toString(), attached.id().toString()));

        for (PhotoUpload unusable : List.of(cancelled, expired, attached)) {
            assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), unusable.id().toString())))
                    .isInstanceOf(PhotoUploadNotUsableException.class);
        }
        assertThat(garments.all()).hasSize(1);
    }

    @Test
    void aMalformedPhotoUploadIdIsAFieldError() {
        assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), "not-a-photo")))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("photoUploadId", GarmentValueProblem.INVALID_FORMAT)));
    }

    @Test
    void anImagelessAdditionStillWorksWithoutAnyPhoto() {
        GarmentCreation creation = service.add(command(UUID.randomUUID().toString(), null));

        assertThat(creation.newlyCreated()).isTrue();
        assertThat(creation.garment().photo()).isEmpty();
        assertThat(uploads.size()).isZero();
    }

    @Test
    void anUnconfirmedPhotoBackedAdditionCreatesNothingAndLeavesThePhotoReady() {
        PhotoUpload photo = readyPhoto(OWNER);
        AddGarmentCommand unconfirmed = new AddGarmentCommand(OWNER, UUID.randomUUID().toString(), false, "TOPS",
                null, "WHITE", List.of(), null, null, null, null, null, null, null, null, null, null,
                photo.id().toString());

        assertThatThrownBy(() -> service.add(unconfirmed)).isInstanceOf(GarmentValidationException.class);
        assertThat(garments.all()).isEmpty();
        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.READY);
    }

    private PhotoUpload readyPhoto(UUID owner) {
        return photo(owner, NOW);
    }

    /** A READY staged photo created at {@code stagedAt} with a 24-hour lifetime. */
    private PhotoUpload photo(UUID owner, Instant stagedAt) {
        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), owner, UUID.randomUUID(), "b".repeat(64),
                stagedAt, Duration.ofHours(24));
        uploads.addIfUploadKeyUnused(intent);
        uploads.markReady(intent.id(), PhotoUpload.STORED_CONTENT_TYPE, 1536, 2048, stagedAt);
        return uploads.findOwned(owner, intent.id()).orElseThrow();
    }

    private static AddGarmentCommand command(String key, String photoUploadId) {
        return new AddGarmentCommand(OWNER, key, true, "TOPS", "T_SHIRTS", "WHITE", List.of("COTTON"), null, null,
                null, null, null, null, null, null, null, null, photoUploadId);
    }
}
