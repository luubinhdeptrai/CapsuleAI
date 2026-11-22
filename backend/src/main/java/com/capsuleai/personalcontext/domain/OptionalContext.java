package com.capsuleai.personalcontext.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * US-PROF-002 — what the user currently declares as optional context. An empty value is not provided (absent
 * context, not a default or an inferred attribute).
 */
public record OptionalContext(Optional<GenderContext> genderContext, Optional<BodyShape> bodyShape) {

    public OptionalContext {
        Objects.requireNonNull(genderContext, "genderContext must be Optional.empty() when not provided");
        Objects.requireNonNull(bodyShape, "bodyShape must be Optional.empty() when not provided");
    }

    /** Nothing declared. */
    public static OptionalContext none() {
        return new OptionalContext(Optional.empty(), Optional.empty());
    }

    public boolean declaresNothing() {
        return genderContext.isEmpty() && bodyShape.isEmpty();
    }
}