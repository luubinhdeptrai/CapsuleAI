package com.capsuleai.wardrobe.domain;

import java.util.Optional;

/**
 * Optional user-entered Brand text for any category (FR-GAR-012, BRULE-GAR-010).
 * No enum, catalogue or recognition.
 *
 * <p>A supplied Brand is kept exactly as entered, including case, diacritics and
 * spaces, following the existing Display Name convention. Blank input means
 * "not supplied". The Product Owner approved these rules on 2026-11-07
 * (OPEN-S2-01B, TD-S2-12, PRD OPQ-013).
 */
public record Brand(String value) {

    /** Approved bound in Unicode code points (OPEN-S2-01B). */
    public static final int MAX_LENGTH = 100;

    public Brand {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("A brand value is required");
        }
    }

    /** Returns empty when no Brand was supplied, otherwise the exact value. */
    public static Optional<Brand> optional(String rawBrand) {
        if (rawBrand == null || rawBrand.isBlank()) {
            return Optional.empty();
        }
        if (!isWellFormedUnicode(rawBrand)) {
            throw new InvalidGarmentValueException(GarmentValueProblem.INVALID_CHARACTERS);
        }
        if (rawBrand.codePointCount(0, rawBrand.length()) > MAX_LENGTH) {
            throw new InvalidGarmentValueException(GarmentValueProblem.TOO_LONG);
        }
        return Optional.of(new Brand(rawBrand));
    }

    /** Rejects unpaired UTF-16 surrogates, which PostgreSQL text cannot store faithfully. */
    private static boolean isWellFormedUnicode(String candidate) {
        for (int index = 0; index < candidate.length(); index++) {
            char current = candidate.charAt(index);
            if (Character.isHighSurrogate(current)) {
                boolean followedByLowSurrogate = index + 1 < candidate.length()
                        && Character.isLowSurrogate(candidate.charAt(index + 1));
                if (!followedByLowSurrogate) {
                    return false;
                }
                index++;
            } else if (Character.isLowSurrogate(current)) {
                return false;
            }
        }
        return true;
    }
}