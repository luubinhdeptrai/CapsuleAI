package com.capsuleai.wardrobe.domain;

/**
 * Why a submitted garment value was rejected.
 *
 * <p>The constant names are part of the published API contract (field error
 * codes), so rename one only together with a contract change.
 */
public enum GarmentValueProblem {

    /** A required value (Category, Color, confirmation, Idempotency-Key) is missing. */
    REQUIRED,

    /** The user did not explicitly confirm the garment (confirmed was false). */
    NOT_CONFIRMED,

    /** The value is not a code of the approved vocabulary. */
    UNSUPPORTED_VALUE,

    /** The Subtype or Length belongs to another category's approved list. */
    NOT_IN_CATEGORY,

    /** A category-specific property was supplied for a category it does not apply to. */
    NOT_APPLICABLE,

    /** Only one of Price amount and currency was supplied. */
    INCOMPLETE_PRICE,

    /** The Price amount is not a non-negative decimal of at most 12 integer digits. */
    INVALID_AMOUNT,

    /** The Price amount has more decimals than the currency allows. */
    TOO_MANY_DECIMALS,

    /** The currency is not an upper-case ISO 4217 code of a real currency. */
    UNSUPPORTED_CURRENCY,

    /** The Brand is longer than 100 characters. */
    TOO_LONG,

    /** The Brand contains malformed Unicode. */
    INVALID_CHARACTERS,

    /** The Idempotency-Key is not a UUID. */
    INVALID_FORMAT
}