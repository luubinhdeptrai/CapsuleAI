package com.capsuleai.wardrobe.application;

import java.time.Duration;
import java.util.Objects;

/**
 * Photo rules and lifecycle settings, supplied by configuration (US-GAR-003).
 *
 * @param maximumBytes        FR-AI-012 "15 MB": 15,000,000 bytes (OPEN-S3-09, approved)
 * @param minimumShortestSide FR-AI-012: 512 pixels
 * @param pendingTtl          how long an unconfirmed photo stays usable: 24 hours, approved by the Product
 *                            Owner (OPEN-S3-07, S3-IMG-08); configurable
 * @param inFlightFence       minimum time between staging and deleting an object of a photo discarded while
 *                            still receiving; longer than any store plus decode can take
 * @param cleanupBatchSize    rows handled per expiry or deletion pass
 * @param cleanupRetryDelay   lease and retry delay for an object deletion
 */
public record PhotoUploadPolicy(
        long maximumBytes,
        int minimumShortestSide,
        Duration pendingTtl,
        Duration inFlightFence,
        int cleanupBatchSize,
        Duration cleanupRetryDelay) {

    public PhotoUploadPolicy {
        Objects.requireNonNull(pendingTtl, "pendingTtl");
        Objects.requireNonNull(inFlightFence, "inFlightFence");
        Objects.requireNonNull(cleanupRetryDelay, "cleanupRetryDelay");
        if (maximumBytes < 1 || minimumShortestSide < 1 || cleanupBatchSize < 1) {
            throw new IllegalArgumentException("Photo limits and batch size must be positive");
        }
        if (pendingTtl.isNegative() || pendingTtl.isZero() || inFlightFence.isNegative()
                || cleanupRetryDelay.isNegative() || cleanupRetryDelay.isZero()) {
            throw new IllegalArgumentException("Photo lifecycle durations must be positive");
        }
    }
}