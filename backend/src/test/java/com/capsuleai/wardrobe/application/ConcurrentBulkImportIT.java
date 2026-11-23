package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.BulkImportRepository;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository.BatchOverview;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository.ItemOverview;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository.LinkedPhotoState;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.BulkImportBatch;
import com.capsuleai.wardrobe.domain.BulkImportBatchStatus;
import com.capsuleai.wardrobe.domain.BulkImportItem;
import com.capsuleai.wardrobe.domain.BulkImportItemStatus;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

/**
 * US-BULK-001 with real transactions on PostgreSQL READ COMMITTED (S4-BULK-04/07, S4-INT-01, OPEN-S4-25/28): a
 * stop and an acceptance meet at the batch row and leave one coherent outcome; two confirmations of one item
 * create one garment; concurrent starts never exceed the active-batch limit. No deadlock within the timeout.
 */
class ConcurrentBulkImportIT extends WardrobeIntegrationTestSupport {

    private static final GarmentValues REVIEWED = new GarmentValues("TOPS", null, "NAVY", List.of(), null, List.of(),
            null, null, null, null, null, null, null, null);

    @Autowired
    private BulkImportService bulkImportService;

    @Autowired
    private AcceptBulkImportItemService acceptBulkImportItemService;

    @Autowired
    private BulkImportRepository bulkImportRepository;

    @Autowired
    private PhotoUploadRepository photoUploadRepository;

    @Autowired
    private WardrobeTransactions wardrobeTransactions;

    @Autowired
    private PhotoUploadPolicy photoUploadPolicy;

    @Test
    void aStopAndAnAcceptanceOfTheSameItemLeaveOneCoherentOutcome() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-stop@example.com");
        for (int round = 0; round < 5; round++) {
            BatchOverview batch = startWithLinkedPhoto(tokens, round);
            String batchId = batch.batch().id().toString();
            String itemId = batch.items().getFirst().item().id().toString();
            UUID photoId = batch.items().getFirst().item().photoUploadId().orElseThrow();
            CyclicBarrier start = new CyclicBarrier(2);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<String> acceptance = executor.submit(acceptance(start, tokens.accountId(), batchId, itemId));
                Future<String> stop = executor.submit(() -> {
                    start.await(10, SECONDS);
                    bulkImportService.stop(tokens.accountId(), batchId, true);
                    return "stopped";
                });
                assertThat(stop.get(20, SECONDS)).isEqualTo("stopped");
                String acceptanceOutcome = acceptance.get(20, SECONDS);

                BatchOverview after = bulkImportService.view(tokens.accountId(), batchId);
                BulkImportItemStatus itemStatus = after.items().getFirst().item().status();
                if (acceptanceOutcome.equals("accepted")) {
                    assertThat(itemStatus).isEqualTo(BulkImportItemStatus.ACCEPTED);
                    assertThat(photoUploadStatus(photoId)).isEqualTo("ATTACHED");
                } else {
                    assertThat(acceptanceOutcome).isEqualTo("closed");
                    assertThat(itemStatus).isEqualTo(BulkImportItemStatus.CANCELED);
                    assertThat(photoUploadStatus(photoId)).isEqualTo("DISCARDED");
                }
            } finally {
                executor.shutdownNow();
            }
        }
        assertThat(countGarmentsOf(tokens.accountId())).isEqualTo(
                jdbc.sql("SELECT count(*) FROM wardrobe.wardrobe_bulk_import_item WHERE status = 'ACCEPTED'")
                        .query(Long.class).single());
    }

    @Test
    void twoConfirmationsOfOneItemCreateOneGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-double@example.com");
        BatchOverview batch = startWithLinkedPhoto(tokens, 0);
        String batchId = batch.batch().id().toString();
        String itemId = batch.items().getFirst().item().id().toString();
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(acceptance(start, tokens.accountId(), batchId, itemId));
            Future<String> second = executor.submit(acceptance(start, tokens.accountId(), batchId, itemId));

            assertThat(List.of(first.get(20, SECONDS), second.get(20, SECONDS))).containsExactly("accepted", "accepted");
        } finally {
            executor.shutdownNow();
        }
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    @Test
    void concurrentStartsNeverExceedTheActiveBatchLimit() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-limit@example.com");
        int attempts = 6;
        CyclicBarrier start = new CyclicBarrier(attempts);
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        try {
            List<Future<String>> outcomes = new ArrayList<>();
            for (int attempt = 0; attempt < attempts; attempt++) {
                outcomes.add(executor.submit(() -> {
                    start.await(10, SECONDS);
                    try {
                        bulkImportService.create(new CreateBulkImportCommand(tokens.accountId(),
                                UUID.randomUUID().toString(), 1, true));
                        return "started";
                    } catch (BulkImportLimitReachedException limitReached) {
                        return "limit";
                    }
                }));
            }
            List<String> results = new ArrayList<>();
            for (Future<String> outcome : outcomes) {
                results.add(outcome.get(20, SECONDS));
            }
            assertThat(results).filteredOn("started"::equals).hasSize(3);
            assertThat(results).filteredOn("limit"::equals).hasSize(3);
        } finally {
            executor.shutdownNow();
        }
        assertThat(countRows("wardrobe.wardrobe_bulk_import_batch")).isEqualTo(3);
    }

    /**
     * Scenario B: item work holds the batch in share mode, so another item is accepted while one item's command is
     * still in flight; a stop needs the batch exclusively and really waits (seen in pg_stat_activity) until that
     * command ends, then cancels what is unfinished.
     */
    @Test
    void anotherItemIsAcceptedWhileOneItemIsHeldButAStopWaitsForIt() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-parallel@example.com");
        BatchOverview batch = startWithLinkedPhotos(tokens, 2);
        UUID batchId = batch.batch().id();
        UUID heldItem = batch.items().get(0).item().id();
        String otherItem = batch.items().get(1).item().id().toString();
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            Future<Void> inFlight = executor.submit(() -> wardrobeTransactions.inTransaction(() -> {
                bulkImportRepository.lockForItemWork(tokens.accountId(), batchId).orElseThrow();
                bulkImportRepository.lockItem(batchId, heldItem).orElseThrow();
                held.countDown();
                awaitQuietly(release);
                return null;
            }));
            assertThat(held.await(10, SECONDS)).isTrue();

            Future<BatchOverview> acceptance = executor.submit(() -> acceptBulkImportItemService.accept(
                    new AcceptBulkImportItemCommand(tokens.accountId(), batchId.toString(), otherItem, "2", true,
                            REVIEWED)));
            assertThat(acceptance.get(10, SECONDS).items().get(1).item().status())
                    .as("another item is accepted while the first item's command is in flight")
                    .isEqualTo(BulkImportItemStatus.ACCEPTED);

            Future<BatchOverview> stop = executor.submit(() ->
                    bulkImportService.stop(tokens.accountId(), batchId.toString(), true));
            awaitABackendWaitingForALock();
            assertThat(stop.isDone()).as("a stop waits for the in-flight item command").isFalse();
            release.countDown();
            inFlight.get(10, SECONDS);

            BatchOverview stopped = stop.get(10, SECONDS);
            assertThat(stopped.currentStatus()).isEqualTo(BulkImportBatchStatus.STOPPED);
            assertThat(stopped.items()).extracting(item -> item.item().status())
                    .containsExactly(BulkImportItemStatus.CANCELED, BulkImportItemStatus.ACCEPTED);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /**
     * Scenario C, stop first (deterministic): an acceptance that has to wait behind a stop holding the batch
     * exclusively re-reads the batch once the stop commits, finds it stopped and the item cancelled, and is refused
     * as closed. Nothing is created and the photo is not attached.
     */
    @Test
    void anAcceptanceWaitingBehindAStopIsRefusedAndCreatesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-stop-first@example.com");
        BatchOverview batch = startWithLinkedPhotos(tokens, 1);
        UUID batchId = batch.batch().id();
        String itemId = batch.items().getFirst().item().id().toString();
        UUID photoId = batch.items().getFirst().item().photoUploadId().orElseThrow();
        CountDownLatch stopping = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Void> stop = executor.submit(() -> wardrobeTransactions.inTransaction(() -> {
                BulkImportBatch locked = bulkImportRepository.lockForStop(tokens.accountId(), batchId).orElseThrow();
                for (BulkImportItem item : bulkImportRepository.lockUnfinishedItems(batchId)) {
                    bulkImportRepository.update(item.canceled(clock.instant()), item.revision());
                }
                bulkImportRepository.markStopped(batchId, locked.stop(clock.instant()).stoppedAt().orElseThrow());
                stopping.countDown();
                awaitQuietly(release);
                return null;
            }));
            assertThat(stopping.await(10, SECONDS)).isTrue();
            Future<String> acceptance = executor.submit(() -> {
                try {
                    acceptBulkImportItemService.accept(new AcceptBulkImportItemCommand(tokens.accountId(),
                            batchId.toString(), itemId, "2", true, REVIEWED));
                    return "accepted";
                } catch (BulkImportClosedException closed) {
                    return "closed";
                }
            });

            awaitABackendWaitingForALock();
            assertThat(acceptance.isDone()).as("the acceptance waits for the stop").isFalse();
            release.countDown();
            stop.get(10, SECONDS);

            assertThat(acceptance.get(10, SECONDS)).isEqualTo("closed");
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
        assertThat(photoUploadStatus(photoId)).as("the photo was not attached").isEqualTo("READY");
    }

    /** Scenario D: two items racing for one staged photo; the photo lock lets exactly one link it. */
    @Test
    void twoItemsRacingForOneStagedPhotoLetExactlyOneLinkIt() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-one-photo@example.com");
        for (int round = 0; round < 5; round++) {
            BatchOverview batch = bulkImportService.create(new CreateBulkImportCommand(tokens.accountId(),
                    UUID.randomUUID().toString(), 2, true)).batch();
            String batchId = batch.batch().id().toString();
            UUID photoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(900 + round, 700));
            CyclicBarrier start = new CyclicBarrier(2);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                List<Future<String>> outcomes = new ArrayList<>();
                for (ItemOverview item : batch.items()) {
                    outcomes.add(executor.submit(() -> {
                        start.await(10, SECONDS);
                        try {
                            bulkImportService.linkPhoto(new LinkBulkImportPhotoCommand(tokens.accountId(), batchId,
                                    item.item().id().toString(), "1", photoId.toString()));
                            return "linked";
                        } catch (PhotoUploadNotUsableException notUsable) {
                            return "not usable";
                        }
                    }));
                }
                assertThat(List.of(outcomes.get(0).get(20, SECONDS), outcomes.get(1).get(20, SECONDS)))
                        .containsExactlyInAnyOrder("linked", "not usable");
            } finally {
                executor.shutdownNow();
            }
            assertThat(jdbc.sql("SELECT count(*) FROM wardrobe.wardrobe_bulk_import_item WHERE photo_upload_id = :photo")
                    .param("photo", photoId).query(Long.class).single()).isOne();
            // Each round's batch is stopped, so the rounds stay within the active-batch limit.
            bulkImportService.stop(tokens.accountId(), batchId, true);
        }
    }

    /** Scenario E: identical starts with the same Idempotency-Key race; exactly one batch exists. */
    @Test
    void concurrentIdenticalStartsCreateOneBatch() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-same-key@example.com");
        String key = UUID.randomUUID().toString();
        int attempts = 4;
        CyclicBarrier start = new CyclicBarrier(attempts);
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        List<BulkImportCreation> creations = new ArrayList<>();
        try {
            List<Future<BulkImportCreation>> outcomes = new ArrayList<>();
            for (int attempt = 0; attempt < attempts; attempt++) {
                outcomes.add(executor.submit(() -> {
                    start.await(10, SECONDS);
                    return bulkImportService.create(new CreateBulkImportCommand(tokens.accountId(), key, 2, true));
                }));
            }
            for (Future<BulkImportCreation> outcome : outcomes) {
                creations.add(outcome.get(20, SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
        assertThat(creations).extracting(creation -> creation.batch().batch().id()).containsOnly(
                creations.getFirst().batch().batch().id());
        assertThat(creations).filteredOn(BulkImportCreation::newlyCreated).hasSize(1);
        assertThat(countRows("wardrobe.wardrobe_bulk_import_batch")).isOne();
        assertThat(countRows("wardrobe.wardrobe_bulk_import_item")).isEqualTo(2);
    }

    /**
     * Scenario F: the expiry job never waits for or overrides an acceptance. While an acceptance holds the linked
     * photo, the job skips it (SKIP LOCKED) without blocking; after the acceptance commits, the attached photo is
     * outside the job's reach. A photo nobody holds is expired, and its item then reads EXPIRED.
     */
    @Test
    void theExpiryJobSkipsAPhotoAnAcceptanceHoldsAndNeverTouchesAnAttachedOne() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-bulk-expiry@example.com");
        BatchOverview batch = startWithLinkedPhotos(tokens, 2);
        UUID batchId = batch.batch().id();
        UUID acceptedItem = batch.items().get(0).item().id();
        UUID acceptedPhoto = batch.items().get(0).item().photoUploadId().orElseThrow();
        UUID idlePhoto = batch.items().get(1).item().photoUploadId().orElseThrow();
        Instant afterTheStagedPhotoLifetime = clock.instant().plus(Duration.ofHours(25));
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Void> acceptanceInFlight = executor.submit(() -> wardrobeTransactions.inTransaction(() -> {
                bulkImportRepository.lockForItemWork(tokens.accountId(), batchId).orElseThrow();
                bulkImportRepository.lockItem(batchId, acceptedItem).orElseThrow();
                photoUploadRepository.lockOwned(tokens.accountId(), acceptedPhoto).orElseThrow();
                held.countDown();
                awaitQuietly(release);
                return null;
            }));
            assertThat(held.await(10, SECONDS)).isTrue();

            Future<Integer> expiry = executor.submit(() -> photoUploadRepository.expireDue(afterTheStagedPhotoLifetime,
                    photoUploadPolicy.inFlightFence(), 10));
            assertThat(expiry.get(10, SECONDS)).as("only the photo nobody holds is expired, without waiting").isOne();
            assertThat(photoUploadStatus(acceptedPhoto)).isEqualTo("READY");
            assertThat(photoUploadStatus(idlePhoto)).isEqualTo("DISCARDED");
            release.countDown();
            acceptanceInFlight.get(10, SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }

        BatchOverview accepted = acceptBulkImportItemService.accept(new AcceptBulkImportItemCommand(tokens.accountId(),
                batchId.toString(), acceptedItem.toString(), "2", true, REVIEWED));
        assertThat(photoUploadRepository.expireDue(afterTheStagedPhotoLifetime, photoUploadPolicy.inFlightFence(), 10))
                .isZero();
        assertThat(photoUploadStatus(acceptedPhoto)).isEqualTo("ATTACHED");
        assertThat(accepted.items()).extracting(ItemOverview::photoState)
                .containsExactly(LinkedPhotoState.NONE, LinkedPhotoState.EXPIRED);
        assertThat(accepted.items().get(0).garmentCurrent()).contains(true);
    }

    /** A new batch whose items each have a READY staged photo linked (revision 2). */
    private BatchOverview startWithLinkedPhotos(IssuedTokens tokens, int itemCount) throws Exception {
        BatchOverview overview = bulkImportService.create(new CreateBulkImportCommand(tokens.accountId(),
                UUID.randomUUID().toString(), itemCount, true)).batch();
        String batchId = overview.batch().id().toString();
        for (int position = 0; position < itemCount; position++) {
            UUID photoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1000 + position, 750));
            overview = bulkImportService.linkPhoto(new LinkBulkImportPhotoCommand(tokens.accountId(), batchId,
                    overview.items().get(position).item().id().toString(), "1", photoId.toString()));
        }
        return overview;
    }

    /** Polls PostgreSQL until another session of this database is blocked on a lock (at most 10 seconds). */
    private void awaitABackendWaitingForALock() throws InterruptedException {
        long giveUp = System.nanoTime() + SECONDS.toNanos(10);
        while (System.nanoTime() < giveUp) {
            long waiting = jdbc.sql("""
                            SELECT count(*) FROM pg_stat_activity
                            WHERE datname = current_database() AND wait_event_type = 'Lock'
                            """).query(Long.class).single();
            if (waiting > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("No session started waiting for a row lock within 10 seconds");
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(20, SECONDS)) {
                throw new IllegalStateException("The test never released the held transaction");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while holding the transaction", interrupted);
        }
    }

    /** A new one-item batch whose item has a READY staged photo linked (revision 2). */
    private BatchOverview startWithLinkedPhoto(IssuedTokens tokens, int round) throws Exception {
        BatchOverview started = bulkImportService.create(new CreateBulkImportCommand(tokens.accountId(),
                UUID.randomUUID().toString(), 1, true)).batch();
        UUID photoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1024 + round, 768));
        return bulkImportService.linkPhoto(new LinkBulkImportPhotoCommand(tokens.accountId(),
                started.batch().id().toString(), started.items().getFirst().item().id().toString(), "1",
                photoId.toString()));
    }

    private Callable<String> acceptance(CyclicBarrier start, UUID owner, String batchId, String itemId) {
        return () -> {
            start.await(10, SECONDS);
            try {
                acceptBulkImportItemService.accept(new AcceptBulkImportItemCommand(owner, batchId, itemId, "2", true,
                        REVIEWED));
                return "accepted";
            } catch (BulkImportClosedException closed) {
                return "closed";
            }
        };
    }

}
