package com.capsuleai.wardrobe.adapter.in.scheduling;

import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService.CleanupResult;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Runs the pending-photo lifecycle periodically inside the application (S3-IMG-08); no external
 * scheduler or broker. A failed run is logged and the next run continues from durable state.
 */
public class PhotoUploadCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PhotoUploadCleanupJob.class);

    private final PhotoUploadCleanupService cleanup;

    public PhotoUploadCleanupJob(PhotoUploadCleanupService cleanup) {
        this.cleanup = Objects.requireNonNull(cleanup, "cleanup");
    }

    @Scheduled(initialDelayString = "${capsuleai.wardrobe.photo-upload.cleanup.initial-delay}",
               fixedDelayString = "${capsuleai.wardrobe.photo-upload.cleanup.interval}")
    public void run() {
        try {
            CleanupResult result = cleanup.run();
            if (result.expired() + result.deleted() + result.failed() > 0) {
                log.info("Photo cleanup expired {} staged photos, deleted {} objects, {} deletions to retry",
                        result.expired(), result.deleted(), result.failed());
            }
        } catch (RuntimeException failure) {
            log.warn("Photo cleanup failed and will run again at its next interval ({})",
                    failure.getClass().getSimpleName());
        }
    }
}