package com.capsuleai.wardrobe.application;

import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService.CleanupResult;
import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-005 (S4-GAR-03, OPEN-S4-04/33): prompt minimization, retried deletion, completion, overdue counting,
 * recovery after a crash between deletion and its record, and the startup limits of the policy.
 */
class GarmentRemovalCleanupServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");
    private static final GarmentRemovalPolicy POLICY =
            new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(21), 50, Duration.ofMinutes(15));

    private InMemoryGarmentRemovalRepository garmentRemovalRepository;
    private FaultInjectingPhotoStorage photoStorage;
    private MutableClock clock;
    private GarmentRemovalCleanupService garmentRemovalCleanupService;

    @BeforeEach
    void setUp() {
        garmentRemovalRepository = new InMemoryGarmentRemovalRepository(new InMemoryGarmentRepository());
        photoStorage = new FaultInjectingPhotoStorage();
        clock = new MutableClock(NOW);
        garmentRemovalCleanupService = new GarmentRemovalCleanupService(garmentRemovalRepository, photoStorage,
                new ImmediateWardrobeTransactions(), clock, POLICY);
    }

    @Test
    void aDueObligationIsMinimizedItsObjectDeletedAndCompleted() {
        UUID garmentId = UUID.randomUUID();
        String objectKey = recordWithPhoto(garmentId, NOW);

        CleanupResult result = garmentRemovalCleanupService.run();

        assertThat(result).isEqualTo(new CleanupResult(1, 1, 1, 0, 0));
        assertThat(photoStorage.contains(objectKey)).isFalse();
        assertThat(garmentRemovalRepository.obligation(garmentId).orElseThrow().completed).isTrue();
    }

    @Test
    void aStorageFailureMinimizesTheDataAndRetriesTheObjectAfterTheLease() {
        UUID garmentId = UUID.randomUUID();
        String objectKey = recordWithPhoto(garmentId, NOW);
        photoStorage.failDeletes(true);

        assertThat(garmentRemovalCleanupService.run()).isEqualTo(new CleanupResult(1, 0, 0, 1, 0));
        assertThat(photoStorage.contains(objectKey)).isTrue();

        photoStorage.failDeletes(false);
        assertThat(garmentRemovalCleanupService.run())
                .as("the leased obligation is not claimed again before the retry delay")
                .isEqualTo(new CleanupResult(0, 0, 0, 0, 0));

        clock.advance(Duration.ofMinutes(16));
        assertThat(garmentRemovalCleanupService.run()).isEqualTo(new CleanupResult(0, 1, 1, 0, 0));
        assertThat(photoStorage.contains(objectKey)).isFalse();
    }

    @Test
    void anObligationWithoutAPhotoCompletesAfterMinimization() {
        UUID garmentId = UUID.randomUUID();
        garmentRemovalRepository.recordCleanup(garmentId, OWNER, NOW, NOW.plus(Duration.ofDays(30)), Optional.empty());

        assertThat(garmentRemovalCleanupService.run()).isEqualTo(new CleanupResult(1, 0, 1, 0, 0));
        assertThat(photoStorage.deletionCount()).isZero();
    }

    @Test
    void incompleteObligationsOlderThanTheWarningAgeAreCountedAsOverdue() {
        recordWithPhoto(UUID.randomUUID(), NOW.minus(Duration.ofDays(22)));
        recordWithPhoto(UUID.randomUUID(), NOW);
        photoStorage.failDeletes(true);

        CleanupResult result = garmentRemovalCleanupService.run();

        assertThat(result.failed()).isEqualTo(2);
        assertThat(result.overdue()).as("only the obligation removed 22 days ago is overdue").isOne();
    }

    @Test
    void aFailureAfterTheObjectIsDeletedOnlyRepeatsTheIdempotentDeletionAfterTheLease() {
        UUID garmentId = UUID.randomUUID();
        String objectKey = recordWithPhoto(garmentId, NOW);
        garmentRemovalRepository.crashOnNextObjectDeletionRecord();

        assertThat(garmentRemovalCleanupService.run()).as("minimized, deleted, but the deletion record failed")
                .isEqualTo(new CleanupResult(1, 0, 0, 1, 0));
        assertThat(photoStorage.contains(objectKey)).as("the object was deleted before the failure").isFalse();
        InMemoryGarmentRemovalRepository.Obligation afterCrash = garmentRemovalRepository.obligation(garmentId)
                .orElseThrow();
        assertThat(afterCrash.dataMinimized).isTrue();
        assertThat(afterCrash.objectDeleted).as("the deletion was not recorded").isFalse();
        assertThat(afterCrash.completed).isFalse();

        assertThat(garmentRemovalCleanupService.run()).as("the claim stays leased until the retry delay")
                .isEqualTo(new CleanupResult(0, 0, 0, 0, 0));

        clock.advance(Duration.ofMinutes(16));
        assertThat(garmentRemovalCleanupService.run())
                .as("minimization is not repeated; the deletion is repeated, recorded and the obligation completed")
                .isEqualTo(new CleanupResult(0, 1, 1, 0, 0));
        assertThat(photoStorage.deletionCount()).as("the idempotent delete ran once more").isEqualTo(2);
        InMemoryGarmentRemovalRepository.Obligation recovered = garmentRemovalRepository.obligation(garmentId)
                .orElseThrow();
        assertThat(recovered.completed).isTrue();
        assertThat(recovered.purgeDeadline).as("the deadline never moves").isEqualTo(NOW.plus(Duration.ofDays(30)));
        assertThat(recovered.attempts).isEqualTo(2);
    }

    @Test
    void oneObligationThatKeepsFailingNeverHoldsBackTheOthers() {
        UUID failingGarmentId = UUID.randomUUID();
        UUID healthyGarmentId = UUID.randomUUID();
        garmentRemovalRepository.recordCleanup(failingGarmentId, OWNER, NOW.minusSeconds(1),
                NOW.plus(Duration.ofDays(30)), Optional.empty());
        String healthyObjectKey = recordWithPhoto(healthyGarmentId, NOW);
        garmentRemovalRepository.failMinimizationOf(failingGarmentId);

        assertThat(garmentRemovalCleanupService.run())
                .as("the failing obligation is claimed first, yet the healthy one is fully processed")
                .isEqualTo(new CleanupResult(1, 1, 1, 1, 0));
        assertThat(garmentRemovalRepository.obligation(healthyGarmentId).orElseThrow().completed).isTrue();
        assertThat(photoStorage.contains(healthyObjectKey)).isFalse();
        assertThat(garmentRemovalRepository.obligation(failingGarmentId).orElseThrow().completed).isFalse();

        clock.advance(Duration.ofMinutes(16));
        assertThat(garmentRemovalCleanupService.run()).as("the failing obligation is retried after its lease")
                .isEqualTo(new CleanupResult(0, 0, 0, 1, 0));
        assertThat(garmentRemovalRepository.obligation(failingGarmentId).orElseThrow().attempts).isEqualTo(2);
    }

    @Test
    void thePolicyAcceptsOnlyLimitsWithinTheApprovedThirtyDayObligation() {
        Duration fifteenMinutes = Duration.ofMinutes(15);
        assertThat(new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(29), 1, Duration.ofSeconds(1)))
                .as("the 30-day boundary itself is allowed").isNotNull();

        assertThatThrownBy(() -> new GarmentRemovalPolicy(Duration.ofDays(30).plusSeconds(1), Duration.ofDays(21), 50,
                fifteenMinutes)).as("longer than the approved 30 days").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GarmentRemovalPolicy(Duration.ZERO, Duration.ofDays(21), 50, fifteenMinutes))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(30), 50,
                fifteenMinutes)).as("the warning must come before the limit").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ZERO, 50, fifteenMinutes))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(21), 0, fifteenMinutes))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(21), 50,
                Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }

    private String recordWithPhoto(UUID garmentId, Instant removedAt) {
        String objectKey = "photos/" + UUID.randomUUID() + ".jpg";
        photoStorage.put(objectKey, new byte[] {1, 2, 3});
        garmentRemovalRepository.recordCleanup(garmentId, OWNER, removedAt, removedAt.plus(Duration.ofDays(30)),
                Optional.of(new PhotoCleanupTarget(UUID.randomUUID(), objectKey)));
        return objectKey;
    }
}
