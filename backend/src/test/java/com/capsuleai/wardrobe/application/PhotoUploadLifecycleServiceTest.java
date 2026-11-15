package com.capsuleai.wardrobe.application;

import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService.CleanupResult;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003 — cancellation, expiry and resumable object deletion (S3-IMG-04/08, AC-03/AC-06). */
class PhotoUploadLifecycleServiceTest {

    private static final Instant START = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();
    private static final PhotoUploadPolicy POLICY = new PhotoUploadPolicy(15_000_000, 512, Duration.ofHours(24),
            Duration.ofMinutes(5), 100, Duration.ofMinutes(10));

    private final MutableClock clock = new MutableClock(START);
    private final InMemoryPhotoUploadRepository uploads = new InMemoryPhotoUploadRepository();
    private final FaultInjectingPhotoStorage storage = new FaultInjectingPhotoStorage();
    private final ImmediateWardrobeTransactions transactions = new ImmediateWardrobeTransactions();
    private final CancelPhotoUploadService cancel = new CancelPhotoUploadService(uploads, transactions, clock, POLICY);
    private final PhotoUploadCleanupService cleanup =
            new PhotoUploadCleanupService(uploads, storage, transactions, clock, POLICY);

    @Test
    void cancellingAReadyPhotoMakesItUnusableAndDeletesItsObjectOnTheNextRun() {
        PhotoUpload photo = storedReadyPhoto();

        cancel.cancel(OWNER, photo.id().toString());
        CleanupResult result = cleanup.run();

        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.DISCARDED);
        assertThat(result.deleted()).isOne();
        assertThat(storage.contains(photo.objectKey())).isFalse();
        assertThat(uploads.objectDeleted(photo.id())).isTrue();
    }

    @Test
    void cancellingTwiceSucceedsAndAnAttachedPhotoCannotBeCancelled() {
        PhotoUpload photo = storedReadyPhoto();
        cancel.cancel(OWNER, photo.id().toString());
        cancel.cancel(OWNER, photo.id().toString());

        PhotoUpload attached = storedReadyPhoto();
        uploads.attach(attached.id(), UUID.randomUUID(), START);
        assertThatThrownBy(() -> cancel.cancel(OWNER, attached.id().toString()))
                .isInstanceOf(PhotoUploadNotUsableException.class);
        assertThat(uploads.findOwned(OWNER, attached.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.ATTACHED);
    }

    @Test
    void anotherOwnersUnknownOrMalformedPhotoCannotBeCancelled() {
        PhotoUpload photo = storedReadyPhoto();

        assertThatThrownBy(() -> cancel.cancel(UUID.randomUUID(), photo.id().toString()))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> cancel.cancel(OWNER, UUID.randomUUID().toString()))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> cancel.cancel(OWNER, "x"))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void anUnconfirmedPhotoExpiresAfterThePendingTtlAndItsObjectIsDeleted() {
        PhotoUpload photo = storedReadyPhoto();

        clock.advance(Duration.ofHours(23));
        assertThat(cleanup.run().expired()).as("not yet expired").isZero();
        clock.advance(Duration.ofHours(1));
        CleanupResult result = cleanup.run();

        assertThat(result.expired()).isOne();
        assertThat(result.deleted()).isOne();
        assertThat(storage.contains(photo.objectKey())).isFalse();
    }

    @Test
    void anObjectOfAPhotoDiscardedWhileReceivingIsNotDeletedBeforeTheInFlightFence() {
        PhotoUpload receiving = PhotoUpload.receiving(UUID.randomUUID(), OWNER, UUID.randomUUID(), "c".repeat(64),
                START, Duration.ofHours(24));
        uploads.addIfUploadKeyUnused(receiving);
        cancel.cancel(OWNER, receiving.id().toString());
        storage.put(receiving.objectKey(), new byte[] {1}); // a store that finished after the cancel

        assertThat(cleanup.run().deleted()).isZero();
        clock.advance(Duration.ofMinutes(5));
        assertThat(cleanup.run().deleted()).isOne();
        assertThat(storage.contains(receiving.objectKey())).isFalse();
    }

    @Test
    void aFailedDeletionIsRetriedAfterTheRetryDelay() {
        PhotoUpload photo = storedReadyPhoto();
        cancel.cancel(OWNER, photo.id().toString());
        storage.failDeletes(true);

        assertThat(cleanup.run().failed()).isOne();
        storage.failDeletes(false);
        assertThat(cleanup.run().deleted()).as("leased until the retry delay").isZero();
        clock.advance(Duration.ofMinutes(10));
        assertThat(cleanup.run().deleted()).isOne();
        assertThat(uploads.objectDeleted(photo.id())).isTrue();
    }

    @Test
    void anAttachedPhotoIsNeverExpiredOrDeleted() {
        PhotoUpload photo = storedReadyPhoto();
        uploads.attach(photo.id(), UUID.randomUUID(), START);

        clock.advance(Duration.ofDays(30));
        CleanupResult result = cleanup.run();

        assertThat(result.expired()).isZero();
        assertThat(result.deleted()).isZero();
        assertThat(storage.contains(photo.objectKey())).isTrue();
        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.ATTACHED);
    }

    private PhotoUpload storedReadyPhoto() {
        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), OWNER, UUID.randomUUID(), "d".repeat(64),
                clock.instant(), POLICY.pendingTtl());
        uploads.addIfUploadKeyUnused(intent);
        storage.store(intent.objectKey(), new byte[] {(byte) 0xFF, (byte) 0xD8}, PhotoUpload.STORED_CONTENT_TYPE);
        uploads.markReady(intent.id(), PhotoUpload.STORED_CONTENT_TYPE, 1024, 768, clock.instant());
        return uploads.findOwned(OWNER, intent.id()).orElseThrow();
    }
}
