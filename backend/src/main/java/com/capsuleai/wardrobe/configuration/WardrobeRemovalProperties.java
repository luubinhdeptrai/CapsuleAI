package com.capsuleai.wardrobe.configuration;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * US-GAR-005 configuration: {@code capsuleai.wardrobe.removal-cleanup.*}. Values in application.properties
 * implement OPEN-S4-04/33 (delegated defaults selected on 2026-11-16, not Product Owner approvals of the
 * values). {@link com.capsuleai.wardrobe.application.GarmentRemovalPolicy} validates them at startup.
 */
@ConfigurationProperties("capsuleai.wardrobe.removal-cleanup")
public record WardrobeRemovalProperties(
        boolean enabled,
        Duration initialDelay,
        Duration interval,
        int batchSize,
        Duration retryDelay,
        Duration purgeLimit,
        Duration overdueWarningAge) {

    public WardrobeRemovalProperties {
        Objects.requireNonNull(initialDelay, "capsuleai.wardrobe.removal-cleanup.initial-delay");
        Objects.requireNonNull(interval, "capsuleai.wardrobe.removal-cleanup.interval");
        Objects.requireNonNull(retryDelay, "capsuleai.wardrobe.removal-cleanup.retry-delay");
        Objects.requireNonNull(purgeLimit, "capsuleai.wardrobe.removal-cleanup.purge-limit");
        Objects.requireNonNull(overdueWarningAge, "capsuleai.wardrobe.removal-cleanup.overdue-warning-age");
    }
}