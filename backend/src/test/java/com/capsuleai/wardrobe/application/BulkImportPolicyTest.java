package com.capsuleai.wardrobe.application;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-BULK-001 (S4-BULK-06, OPEN-S4-25 delegated defaults): admission limits and their derived byte bound. */
class BulkImportPolicyTest {

    private final BulkImportPolicy defaults = new BulkImportPolicy(20, 3, Duration.ofDays(7));

    @Test
    void theDefaultsBoundOneBatchAtThreeHundredMillionOriginalBytes() {
        assertThat(defaults.maximumOriginalBytesPerBatch(15_000_000L)).isEqualTo(300_000_000L);
    }

    @Test
    void itemCountsFromOneToTheLimitAreAdmitted() {
        assertThat(defaults.admitsItemCount(1)).isTrue();
        assertThat(defaults.admitsItemCount(20)).isTrue();
        assertThat(defaults.admitsItemCount(0)).isFalse();
        assertThat(defaults.admitsItemCount(21)).as("a 21st item").isFalse();
    }

    @Test
    void aFourthActiveBatchIsNotAdmitted() {
        assertThat(defaults.admitsAnotherBatch(2)).isTrue();
        assertThat(defaults.admitsAnotherBatch(3)).isFalse();
    }

    @Test
    void unusableConfigurationIsRejectedAtStartup() {
        assertThatThrownBy(() -> new BulkImportPolicy(0, 3, Duration.ofDays(7))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BulkImportPolicy(BulkImportPolicy.ITEM_CEILING + 1, 3, Duration.ofDays(7)))
                .as("above the database ceiling").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BulkImportPolicy(20, 0, Duration.ofDays(7))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BulkImportPolicy(20, 3, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }
}
