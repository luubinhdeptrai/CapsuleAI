package com.capsuleai.identity.application;

import java.util.List;

/**
 * The request was rejected before any state changed.
 * Carries every field problem so the client can show all corrections at once.
 */
public final class RequestValidationException extends RuntimeException {

    private final List<FieldViolation> violations;

    public RequestValidationException(List<FieldViolation> violations) {
        super("Request rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<FieldViolation> violations() {
        return violations;
    }
}