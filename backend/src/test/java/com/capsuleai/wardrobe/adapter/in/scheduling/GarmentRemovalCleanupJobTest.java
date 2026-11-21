package com.capsuleai.wardrobe.adapter.in.scheduling;

import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService;
import com.capsuleai.wardrobe.application.GarmentRemovalPolicy;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalCleanupTarget;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.configuration.WardrobeRemovalCleanupConfiguration;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-GAR-005 (S4-GAR-03, OPEN-S4-04/33): the scheduled trigger of the removal cleanup. A failed run never
 * escapes the scheduler, overdue obligations are reported as a warning, every log line carries counts only
 * (no garment id, owner id or object key), and the job exists only while the cleanup is enabled.
 */
@ExtendWith(OutputCaptureExtension.class)
class GarmentRemovalCleanupJobTest {

    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");
    private static final GarmentRemovalPolicy POLICY =
            new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(21), 50, Duration.ofMinutes(15));
    private static final String OBJECT_KEY = "photos/" + UUID.randomUUID() + ".jpg";

    @Test
    void aFailedRunIsLoggedWithoutIdentifiersAndNeverEscapesTheScheduler(CapturedOutput output) {
        ScriptedGarmentRemovalRepository garmentRemovalRepository = new ScriptedGarmentRemovalRepository();
        garmentRemovalRepository.failClaims = true;
        GarmentRemovalCleanupJob garmentRemovalCleanupJob = jobUsing(garmentRemovalRepository);

        garmentRemovalCleanupJob.run();

        assertThat(output).contains("Removal cleanup failed and will run again at its next interval")
                .contains("IllegalStateException")
                .doesNotContain(ScriptedGarmentRemovalRepository.FAILURE_DETAIL);
    }

    @Test
    void completedWorkIsLoggedAsCountsAndOverdueObligationsAsAWarning(CapturedOutput output) {
        ScriptedGarmentRemovalRepository garmentRemovalRepository = new ScriptedGarmentRemovalRepository();
        UUID garmentId = UUID.randomUUID();
        garmentRemovalRepository.claimable = List.of(new GarmentRemovalCleanupTarget(garmentId,
                NOW.minus(Duration.ofDays(22)), NOW.plus(Duration.ofDays(8)), Optional.of(OBJECT_KEY), false, false));
        garmentRemovalRepository.overdueCount = 1;
        GarmentRemovalCleanupJob garmentRemovalCleanupJob = jobUsing(garmentRemovalRepository);

        garmentRemovalCleanupJob.run();

        assertThat(output)
                .contains("Removal cleanup minimized 1 garments, deleted 1 photo objects, completed 1 obligations, "
                        + "0 obligations to retry")
                .contains("1 removal cleanup obligations are overdue and approach the 30-day deletion limit")
                .doesNotContain(garmentId.toString())
                .doesNotContain(OBJECT_KEY);
    }

    @Test
    void theJobIsScheduledOnlyWhileTheCleanupIsEnabled() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withBean(GarmentRemovalCleanupService.class, () -> serviceUsing(new ScriptedGarmentRemovalRepository()))
                .withUserConfiguration(WardrobeRemovalCleanupConfiguration.class)
                .withPropertyValues("capsuleai.wardrobe.removal-cleanup.initial-delay=PT1H",
                        "capsuleai.wardrobe.removal-cleanup.interval=PT1H");

        contextRunner.withPropertyValues("capsuleai.wardrobe.removal-cleanup.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(GarmentRemovalCleanupJob.class));
        contextRunner.withPropertyValues("capsuleai.wardrobe.removal-cleanup.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GarmentRemovalCleanupJob.class));
        contextRunner.run(context -> assertThat(context).as("disabled unless explicitly enabled")
                .doesNotHaveBean(GarmentRemovalCleanupJob.class));
    }

    private static GarmentRemovalCleanupJob jobUsing(GarmentRemovalRepository garmentRemovalRepository) {
        return new GarmentRemovalCleanupJob(serviceUsing(garmentRemovalRepository));
    }

    private static GarmentRemovalCleanupService serviceUsing(GarmentRemovalRepository garmentRemovalRepository) {
        FaultInjectingPhotoStorage photoStorage = new FaultInjectingPhotoStorage();
        photoStorage.put(OBJECT_KEY, new byte[] {1, 2, 3});
        return new GarmentRemovalCleanupService(garmentRemovalRepository, photoStorage, new DirectTransactions(),
                Clock.fixed(NOW, ZoneOffset.UTC), POLICY);
    }

    /** Runs the work directly, as the cleanup service's own transaction boundary would. */
    private static final class DirectTransactions implements WardrobeTransactions {

        @Override
        public <T> T inTransaction(Supplier<T> work) {
            return work.get();
        }
    }

    /** A repository whose claim result, failure and overdue count are scripted by each test. */
    private static final class ScriptedGarmentRemovalRepository implements GarmentRemovalRepository {

        static final String FAILURE_DETAIL = "connection to wardrobe_garment_removal_cleanup lost";

        private List<GarmentRemovalCleanupTarget> claimable = List.of();
        private boolean failClaims;
        private int overdueCount;

        @Override
        public boolean removeIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision, Instant removedAt) {
            throw new UnsupportedOperationException("not used by the cleanup");
        }

        @Override
        public Optional<PhotoCleanupTarget> findAttachedPhoto(UUID garmentId) {
            throw new UnsupportedOperationException("not used by the cleanup");
        }

        @Override
        public void recordCleanup(UUID garmentId, UUID ownerAccountId, Instant removedAt, Instant purgeDeadline,
                                  Optional<PhotoCleanupTarget> attachedPhoto) {
            throw new UnsupportedOperationException("not used by the cleanup");
        }

        @Override
        public List<GarmentRemovalCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
            if (failClaims) {
                throw new IllegalStateException(FAILURE_DETAIL);
            }
            List<GarmentRemovalCleanupTarget> claimed = claimable;
            claimable = List.of();
            return claimed;
        }

        @Override
        public boolean minimizeRemovedGarment(UUID garmentId, Instant minimizedAt) {
            return true;
        }

        @Override
        public boolean markObjectDeleted(UUID garmentId, Instant deletedAt) {
            return true;
        }

        @Override
        public boolean completeIfFinished(UUID garmentId, Instant completedAt) {
            return true;
        }

        @Override
        public int countOverdue(Instant removedBefore) {
            return overdueCount;
        }
    }
}
