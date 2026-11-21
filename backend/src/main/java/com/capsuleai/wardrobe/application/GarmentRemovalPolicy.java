package com.capsuleai.wardrobe.application;

import java.time.Duration;
import java.util.Objects;

/**
 * Removal retention and cleanup settings, supplied by configuration (US-GAR-005, OPEN-S4-04/33).
 *
 * @param purgeLimit         time from the original removal by which the photo and nonessential data must
 *                           be physically deleted; the approved obligation is at most 30 days (AC-05)
 * @param overdueWarningAge  age after which an incomplete obligation is reported as overdue; must leave
 *                           time for intervention before {@code purgeLimit}
 * @param cleanupBatchSize   obligations claimed per worker run
 * @param cleanupRetryDelay  lease and retry delay after a claim
 */
public record GarmentRemovalPolicy(
        Duration purgeLimit,
        Duration overdueWarningAge,
        int cleanupBatchSize,
        Duration cleanupRetryDelay) {

    /** The approved maximum (DATA-RET-002, BRULE-GAR-009): physical deletion within 30 days of removal. */
    public static final Duration MAXIMUM_PURGE_LIMIT = Duration.ofDays(30);

    public GarmentRemovalPolicy {
        Objects.requireNonNull(purgeLimit, "purgeLimit");
        Objects.requireNonNull(overdueWarningAge, "overdueWarningAge");
        Objects.requireNonNull(cleanupRetryDelay, "cleanupRetryDelay");
        if (purgeLimit.isNegative() || purgeLimit.isZero() || purgeLimit.compareTo(MAXIMUM_PURGE_LIMIT) > 0) {
            throw new IllegalArgumentException("The purge limit must be positive and at most 30 days");
        }
        if (overdueWarningAge.isNegative() || overdueWarningAge.isZero()
                || overdueWarningAge.compareTo(purgeLimit) >= 0) {
            throw new IllegalArgumentException("The overdue warning age must be positive and shorter than the purge limit");
        }
        if (cleanupBatchSize < 1) {
            throw new IllegalArgumentException("The cleanup batch size must be positive");
        }
        if (cleanupRetryDelay.isNegative() || cleanupRetryDelay.isZero()) {
            throw new IllegalArgumentException("The cleanup retry delay must be positive");
        }
    }
}