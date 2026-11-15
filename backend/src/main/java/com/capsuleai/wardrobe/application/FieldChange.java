package com.capsuleai.wardrobe.application;

import java.util.Objects;

/**
 * One property of a presence-aware partial edit (S3-GAR-01, OPEN-S3-05): keep the accepted
 * value, clear it, replace it, or a submitted value of the wrong kind that must be reported
 * for correction. The web adapter decides which; the application never sees JSON.
 *
 * @param <T> the submitted value type (a code, a list of codes or a Price pair)
 */
public sealed interface FieldChange<T> {

    /** The property was not part of the edit. */
    record Keep<T>() implements FieldChange<T> {
    }

    /** The user explicitly cleared the property. */
    record Clear<T>() implements FieldChange<T> {
    }

    /** The user supplied a new value; it is validated with the whole resulting profile. */
    record Replace<T>(T value) implements FieldChange<T> {

        public Replace {
            Objects.requireNonNull(value, "value");
        }
    }

    /** A value of a kind the property cannot hold, for example a number where a code belongs. */
    record Malformed<T>() implements FieldChange<T> {
    }

    static <T> FieldChange<T> keep() {
        return new Keep<>();
    }

    static <T> FieldChange<T> clear() {
        return new Clear<>();
    }

    static <T> FieldChange<T> replace(T value) {
        return new Replace<>(value);
    }

    static <T> FieldChange<T> malformed() {
        return new Malformed<>();
    }

    default boolean isMalformed() {
        return this instanceof Malformed<T>;
    }

    default boolean isClear() {
        return this instanceof Clear<T>;
    }

    /**
     * The value after this change: {@code current} if kept, null if cleared, the new value if
     * replaced. A malformed change has no resulting value; report it before applying.
     */
    default T applyTo(T current) {
        return switch (this) {
            case Keep<T> keep -> current;
            case Clear<T> clear -> null;
            case Replace<T> replace -> replace.value();
            case Malformed<T> malformed -> throw new IllegalStateException("A malformed change cannot be applied");
        };
    }
}