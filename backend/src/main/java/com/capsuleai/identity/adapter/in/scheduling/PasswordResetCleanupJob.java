package com.capsuleai.identity.adapter.in.scheduling;

import com.capsuleai.identity.application.PurgeEndedPasswordResetsService;
import com.capsuleai.identity.application.PurgeEndedPasswordResetsService.PurgeResult;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Runs the password-reset cleanup periodically inside the application (OPEN-S2-07); no
 * external scheduler or broker is involved. A failed run is logged and retried by the next
 * run, which deletes whatever is still due.
 */
public class PasswordResetCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetCleanupJob.class);

    private final PurgeEndedPasswordResetsService purge;

    public PasswordResetCleanupJob(PurgeEndedPasswordResetsService purge) {
        this.purge = Objects.requireNonNull(purge, "purge");
    }

    @Scheduled(initialDelayString = "${capsuleai.identity.password-reset.cleanup.initial-delay}",
               fixedDelayString = "${capsuleai.identity.password-reset.cleanup.interval}")
    public void run() {
        try {
            PurgeResult result = purge.purge();
            log.info("Password-reset cleanup deleted {} ended interactions and {} attempt counters",
                    result.deletedInteractions(), result.deletedAttemptCounters());
        } catch (RuntimeException failure) {
            log.warn("Password-reset cleanup failed and will run again at its next interval ({})",
                    failure.getClass().getSimpleName());
        }
    }
}