package com.capsuleai.identity.adapter.out.timing;

import com.capsuleai.identity.application.ports.RecoveryResponsePacing;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinimumResponseTimePacingTest {

    @Test
    void aRequestFinishesNoSoonerThanTheMinimum() {
        RecoveryResponsePacing pacing = new MinimumResponseTimePacing(Duration.ofMillis(30));
        long started = System.nanoTime();

        pacing.start().finishNoSoonerThanMinimum();

        assertThat(System.nanoTime() - started).isGreaterThanOrEqualTo(Duration.ofMillis(30).toNanos());
    }

    @Test
    void aZeroMinimumDoesNotWait() {
        RecoveryResponsePacing pacing = new MinimumResponseTimePacing(Duration.ZERO);
        long started = System.nanoTime();

        pacing.start().finishNoSoonerThanMinimum();

        assertThat(System.nanoTime() - started).isLessThan(Duration.ofMillis(20).toNanos());
    }

    @Test
    void aNegativeMinimumIsRejected() {
        assertThatThrownBy(() -> new MinimumResponseTimePacing(Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
