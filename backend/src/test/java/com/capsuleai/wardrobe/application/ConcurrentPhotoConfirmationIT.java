package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 with real transactions: the upload row lock serializes confirmations and the expiry sweep. */
class ConcurrentPhotoConfirmationIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private AddGarmentService addGarmentService;

    @Autowired
    private PhotoUploadCleanupService cleanupService;

    @Autowired
    private PhotoUploadRepository photoUploads;

    @Autowired
    private WardrobeTransactions transactions;

    @Test
    void ofTwoConfirmationsOfOnePhotoWithDifferentKeysExactlyOneCreatesAGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-confirm@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(confirm(start, tokens.accountId(), photo));
            Future<String> second = executor.submit(confirm(start, tokens.accountId(), photo));

            assertThat(List.of(first.get(20, SECONDS), second.get(20, SECONDS)))
                    .containsExactlyInAnyOrder("created", "not usable");
        } finally {
            executor.shutdownNow();
        }
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
    }

    @Test
    void theExpirySweepSkipsAPhotoBeingConfirmedAndNeverTouchesAnAttachedOne() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-sweep@example.com");
        UUID attached = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("BAGS", "BLACK", "photoUploadId", attached.toString())).andExpect(status().isCreated());
        UUID beingConfirmed = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Object> confirmation = executor.submit(() -> transactions.inTransaction(() -> {
                photoUploads.lockOwned(tokens.accountId(), beingConfirmed).orElseThrow();
                locked.countDown();
                awaitQuietly(release);
                return null;
            }));
            assertThat(locked.await(10, SECONDS)).isTrue();
            clock.advance(Duration.ofHours(25));

            assertThat(cleanupService.run().expired()).as("locked and attached photos are skipped").isZero();
            assertThat(photoUploadStatus(beingConfirmed)).isEqualTo("READY");

            release.countDown();
            confirmation.get(10, SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
        assertThat(cleanupService.run().expired()).as("once unlocked, the expired photo is discarded").isOne();
        assertThat(photoUploadStatus(attached)).isEqualTo("ATTACHED");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(attached))).isTrue();
    }

    private Callable<String> confirm(CyclicBarrier start, UUID owner, UUID photo) {
        AddGarmentCommand command = new AddGarmentCommand(owner, UUID.randomUUID().toString(), true, "TOPS", null,
                "WHITE", List.of(), null, null, null, null, null, null, null, null, null, null, photo.toString());
        return () -> {
            start.await(10, SECONDS);
            try {
                return addGarmentService.add(command).newlyCreated() ? "created" : "replayed";
            } catch (PhotoUploadNotUsableException notUsable) {
                return "not usable";
            }
        };
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(10, SECONDS)) {
                throw new IllegalStateException("The test never released the lock");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }
}
