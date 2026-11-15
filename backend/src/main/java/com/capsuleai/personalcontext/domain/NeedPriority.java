package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/** Common-need priority and its weight (FR-PROF-003, BRULE-COV-002): exactly 0, 1, 2 and 3. */
public enum NeedPriority {
    NOT_RELEVANT(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    NeedPriority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    /** The priority with this weight, or empty for anything outside 0–3 (never clamped). */
    public static Optional<NeedPriority> fromWeight(int weight) {
        return Arrays.stream(values()).filter(priority -> priority.weight == weight).findFirst();
    }
}