package com.capsuleai.personalcontext.domain;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-CTX-001 AC-03 (OPEN-S4-21): the inclusive 30-minute boundary and the WMO code translation. */
class WeatherFreshnessTest {

    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");

    @Test
    void evidenceExactlyThirtyMinutesOldIsCurrent() {
        assertThat(WeatherFreshness.isCurrentAt(NOW.minus(Duration.ofMinutes(30)), NOW)).isTrue();
        assertThat(WeatherFreshness.isCurrentAt(NOW, NOW)).isTrue();
    }

    @Test
    void evidenceJustOlderThanThirtyMinutesIsNotCurrent() {
        assertThat(WeatherFreshness.isCurrentAt(NOW.minus(Duration.ofMinutes(30)).minusNanos(1_000), NOW)).isFalse();
    }

    @Test
    void slightlyFutureEvidenceIsToleratedButFarFutureEvidenceIsImplausible() {
        assertThat(WeatherFreshness.isCurrentAt(NOW.plus(Duration.ofMinutes(5)), NOW)).isTrue();
        assertThat(WeatherFreshness.isImplausiblyFuture(NOW.plus(Duration.ofMinutes(5)), NOW)).isFalse();
        assertThat(WeatherFreshness.isCurrentAt(NOW.plus(Duration.ofMinutes(6)), NOW)).isFalse();
        assertThat(WeatherFreshness.isImplausiblyFuture(NOW.plus(Duration.ofMinutes(6)), NOW)).isTrue();
    }

    @Test
    void wmoCodesMapToConditionsAndUnknownCodesDoNot() {
        assertThat(WeatherCondition.fromWmoCode(0)).contains(WeatherCondition.CLEAR);
        assertThat(WeatherCondition.fromWmoCode(2)).contains(WeatherCondition.PARTLY_CLOUDY);
        assertThat(WeatherCondition.fromWmoCode(63)).contains(WeatherCondition.RAIN);
        assertThat(WeatherCondition.fromWmoCode(99)).contains(WeatherCondition.THUNDERSTORM_WITH_HAIL);
        assertThat(WeatherCondition.fromWmoCode(42)).isEmpty();
    }
}
