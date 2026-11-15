package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The six common-need dimensions of the Personalized Everyday Capsule (SRS 3.14.1/4.10, BRULE-COV-001),
 * each with the request-occasion codes it covers. Codes are defined for the API by OPEN-S3-15. The
 * mapping is data for future Coverage consumers; nothing in this story computes with it, and no request
 * occasion ever overwrites a need (FR-PROF-004).
 */
public enum CommonNeed {
    EVERYDAY_CASUAL("EVERYDAY", "DATE", "SOCIAL_EVENT"),
    WORK_PROFESSIONAL("WORK"),
    SCHOOL_UNIVERSITY("SCHOOL_UNIVERSITY"),
    FORMAL_SPECIAL_EVENT("FORMAL_EVENT"),
    TRAVEL("TRAVEL"),
    SPORT_ACTIVE("SPORT_ACTIVITY");

    private final List<String> relevantOccasionCodes;

    CommonNeed(String... relevantOccasionCodes) {
        this.relevantOccasionCodes = List.of(relevantOccasionCodes);
    }

    /** The SRS 3.14.1 request-occasion codes this need covers. */
    public List<String> relevantOccasionCodes() {
        return relevantOccasionCodes;
    }

    public static Optional<CommonNeed> fromCode(String code) {
        return Arrays.stream(values()).filter(need -> need.name().equals(code)).findFirst();
    }
}