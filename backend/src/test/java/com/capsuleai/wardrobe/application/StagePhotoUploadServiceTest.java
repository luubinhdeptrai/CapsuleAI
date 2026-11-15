package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
import com.capsuleai.wardrobe.support.TestImages;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003 — staging rules, retry identity and storage recovery (S3-IMG-02/03/04, FR-AI-012). */
class StagePhotoUploadServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();
    private static final PhotoUploadPolicy POLICY = new PhotoUploadPolicy(15_000_000, 512, Duration.ofHours(24),
            Duration.ofMinutes(5), 100, Duration.ofMinutes(10));

    private final InMemoryPhotoUploadRepository uploads = new InMemoryPhotoUploadRepository();
    private final FaultInjectingPhotoStorage storage = new FaultInjectingPhotoStorage();
    private final FixedInspector inspector = new FixedInspector();
    private final StagePhotoUploadService service = new StagePhotoUploadService(uploads, storage, inspector,
            new ImmediateWardrobeTransactions(), Clock.fixed(NOW, ZoneOffset.UTC), POLICY);

    @Test
    void aValidPhotoIsStagedReadyStoredAndIsNotAGarment() {
        StagedPhoto staged = service.stage(command(UUID.randomUUID().toString(), new byte[] {1, 2, 3}));

        PhotoUpload upload = staged.upload();
        assertThat(staged.newlyStaged()).isTrue();
        assertThat(upload.status()).isEqualTo(PhotoUploadStatus.READY);
        assertThat(upload.contentType()).isEqualTo("image/jpeg");
        assertThat(upload.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(upload.garmentId()).isNull();
        assertThat(storage.contains(upload.objectKey())).isTrue();
    }

    @Test
    void theSizeLimitAcceptsExactly15000000BytesAndRejectsOneMore() {
        assertThat(service.stage(command(UUID.randomUUID().toString(), new byte[15_000_000])).upload().status())
                .isEqualTo(PhotoUploadStatus.READY);
        int inspectionsBefore = inspector.calls.get();

        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[15_000_001])))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.TOO_LARGE));
        assertThat(inspector.calls.get()).as("an oversized photo is never decoded").isEqualTo(inspectionsBefore);
        assertThat(uploads.size()).isOne();
    }

    @Test
    void theShortestSideOfTheOriginalMustBeAtLeast512Pixels() {
        inspector.originalSize(511, 800);
        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[] {1})))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.TOO_SMALL));
        assertThat(uploads.size()).isZero();
        assertThat(storage.objectCount()).isZero();

        inspector.originalSize(512, 800);
        assertThat(service.stage(command(UUID.randomUUID().toString(), new byte[] {1})).upload().status())
                .isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void aRetryWithTheSameKeyAndBytesReturnsTheSameStagedPhoto() {
        String key = UUID.randomUUID().toString();
        StagedPhoto first = service.stage(command(key, new byte[] {9, 9, 9}));

        StagedPhoto retry = service.stage(command(key, new byte[] {9, 9, 9}));

        assertThat(retry.newlyStaged()).isFalse();
        assertThat(retry.upload().id()).isEqualTo(first.upload().id());
        assertThat(inspector.calls.get()).isOne();
        assertThat(storage.objectCount()).isOne();
    }

    @Test
    void theSameKeyWithDifferentBytesIsRefused() {
        String key = UUID.randomUUID().toString();
        service.stage(command(key, new byte[] {1}));

        assertThatThrownBy(() -> service.stage(command(key, new byte[] {2})))
                .isInstanceOf(PhotoUploadKeyReusedException.class);
        assertThat(uploads.size()).isOne();
    }

    @Test
    void aMissingPhotoAndAMissingOrMalformedKeyAreFieldErrors() {
        assertThatThrownBy(() -> service.stage(new StagePhotoCommand(OWNER, null, null)))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("Idempotency-Key", GarmentValueProblem.REQUIRED),
                                new GarmentFieldViolation("photo", GarmentValueProblem.REQUIRED)));
        assertThatThrownBy(() -> service.stage(command("not-a-uuid", new byte[] {1})))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("Idempotency-Key", GarmentValueProblem.INVALID_FORMAT)));
        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[0])))
                .isInstanceOf(GarmentValidationException.class);
    }

    @Test
    void aRejectedPhotoLeavesNoRowAndNoObject() {
        inspector.reject(PhotoRejectionReason.UNSUPPORTED_FORMAT);

        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[] {1})))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.UNSUPPORTED_FORMAT));
        assertThat(uploads.size()).isZero();
        assertThat(storage.objectCount()).isZero();
    }

    @Test
    void aStorageFailureDiscardsTheIntentBehindTheInFlightFenceAndIsReportedUnavailable() {
        storage.failStores(true);
        String key = UUID.randomUUID().toString();

        assertThatThrownBy(() -> service.stage(command(key, new byte[] {1})))
                .isInstanceOf(PhotoStorageUnavailableException.class);

        PhotoUpload discarded = uploads.findByUploadKey(OWNER, UUID.fromString(key)).orElseThrow();
        assertThat(discarded.status()).isEqualTo(PhotoUploadStatus.DISCARDED);
        assertThat(uploads.nextCleanupAt(discarded.id())).contains(NOW.plus(Duration.ofMinutes(5)));
    }

    @Test
    void aPhotoCancelledWhileItWasBeingStoredIsNotUsableAndItsObjectIsQueuedForDeletion() {
        String key = UUID.randomUUID().toString();
        storage.beforeNextStore(() -> {
            PhotoUpload receiving = uploads.findByUploadKey(OWNER, UUID.fromString(key)).orElseThrow();
            uploads.cancel(OWNER, receiving.id(), NOW, Duration.ofMinutes(5));
        });

        assertThatThrownBy(() -> service.stage(command(key, new byte[] {1})))
                .isInstanceOf(PhotoUploadNotUsableException.class);

        PhotoUpload cancelled = uploads.findByUploadKey(OWNER, UUID.fromString(key)).orElseThrow();
        assertThat(cancelled.status()).isEqualTo(PhotoUploadStatus.DISCARDED);
        assertThat(storage.contains(cancelled.objectKey())).as("the late write landed").isTrue();
        assertThat(uploads.nextCleanupAt(cancelled.id())).as("and is due for deletion now").contains(NOW);
    }

    private static StagePhotoCommand command(String key, byte[] bytes) {
        return new StagePhotoCommand(OWNER, key, TestImages.received(bytes));
    }

    /** A decoder stand-in: a fixed result, or a configured rejection; counts calls. */
    private static final class FixedInspector implements PhotoInspector {

        private final AtomicInteger calls = new AtomicInteger();
        private int originalWidth = 1200;
        private int originalHeight = 1600;
        private PhotoRejectionReason rejection;

        void originalSize(int width, int height) {
            originalWidth = width;
            originalHeight = height;
        }

        void reject(PhotoRejectionReason reason) {
            rejection = reason;
        }

        @Override
        public InspectedPhoto inspectAndNormalize(ReceivedPhoto photo) {
            calls.incrementAndGet();
            if (rejection != null) {
                throw new PhotoRejectedException(rejection);
            }
            return new InspectedPhoto("JPEG", originalWidth, originalHeight, originalWidth, originalHeight,
                    new byte[] {(byte) 0xFF, (byte) 0xD8});
        }
    }
}
