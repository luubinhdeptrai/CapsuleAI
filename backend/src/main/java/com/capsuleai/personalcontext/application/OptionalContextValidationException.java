package com.capsuleai.personalcontext.application;

import java.util.List;

/** The optional-context change was rejected and nothing was stored; every problem is listed (UC-005 E1). */
public final class OptionalContextValidationException extends RuntimeException {

    private final List<PreferenceFieldViolation> violations;

    public OptionalContextValidationException(List<PreferenceFieldViolation> violations) {
        super("Optional-context change rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<PreferenceFieldViolation> violations() {
        return violations;
    }
}