package com.capsuleai.wardrobe.adapter.in.scheduling;

import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService;
import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService.CleanupResult;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Runs the removal cleanup periodically inside the application (US-GAR-005, S4-GAR-03); no external
 * scheduler or broker. A failed run is logged and the next run continues from durable state. Only counts
 * are logged: never garment ids, owner ids or object keys. Overdue obligations are logged as a warning
 * so operations can intervene before the 30-day limit.
 */
public class GarmentRemovalCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(GarmentRemovalCleanupJob.class);

    private final GarmentRemovalCleanupService garmentRemovalCleanupService;

    public GarmentRemovalCleanupJob(GarmentRemovalCleanupService garmentRemovalCleanupService) {
        this.garmentRemovalCleanupService =
                Objects.requireNonNull(garmentRemovalCleanupService, "garmentRemovalCleanupService");
    }

    @Scheduled(initialDelayString = "${capsuleai.wardrobe.removal-cleanup.initial-delay}",
               fixedDelayString = "${capsuleai.wardrobe.removal-cleanup.interval}")
    public void run() {
        try {
            CleanupResult result = garmentRemovalCleanupService.run();
            if (result.minimized() + result.objectsDeleted() + result.completed() + result.failed() > 0) {
                log.info("Removal cleanup minimized {} garments, deleted {} photo objects, completed {} obligations, "
                                + "{} obligations to retry",
                        result.minimized(), result.objectsDeleted(), result.completed(), result.failed());
            }
            if (result.overdue() > 0) {
                log.warn("{} removal cleanup obligations are overdue and approach the 30-day deletion limit",
                        result.overdue());
            }
        } catch (RuntimeException failure) {
            log.warn("Removal cleanup failed and will run again at its next interval ({})",
                    failure.getClass().getSimpleName());
        }
    }
}
