package com.capsuleai.wardrobe.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchasePriceTest {

    @ParameterizedTest
    @CsvSource({
            "1250000, VND",
            "0, VND",
            "12.50, USD",
            "12.5, USD",
            "999999999999, JPY",
            "0.00, USD",
            "999999999999.99, USD",
            "1.234, BHD"})
    void anExactNonNegativeAmountWithinTheCurrencyMinorUnitIsAccepted(String amount, String currency) {
        PurchasePrice price = PurchasePrice.of(amount, currency);

        assertThat(price.amountText()).isEqualTo(amount);
        assertThat(price.currencyCode()).isEqualTo(currency);
    }

    @Test
    void trailingZerosAreKeptExactlyAsEntered() {
        assertThat(PurchasePrice.of("12.50", "USD").amount()).isEqualByComparingTo(new BigDecimal("12.5"));
        assertThat(PurchasePrice.of("12.50", "USD").amountText()).isEqualTo("12.50");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "1e3", "1,000", " 12", "12.", ".5", "012", "1000000000000", "twelve", ""})
    void anythingButAPlainNonNegativeDecimalIsAnInvalidAmount(String amount) {
        assertThatThrownBy(() -> PurchasePrice.of(amount, "USD"))
                .isInstanceOfSatisfying(InvalidGarmentValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(GarmentValueProblem.INVALID_AMOUNT));
    }

    @ParameterizedTest
    @CsvSource({"150000.5, VND", "1.5, JPY", "12.505, USD"})
    void moreDecimalsThanTheCurrencyAllowsAreRejectedNotRounded(String amount, String currency) {
        assertThatThrownBy(() -> PurchasePrice.of(amount, currency))
                .isInstanceOfSatisfying(InvalidGarmentValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(GarmentValueProblem.TOO_MANY_DECIMALS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"vnd", "Usd", "XYZ", "XAU", "US", "USDT", ""})
    void lowerCaseUnknownAndNonMonetaryCodesAreUnsupportedCurrencies(String currency) {
        assertThatThrownBy(() -> PurchasePrice.of("10", currency))
                .isInstanceOfSatisfying(InvalidGarmentValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(GarmentValueProblem.UNSUPPORTED_CURRENCY));
    }
}
