package com.capsuleai.personalcontext.application;

import java.util.Objects;

/**
 * One presence-aware change (S3-PROF-03, OPEN-S3-05): keep, clear, replace, or a value of the wrong
 * kind to report. Personal Context's own type; it deliberately does not reuse Wardrobe's (AR-03).
 */
public sealed interface ValueChange<T> {

    record Keep<T>() implements ValueChange<T> {
    }

    record Clear<T>() implements ValueChange<T> {
    }

    record Replace<T>(T value) implements ValueChange<T> {

        public Replace {
            Objects.requireNonNull(value, "value");
        }
    }

    record Malformed<T>() implements ValueChange<T> {
    }

    static <T> ValueChange<T> keep() {
        return new Keep<>();
    }

    static <T> ValueChange<T> clear() {
        return new Clear<>();
    }

    static <T> ValueChange<T> replace(T value) {
        return new Replace<>(value);
    }

    static <T> ValueChange<T> malformed() {
        return new Malformed<>();
    }
}