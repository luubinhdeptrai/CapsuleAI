package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Profile style preferences (SRS 4.10, PRD 11.3, FR-PROF-002). A separate vocabulary from garment
 * Style (BRULE-GAR-004): similar-looking garment codes such as STREET or MINIMALIST are not values here.
 * The constant names are stored and published.
 */
public enum ProfileStyle {
    MINIMAL,
    CASUAL,
    SMART_CASUAL,
    CLASSIC,
    STREETWEAR,
    SPORTY,
    FORMAL;

    /** The value whose code equals {@code code} exactly; no case folding, trimming or fallback. */
    public static Optional<ProfileStyle> fromCode(String code) {
        return Arrays.stream(values()).filter(style -> style.name().equals(code)).findFirst();
    }
}