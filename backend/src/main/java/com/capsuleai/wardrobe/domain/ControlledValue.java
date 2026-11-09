package com.capsuleai.wardrobe.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * A value of an approved garment vocabulary. The enum constant name is the
 * stable code used in the API and the database; {@link #label()} is the exact
 * approved label it stands for (BRULE-GAR-004/005).
 */
public interface ControlledValue {

    String name();

    String label();

    /**
     * Finds the value whose code equals {@code code} exactly. No case folding,
     * trimming or fallback: an unknown code is simply absent (no invented OTHER).
     */
    static <E extends Enum<E> & ControlledValue> Optional<E> fromCode(Class<E> vocabulary, String code) {
        return Arrays.stream(vocabulary.getEnumConstants())
                .filter(value -> value.name().equals(code))
                .findFirst();
    }
}