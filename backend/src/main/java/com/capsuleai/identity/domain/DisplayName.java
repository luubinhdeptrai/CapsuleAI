package com.capsuleai.identity.domain;

import java.util.Optional;

/**
 * Optional account display name (FR-AUTH-001, DATA-AUTH-002).
 *
 * <p>A supplied name is kept exactly as entered, including Vietnamese
 * diacritics and surrounding spaces. A null or blank value means "omitted".
 */
public record DisplayName(String value) {

    /** Proposed technical bound; see Shared Architecture A-01. */
    public static final int MAX_LENGTH = 100;

    public DisplayName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A display name value is required");
        }
    }

    /** Returns empty when the name was omitted, otherwise the exact value. */
    public static Optional<DisplayName> optional(String rawDisplayName) {
        if (rawDisplayName == null || rawDisplayName.isBlank()) {
            return Optional.empty();
        }
        if (rawDisplayName.codePointCount(0, rawDisplayName.length()) > MAX_LENGTH) {
            throw new InvalidValueException(ValueProblem.TOO_LONG);
        }
        return Optional.of(new DisplayName(rawDisplayName));
    }
}