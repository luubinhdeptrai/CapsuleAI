package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService;
import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService.CleanupResult;
import com.capsuleai.wardrobe.application.RemoveGarmentCommand;
import com.capsuleai.wardrobe.application.RemoveGarmentService;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalCleanupTarget;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-GAR-005 removal cleanup on real PostgreSQL with fault-injecting private storage (S4-GAR-03,
 * OPEN-S4-02/04): minimization, object deletion, retry after the lease, leased work exclusion, concurrent
 * claims with FOR UPDATE SKIP LOCKED, and overdue counting. Unrelated garments are never touched.
 */
class GarmentRemovalCleanupIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private RemoveGarmentService removeGarmentService;

    @Autowired
    private GarmentRemovalCleanupService garmentRemovalCleanupService;

    @Autowired
    private GarmentRemovalRepository garmentRemovalRepository;

    @Autowired
    private WardrobeTransactions wardrobeTransactions;

    @Test
    void cleanupMinimizesTheRemovedGarmentDeletesItsPhotoAndCompletes() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-complete@example.com");
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), richGarmentWithPhoto(photoUploadId));
        String objectKey = objectKeyOf(photoUploadId);
        String fingerprint = jdbc.sql("SELECT creation_fingerprint FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(String.class).single();
        remove(tokens.accountId(), garmentId);

        CleanupResult result = garmentRemovalCleanupService.run();

        assertThat(result).isEqualTo(new CleanupResult(1, 1, 1, 0, 0));
        Map<String, Object> garmentRow = jdbc.sql("""
                        SELECT category, color, subtype, brand, price_amount, price_currency, creation_fingerprint,
                               minimized_at IS NOT NULL AS minimized
                        FROM wardrobe.wardrobe_garment WHERE id = :id
                        """)
                .param("id", garmentId).query().singleRow();
        assertThat(garmentRow).containsEntry("category", "TOPS").containsEntry("color", "NAVY")
                .containsEntry("subtype", null).containsEntry("brand", null)
                .containsEntry("price_amount", null).containsEntry("price_currency", null)
                .containsEntry("creation_fingerprint", fingerprint).containsEntry("minimized", true);
        assertThat(childRows(garmentId)).isZero();
        assertThat(jdbc.sql("SELECT count(*) FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(Long.class).single()).isZero();
        assertThat(photoStorage.contains(objectKey)).isFalse();
        assertThat(jdbc.sql("SELECT completed_at IS NOT NULL FROM wardrobe.wardrobe_garment_removal_cleanup "
                        + "WHERE garment_id = :id")
                .param("id", garmentId).query(Boolean.class).single()).isTrue();
    }

    @Test
    void aFailedDeletionIsRetriedAfterTheLeaseAndUnrelatedGarmentsAreUntouched() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-retry@example.com");
        UUID removedPhotoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        UUID keptPhotoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(640, 640));
        UUID removedGarmentId = addGarmentAndReturnId(tokens.accessToken(), richGarmentWithPhoto(removedPhotoId));
        UUID keptGarmentId = addGarmentAndReturnId(tokens.accessToken(), richGarmentWithPhoto(keptPhotoId));
        String removedObjectKey = objectKeyOf(removedPhotoId);
        String keptObjectKey = objectKeyOf(keptPhotoId);
        remove(tokens.accountId(), removedGarmentId);
        photoStorage.failDeletes(true);

        assertThat(garmentRemovalCleanupService.run()).isEqualTo(new CleanupResult(1, 0, 0, 1, 0));
        assertThat(photoStorage.contains(removedObjectKey)).isTrue();

        photoStorage.failDeletes(false);
        assertThat(garmentRemovalCleanupService.run()).isEqualTo(new CleanupResult(0, 0, 0, 0, 0));
        clock.advance(Duration.ofMinutes(16));
        assertThat(garmentRemovalCleanupService.run()).isEqualTo(new CleanupResult(0, 1, 1, 0, 0));
        assertThat(photoStorage.contains(removedObjectKey)).isFalse();

        assertThat(photoStorage.contains(keptObjectKey)).isTrue();
        assertThat(photoUploadStatus(keptPhotoId)).isEqualTo("ATTACHED");
        assertThat(jdbc.sql("SELECT brand FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", keptGarmentId).query(String.class).single()).isEqualTo("Uniqlo");
        assertThat(childRows(keptGarmentId)).isEqualTo(2);
    }

    @Test
    void leasedWorkIsNotClaimedByAConcurrentRun() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-lease@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));
        remove(tokens.accountId(), garmentId);
        Instant now = clock.instant();

        assertThat(garmentRemovalRepository.claimDueCleanup(now, now.plus(Duration.ofMinutes(15)), 10))
                .singleElement().satisfies(target -> assertThat(target.garmentId()).isEqualTo(garmentId));
        assertThat(garmentRemovalRepository.claimDueCleanup(now, now.plus(Duration.ofMinutes(15)), 10)).isEmpty();
    }

    @Test
    void twoWorkersClaimingAtTheSameTimeNeverTakeTheSameObligation() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-concurrent-claims@example.com");
        Set<UUID> removedGarments = new HashSet<>();
        for (String color : List.of("BLACK", "WHITE", "RED")) {
            UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", color));
            remove(tokens.accountId(), garmentId);
            removedGarments.add(garmentId);
        }
        Instant now = clock.instant();
        Instant leaseUntil = now.plus(Duration.ofMinutes(15));
        CountDownLatch firstWorkerHoldsItsClaim = new CountDownLatch(1);
        CountDownLatch secondWorkerFinished = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // The first worker claims two obligations and keeps its transaction (and row locks) open.
            Future<List<UUID>> firstWorker = executor.submit(() -> wardrobeTransactions.inTransaction(() -> {
                List<UUID> claimed = garmentIds(garmentRemovalRepository.claimDueCleanup(now, leaseUntil, 2));
                firstWorkerHoldsItsClaim.countDown();
                awaitWithin(secondWorkerFinished);
                return claimed;
            }));
            assertThat(firstWorkerHoldsItsClaim.await(20, SECONDS)).isTrue();

            // Meanwhile the second worker skips the locked rows instead of waiting for them or taking them again.
            List<UUID> secondClaim = wardrobeTransactions.inTransaction(() ->
                    garmentIds(garmentRemovalRepository.claimDueCleanup(now, leaseUntil, 10)));
            secondWorkerFinished.countDown();
            List<UUID> firstClaim = firstWorker.get(20, SECONDS);

            assertThat(firstClaim).hasSize(2);
            assertThat(secondClaim).as("only the obligation the first worker did not lock").hasSize(1)
                    .doesNotContainAnyElementsOf(firstClaim);
            Set<UUID> allClaimed = new HashSet<>(firstClaim);
            allClaimed.addAll(secondClaim);
            assertThat(allClaimed).isEqualTo(removedGarments);
        } finally {
            secondWorkerFinished.countDown();
            executor.shutdownNow();
        }
        assertThat(jdbc.sql("SELECT sum(attempts) FROM wardrobe.wardrobe_garment_removal_cleanup")
                .query(Long.class).single()).as("each obligation was claimed exactly once").isEqualTo(3L);
    }

    @Test
    void anOverdueObligationIsCountedAndItsDeadlineNeverMoves() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-overdue@example.com");
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), richGarmentWithPhoto(photoUploadId));
        remove(tokens.accountId(), garmentId);
        String deadlineBefore = deadlineOf(garmentId);
        photoStorage.failDeletes(true);

        clock.advance(Duration.ofDays(22));
        CleanupResult result = garmentRemovalCleanupService.run();

        assertThat(result.overdue()).isOne();
        assertThat(deadlineOf(garmentId)).isEqualTo(deadlineBefore);
    }

    private void remove(UUID owner, UUID garmentId) {
        removeGarmentService.remove(new RemoveGarmentCommand(owner, garmentId.toString(), true, "1"));
    }

    private static List<UUID> garmentIds(List<GarmentRemovalCleanupTarget> claimed) {
        return claimed.stream().map(GarmentRemovalCleanupTarget::garmentId).toList();
    }

    private static void awaitWithin(CountDownLatch latch) {
        try {
            if (!latch.await(20, SECONDS)) {
                throw new IllegalStateException("The second worker did not finish while the first held its claim");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }

    private static Map<String, Object> richGarmentWithPhoto(UUID photoUploadId) {
        return garment("TOPS", "NAVY", "subtype", "T_SHIRTS", "brand", "Uniqlo",
                "materials", List.of("COTTON"), "styles", List.of("CASUAL"),
                "price", Map.of("amount", "10.50", "currency", "USD"),
                "photoUploadId", photoUploadId.toString());
    }

    private String objectKeyOf(UUID photoUploadId) {
        return jdbc.sql("SELECT object_key FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(String.class).single();
    }

    private long childRows(UUID garmentId) {
        return jdbc.sql("""
                        SELECT (SELECT count(*) FROM wardrobe.wardrobe_garment_material m WHERE m.garment_id = :id)
                             + (SELECT count(*) FROM wardrobe.wardrobe_garment_style s WHERE s.garment_id = :id)
                        """)
                .param("id", garmentId).query(Long.class).single();
    }

    private String deadlineOf(UUID garmentId) {
        return jdbc.sql("SELECT purge_deadline::text FROM wardrobe.wardrobe_garment_removal_cleanup "
                        + "WHERE garment_id = :id")
                .param("id", garmentId).query(String.class).single();
    }
}
