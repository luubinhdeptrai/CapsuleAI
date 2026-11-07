package com.capsuleai.wardrobe.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTest {

    @Test
    void aSuppliedBrandIsKeptExactlyAsEntered() {
        assertThat(Brand.optional(" Thời Trang Việt ")).map(Brand::value).contains(" Thời Trang Việt ");
        assertThat(Brand.optional("uniqlo")).map(Brand::value).contains("uniqlo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankMeansNoBrandWasSupplied(String blank) {
        assertThat(Brand.optional(blank)).isEmpty();
        assertThat(Brand.optional(null)).isEmpty();
    }

    @Test
    void oneHundredCharactersAreAcceptedAndOneHundredAndOneAreNot() {
        assertThat(Brand.optional("b".repeat(100))).isPresent();
        // Code points, not UTF-16 units: 100 emoji are 100 characters.
        assertThat(Brand.optional("👜".repeat(100))).isPresent();

        assertThatThrownBy(() -> Brand.optional("b".repeat(101)))
                .isInstanceOfSatisfying(InvalidGarmentValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(GarmentValueProblem.TOO_LONG));
    }

    @Test
    void malformedUnicodeIsRejected() {
        assertThatThrownBy(() -> Brand.optional("Brand\uD83D"))
                .isInstanceOfSatisfying(InvalidGarmentValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(GarmentValueProblem.INVALID_CHARACTERS));
    }
}
