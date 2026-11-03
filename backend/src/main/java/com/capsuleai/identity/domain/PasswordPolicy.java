package com.capsuleai.identity.domain;

/**
 * Password rules for NEW passwords (BRULE-AUTH-003, FR-AUTH-015).
 *
 * <p>12 to 128 characters; letters, numbers, symbols, spaces and any Unicode
 * are allowed; no character-class mixture is required; the value is never
 * trimmed, normalized or otherwise transformed.
 *
 * <p>Length counts Unicode code points, so an emoji is one character.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {
    }

    /**
     * Inspects a new password exactly as entered.
     *
     * @throws InvalidValueException with REQUIRED, INVALID_CHARACTERS, TOO_SHORT or TOO_LONG
     */
    public static void validateNewPassword(String candidate) {
        if (candidate == null || candidate.isEmpty()) {
            throw new InvalidValueException(ValueProblem.REQUIRED);
        }
        if (!isWellFormedUnicode(candidate)) {
            throw new InvalidValueException(ValueProblem.INVALID_CHARACTERS);
        }
        int length = candidate.codePointCount(0, candidate.length());
        if (length < MIN_LENGTH) {
            throw new InvalidValueException(ValueProblem.TOO_SHORT);
        }
        if (length > MAX_LENGTH) {
            throw new InvalidValueException(ValueProblem.TOO_LONG);
        }
    }

    /**
     * Rejects unpaired UTF-16 surrogates. They are not Unicode characters and
     * would be replaced during hashing, so two different inputs could
     * otherwise produce the same credential.
     */
    private static boolean isWellFormedUnicode(String value) {
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isHighSurrogate(current)) {
                boolean followedByLowSurrogate = i + 1 < value.length()
                        && Character.isLowSurrogate(value.charAt(i + 1));
                if (!followedByLowSurrogate) {
                    return false;
                }
                i++;
            } else if (Character.isLowSurrogate(current)) {
                return false;
            }
        }
        return true;
    }
}