package com.capsuleai.wardrobe.configuration;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * US-BULK-001 configuration {@code capsuleai.wardrobe.bulk-import.*} (S4-BULK-06; values OPEN-S4-25/33,
 * delegated defaults). {@link com.capsuleai.wardrobe.application.BulkImportPolicy} validates the values at
 * startup, so an unusable limit stops the application instead of running with it.
 */
@ConfigurationProperties("capsuleai.wardrobe.bulk-import")
public record WardrobeBulkImportProperties(int maximumItemsPerBatch, int maximumActiveBatches,
                                           Duration batchLifetime) {

    public WardrobeBulkImportProperties {
        Objects.requireNonNull(batchLifetime, "capsuleai.wardrobe.bulk-import.batch-lifetime");
    }
}
