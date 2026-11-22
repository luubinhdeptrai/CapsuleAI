package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * US-PROF-002 — optional, self-declared gender context (BR-012, BRULE-PROF-001). The values are a delegated
 * default selected on 2026-11-16 (OPEN-S4-16), chosen to be small and inclusive without collecting free text;
 * they are not Product Owner approved and need an inclusive-representation review. "Prefer not to say" is
 * absence (no value). A value never restricts garments, requests or validity; it is never inferred.
 */
public enum GenderContext {
    WOMAN,
    MAN,
    NON_BINARY,
    /** A gender not listed above; no free text is collected. */
    ANOTHER_GENDER;

    /** The value whose code equals {@code code} exactly; no case folding, trimming or fallback. */
    public static Optional<GenderContext> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.name().equals(code)).findFirst();
    }
}