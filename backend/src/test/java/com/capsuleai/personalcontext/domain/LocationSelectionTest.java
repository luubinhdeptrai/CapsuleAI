package com.capsuleai.personalcontext.domain;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-CTX-001 (OPEN-S4-18): rounding, geographic range and label rules of a selection. */
class LocationSelectionTest {

    @Test
    void coordinatesAreRoundedHalfEvenToTwoDecimals() {
        LocationSelection selection = LocationSelection.rounded(new BigDecimal("10.765"), new BigDecimal("106.6667"),
                Optional.empty(), LocationSource.DEVICE);

        assertThat(selection.latitude()).isEqualTo(new BigDecimal("10.76"));
        assertThat(selection.longitude()).isEqualTo(new BigDecimal("106.67"));
        assertThat(LocationSelection.round(new BigDecimal("10.775"))).isEqualTo(new BigDecimal("10.78"));
    }

    @Test
    void theGeographicRangeIsCheckedAfterRounding() {
        assertThat(LocationSelection.isLatitude(LocationSelection.round(new BigDecimal("90.004")))).isTrue();
        assertThat(LocationSelection.isLatitude(LocationSelection.round(new BigDecimal("-90.01")))).isFalse();
        assertThat(LocationSelection.isLongitude(LocationSelection.round(new BigDecimal("180.00")))).isTrue();
        assertThat(LocationSelection.isLongitude(LocationSelection.round(new BigDecimal("180.006")))).isFalse();
        assertThatThrownBy(() -> new LocationSelection(new BigDecimal("10.7"), new BigDecimal("106.70"),
                Optional.empty(), LocationSource.DEVICE)).as("unrounded").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aManualSelectionNeedsALabelAndADeviceSelectionMayOmitIt() {
        assertThatThrownBy(() -> LocationSelection.rounded(BigDecimal.ONE, BigDecimal.ONE, Optional.empty(),
                LocationSource.MANUAL)).isInstanceOf(IllegalArgumentException.class);
        assertThat(LocationSelection.rounded(BigDecimal.ONE, BigDecimal.ONE, Optional.empty(), LocationSource.DEVICE)
                .label()).isEmpty();
        assertThat(LocationSelection.rounded(BigDecimal.ONE, BigDecimal.ONE, Optional.of("Huế"), LocationSource.MANUAL)
                .label()).contains("Huế");
    }

    @Test
    void labelsAreNormalizedAndBounded() {
        assertThat(LocationSelection.normalizeLabel("  Huế  ")).contains("Huế");
        assertThat(LocationSelection.normalizeLabel("   ")).isEmpty();
        assertThat(LocationSelection.isAcceptableLabel("a".repeat(80))).isTrue();
        assertThat(LocationSelection.isAcceptableLabel("a".repeat(81))).isFalse();
        assertThat(LocationSelection.isAcceptableLabel("Line\nBreak")).isFalse();
    }
}
