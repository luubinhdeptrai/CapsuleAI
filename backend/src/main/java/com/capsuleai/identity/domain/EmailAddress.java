package com.capsuleai.identity.domain;

import java.util.Locale;

/**
 * The normalized email identity of an account (BRULE-AUTH-002, DATA-AUTH-002).
 *
 * <p>Normalization strips surrounding whitespace and lower-cases the value.
 * Provider-specific alias rules (removing dots or "+tags") are deliberately
 * NOT applied, so {@code person+news@example.com} stays a different identity.
 *
 * <p>Use {@link #normalize(String)} for user input. The canonical constructor
 * is for values that are already normalized, such as database rows.
 */
public record EmailAddress(String value) {

    /** RFC 5321 limits a forward path to 254 characters. */
    public static final int MAX_LENGTH = 254;

    public EmailAddress {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("A normalized email value is required");
        }
    }

    public static EmailAddress normalize(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new InvalidValueException(ValueProblem.REQUIRED);
        }
        String normalized = rawEmail.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_LENGTH) {
            throw new InvalidValueException(ValueProblem.TOO_LONG);
        }
        if (!hasMinimalEmailShape(normalized)) {
            throw new InvalidValueException(ValueProblem.INVALID_FORMAT);
        }
        return new EmailAddress(normalized);
    }

    private static boolean hasMinimalEmailShape(String candidate) {
        int at = candidate.indexOf('@');
        boolean exactlyOneAt = at > 0 && at == candidate.lastIndexOf('@');
        boolean hasDomain = at < candidate.length() - 1;
        boolean noInnerWhitespace = candidate.codePoints().noneMatch(Character::isWhitespace);
        return exactlyOneAt && hasDomain && noInnerWhitespace;
    }
}