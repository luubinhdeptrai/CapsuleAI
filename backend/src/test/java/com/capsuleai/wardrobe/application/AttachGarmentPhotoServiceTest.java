package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Provisional US-GAR-006 (S4-GAR-04/05/06, OPEN-S4-07/08): eligibility, replay, refusals and validation. */
class AttachGarmentPhotoServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-11-01T08:00:00Z");
    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");

    private InMemoryPhotoUploadRepository photoUploadRepository;
    private InMemoryGarmentPhotoAttachmentRepository garmentPhotoAttachmentRepository;
    private AttachGarmentPhotoService attachGarmentPhotoService;

    @BeforeEach
    void setUp() {
        photoUploadRepository = new InMemoryPhotoUploadRepository();
        garmentPhotoAttachmentRepository = new InMemoryGarmentPhotoAttachmentRepository(photoUploadRepository);
        attachGarmentPhotoService = new AttachGarmentPhotoService(garmentPhotoAttachmentRepository,
                photoUploadRepository, new ImmediateWardrobeTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void aConfirmedAttachmentOfAReadyPhotoAttachesItAndAdvancesTheRevision() {
        UUID garmentId = addImagelessGarment(OWNER);
        PhotoUpload photo = readyPhoto(OWNER, NOW.minus(Duration.ofHours(1)));

        GarmentPhotoAttachment attachment = attachGarmentPhotoService.attach(command(garmentId, "1", photo.id()));

        assertThat(attachment.newlyAttached()).isTrue();
        assertThat(attachment.garment().revision()).isEqualTo(2);
        assertThat(attachment.garment().photo()).contains(photo.asGarmentPhoto());
        assertThat(photoUploadRepository.findOwned(OWNER, photo.id()).orElseThrow().status())
                .isEqualTo(PhotoUploadStatus.ATTACHED);
        assertThat(garmentPhotoAttachmentRepository.revisionOf(garmentId)).isEqualTo(2);
    }

    @Test
    void repeatingTheSamePairReturnsTheGarmentWithoutAnotherRevision() {
        UUID garmentId = addImagelessGarment(OWNER);
        PhotoUpload photo = readyPhoto(OWNER, NOW.minus(Duration.ofHours(1)));
        attachGarmentPhotoService.attach(command(garmentId, "1", photo.id()));

        GarmentPhotoAttachment repeated = attachGarmentPhotoService.attach(command(garmentId, "1", photo.id()));

        assertThat(repeated.newlyAttached()).isFalse();
        assertThat(repeated.garment().revision()).isEqualTo(2);
        assertThat(repeated.garment().photo()).isPresent();
        assertThat(garmentPhotoAttachmentRepository.revisionOf(garmentId)).isEqualTo(2);
    }

    @Test
    void aGarmentThatAlreadyHasAnotherPhotoIsRefusedAndTheOtherPhotoStaysReady() {
        UUID garmentId = addImagelessGarment(OWNER);
        attachGarmentPhotoService.attach(command(garmentId, "1",
                readyPhoto(OWNER, NOW.minus(Duration.ofHours(1))).id()));
        PhotoUpload second = readyPhoto(OWNER, NOW.minus(Duration.ofMinutes(5)));

        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(garmentId, "2", second.id())))
                .isInstanceOf(GarmentAlreadyHasPhotoException.class);
        assertThat(photoUploadRepository.findOwned(OWNER, second.id()).orElseThrow().status())
                .isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void aStaleRevisionIsAConflictAndNothingIsAttached() {
        UUID garmentId = addImagelessGarment(OWNER);
        PhotoUpload photo = readyPhoto(OWNER, NOW.minus(Duration.ofHours(1)));

        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(garmentId, "7", photo.id())))
                .isInstanceOf(GarmentRevisionConflictException.class);
        assertThat(photoUploadRepository.findOwned(OWNER, photo.id()).orElseThrow().status())
                .isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void anExpiredOrCancelledPhotoIsNotUsable() {
        UUID garmentId = addImagelessGarment(OWNER);
        PhotoUpload expired = readyPhoto(OWNER, NOW.minus(Duration.ofHours(25)));
        PhotoUpload cancelled = readyPhoto(OWNER, NOW.minus(Duration.ofHours(1)));
        photoUploadRepository.cancel(OWNER, cancelled.id(), NOW, Duration.ZERO);

        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(garmentId, "1", expired.id())))
                .isInstanceOf(PhotoUploadNotUsableException.class);
        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(garmentId, "1", cancelled.id())))
                .isInstanceOf(PhotoUploadNotUsableException.class);
        assertThat(garmentPhotoAttachmentRepository.revisionOf(garmentId)).isEqualTo(1);
    }

    @Test
    void anotherOwnersPhotoOrGarmentIsNotFound() {
        UUID otherOwner = UUID.randomUUID();
        UUID ownGarmentId = addImagelessGarment(OWNER);
        UUID foreignGarmentId = addImagelessGarment(otherOwner);
        PhotoUpload ownPhoto = readyPhoto(OWNER, NOW.minus(Duration.ofHours(1)));
        PhotoUpload foreignPhoto = readyPhoto(otherOwner, NOW.minus(Duration.ofHours(1)));

        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(ownGarmentId, "1", foreignPhoto.id())))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(foreignGarmentId, "1", ownPhoto.id())))
                .isInstanceOf(GarmentNotFoundException.class);
    }

    @Test
    void aRemovedGarmentIsNoLongerCurrent() {
        Garment removed = Garment.confirmManualEntry(UUID.randomUUID(), OWNER,
                GarmentProfile.minimum(GarmentCategory.SHOES, GarmentColor.BLACK), CREATED_AT)
                .withRemovalAt(CREATED_AT.plusSeconds(60));
        garmentPhotoAttachmentRepository.add(removed);
        PhotoUpload photo = readyPhoto(OWNER, NOW.minus(Duration.ofHours(1)));

        assertThatThrownBy(() -> attachGarmentPhotoService.attach(command(removed.id(), "2", photo.id())))
                .isInstanceOf(GarmentNoLongerCurrentException.class);
    }

    @Test
    void missingFieldsAreRejectedBeforeAnyLookup() {
        AttachGarmentPhotoCommand empty =
                new AttachGarmentPhotoCommand(OWNER, UUID.randomUUID().toString(), null, null, null);
        AttachGarmentPhotoCommand malformed =
                new AttachGarmentPhotoCommand(OWNER, UUID.randomUUID().toString(), false, "0", "photo-1");

        assertThatThrownBy(() -> attachGarmentPhotoService.attach(empty))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("confirmed", GarmentValueProblem.REQUIRED),
                                new GarmentFieldViolation("expectedRevision", GarmentValueProblem.REQUIRED),
                                new GarmentFieldViolation("photoUploadId", GarmentValueProblem.REQUIRED)));
        assertThatThrownBy(() -> attachGarmentPhotoService.attach(malformed))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("confirmed", GarmentValueProblem.NOT_CONFIRMED),
                                new GarmentFieldViolation("expectedRevision", GarmentValueProblem.INVALID_FORMAT),
                                new GarmentFieldViolation("photoUploadId", GarmentValueProblem.INVALID_FORMAT)));
    }

    private UUID addImagelessGarment(UUID owner) {
        Garment garment = Garment.confirmManualEntry(UUID.randomUUID(), owner,
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.NAVY), CREATED_AT);
        garmentPhotoAttachmentRepository.add(garment);
        return garment.id();
    }

    /** A READY staged photo created at {@code stagedAt} with the approved 24-hour pending TTL. */
    private PhotoUpload readyPhoto(UUID owner, Instant stagedAt) {
        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), owner, UUID.randomUUID(), "c".repeat(64),
                stagedAt, Duration.ofHours(24));
        photoUploadRepository.addIfUploadKeyUnused(intent);
        photoUploadRepository.markReady(intent.id(), PhotoUpload.STORED_CONTENT_TYPE, 1536, 2048, stagedAt);
        return photoUploadRepository.findOwned(owner, intent.id()).orElseThrow();
    }

    private static AttachGarmentPhotoCommand command(UUID garmentId, String expectedRevision, UUID photoUploadId) {
        return new AttachGarmentPhotoCommand(OWNER, garmentId.toString(), true, expectedRevision,
                photoUploadId.toString());
    }
}
