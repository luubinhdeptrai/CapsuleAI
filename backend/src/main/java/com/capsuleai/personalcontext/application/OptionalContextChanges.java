package com.capsuleai.personalcontext.application;

import java.util.Objects;

/**
 * US-PROF-002 (S4-PROF-03) — the requested change of each optional field: keep (omitted), clear (null),
 * replace (a submitted code, validated by the service) or malformed (a value of the wrong JSON kind).
 */
public record OptionalContextChanges(ValueChange<String> genderContext, ValueChange<String> bodyShape) {

    public OptionalContextChanges {
        Objects.requireNonNull(genderContext, "genderContext");
        Objects.requireNonNull(bodyShape, "bodyShape");
    }
}