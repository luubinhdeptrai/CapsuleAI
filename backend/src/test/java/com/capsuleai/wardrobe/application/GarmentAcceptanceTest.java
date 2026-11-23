package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-BULK-001 (OPEN-S4-29): the shared acceptance seam, directly, without a transaction of its own. */
class GarmentAcceptanceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T09:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();

    private final InMemoryGarmentRepository garmentRepository = new InMemoryGarmentRepository();
    private final InMemoryPhotoUploadRepository photoUploadRepository = new InMemoryPhotoUploadRepository();
    private final GarmentAcceptance garmentAcceptance = new GarmentAcceptance(garmentRepository, photoUploadRepository);

    @Test
    void aConfirmedEntryBecomesOneGarmentAndTheSameKeyReplaysIt() {
        ValidatedGarmentEntry entry = new ValidatedGarmentEntry(UUID.randomUUID(), profile("WHITE"), Optional.empty());
        String fingerprint = GarmentCreationFingerprint.of(entry.profile());

        GarmentCreation first = garmentAcceptance.acceptInCurrentTransaction(OWNER, entry, fingerprint, NOW);
        GarmentCreation again = garmentAcceptance.acceptInCurrentTransaction(OWNER, entry, fingerprint, NOW);

        assertThat(first.newlyCreated()).isTrue();
        assertThat(again.newlyCreated()).isFalse();
        assertThat(again.garment().id()).isEqualTo(first.garment().id());
        assertThat(garmentRepository.all()).hasSize(1);
    }

    @Test
    void aPhotoBackedEntryAttachesThePhotoInTheSameUnitOfWork() {
        PhotoUpload photo = readyPhoto(NOW);
        ValidatedGarmentEntry entry = new ValidatedGarmentEntry(UUID.randomUUID(), profile("WHITE"),
                Optional.of(photo.id()));

        GarmentCreation creation = garmentAcceptance.acceptInCurrentTransaction(OWNER, entry,
                GarmentCreationFingerprint.of(entry.profile()), NOW);

        assertThat(creation.garment().photo()).hasValueSatisfying(attached ->
                assertThat(attached.photoUploadId()).isEqualTo(photo.id()));
        PhotoUpload attachedPhoto = photoUploadRepository.findOwned(OWNER, photo.id()).orElseThrow();
        assertThat(attachedPhoto.status()).isEqualTo(PhotoUploadStatus.ATTACHED);
        assertThat(attachedPhoto.garmentId()).isEqualTo(creation.garment().id());
    }

    @Test
    void anUnusableOrUnknownPhotoCreatesNothing() {
        PhotoUpload expired = readyPhoto(NOW.minus(Duration.ofHours(25)));

        assertThatThrownBy(() -> garmentAcceptance.acceptInCurrentTransaction(OWNER,
                new ValidatedGarmentEntry(UUID.randomUUID(), profile("WHITE"), Optional.of(expired.id())), "f", NOW))
                .isInstanceOf(PhotoUploadNotUsableException.class);
        assertThatThrownBy(() -> garmentAcceptance.acceptInCurrentTransaction(OWNER,
                new ValidatedGarmentEntry(UUID.randomUUID(), profile("WHITE"), Optional.of(UUID.randomUUID())), "f",
                NOW))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThat(garmentRepository.all()).isEmpty();
    }

    @Test
    void theSameKeyWithOtherValuesIsAKeyReuse() {
        UUID key = UUID.randomUUID();
        GarmentProfile white = profile("WHITE");
        GarmentProfile red = profile("RED");
        garmentAcceptance.acceptInCurrentTransaction(OWNER, new ValidatedGarmentEntry(key, white, Optional.empty()),
                GarmentCreationFingerprint.of(white), NOW);

        assertThatThrownBy(() -> garmentAcceptance.acceptInCurrentTransaction(OWNER,
                new ValidatedGarmentEntry(key, red, Optional.empty()), GarmentCreationFingerprint.of(red), NOW))
                .isInstanceOf(IdempotencyKeyReusedException.class);
        assertThat(garmentRepository.all()).hasSize(1);
    }

    private static GarmentProfile profile(String color) {
        return GarmentEntryValidator.validateProfile(new GarmentValues("TOPS", null, color, List.of(), null,
                List.of(), null, null, null, null, null, null, null, null));
    }

    /** A READY staged photo created at {@code stagedAt} with a 24-hour lifetime. */
    private PhotoUpload readyPhoto(Instant stagedAt) {
        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), OWNER, UUID.randomUUID(), "c".repeat(64),
                stagedAt, Duration.ofHours(24));
        photoUploadRepository.addIfUploadKeyUnused(intent);
        photoUploadRepository.markReady(intent.id(), PhotoUpload.STORED_CONTENT_TYPE, 1536, 2048, stagedAt);
        return photoUploadRepository.findOwned(OWNER, intent.id()).orElseThrow();
    }
}
