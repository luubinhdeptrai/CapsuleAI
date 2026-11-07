package com.capsuleai.wardrobe.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Optional user-reported purchase Price: the amount originally paid, as an exact
 * decimal with an explicit ISO 4217 currency (FR-GAR-013, BRULE-GAR-010). Never
 * floating point, never converted, never inferred from location or appearance.
 *
 * <p>Validation approved by the Product Owner on 2026-11-07 (OPEN-S2-01A, TD-S2-11):
 * the amount is a plain non-negative decimal string with at most 12 integer
 * digits and no more decimals than the currency's ISO minor unit (no rounding);
 * the currency is an upper-case ISO 4217 code that names a real currency (codes
 * without a minor unit, such as XAU, are rejected). The exact amount text,
 * including trailing zeros, is preserved.
 */
public record PurchasePrice(BigDecimal amount, Currency currency) {

    private static final Pattern PLAIN_DECIMAL = Pattern.compile("(0|[1-9][0-9]{0,11})(\\.[0-9]+)?");
    private static final Pattern CURRENCY_CODE = Pattern.compile("[A-Z]{3}");

    public PurchasePrice {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("A purchase price cannot be negative");
        }
    }

    /**
     * Parses a supplied amount and currency, both present.
     *
     * @throws InvalidGarmentValueException with INVALID_AMOUNT, TOO_MANY_DECIMALS or UNSUPPORTED_CURRENCY
     */
    public static PurchasePrice of(String amountText, String currencyCode) {
        Currency currency = supportedCurrency(currencyCode);
        if (amountText == null || !PLAIN_DECIMAL.matcher(amountText).matches()) {
            throw new InvalidGarmentValueException(GarmentValueProblem.INVALID_AMOUNT);
        }
        BigDecimal amount = new BigDecimal(amountText);
        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new InvalidGarmentValueException(GarmentValueProblem.TOO_MANY_DECIMALS);
        }
        return new PurchasePrice(amount, currency);
    }

    /** The exact amount as entered, for example {@code "1250000"} or {@code "12.50"}. */
    public String amountText() {
        return amount.toPlainString();
    }

    public String currencyCode() {
        return currency.getCurrencyCode();
    }

    private static Currency supportedCurrency(String currencyCode) {
        if (currencyCode == null || !CURRENCY_CODE.matcher(currencyCode).matches()) {
            throw new InvalidGarmentValueException(GarmentValueProblem.UNSUPPORTED_CURRENCY);
        }
        try {
            Currency currency = Currency.getInstance(currencyCode);
            if (currency.getDefaultFractionDigits() < 0) {
                throw new InvalidGarmentValueException(GarmentValueProblem.UNSUPPORTED_CURRENCY);
            }
            return currency;
        } catch (IllegalArgumentException unknownCurrency) {
            throw new InvalidGarmentValueException(GarmentValueProblem.UNSUPPORTED_CURRENCY);
        }
    }
}