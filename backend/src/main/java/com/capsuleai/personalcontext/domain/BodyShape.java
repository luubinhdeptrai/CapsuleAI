package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * US-PROF-002 — optional, self-declared body shape as a neutral geometric silhouette (BR-012,
 * BRULE-PROF-001). The values are a delegated default selected on 2026-11-16 (OPEN-S4-16), not Product Owner
 * approved. No measurement is collected and no shape is ever inferred from photos, garments or devices. A
 * value is soft context only: it never restricts garment categories or validity.
 */
public enum BodyShape {
    RECTANGLE,
    TRIANGLE,
    INVERTED_TRIANGLE,
    HOURGLASS,
    ROUND;

    /** The value whose code equals {@code code} exactly; no case folding, trimming or fallback. */
    public static Optional<BodyShape> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.name().equals(code)).findFirst();
    }
}