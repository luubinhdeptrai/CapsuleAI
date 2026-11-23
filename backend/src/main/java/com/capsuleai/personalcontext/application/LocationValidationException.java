package com.capsuleai.personalcontext.application;

import java.util.List;

/** The location change was rejected and nothing was stored; every problem is listed. No value is echoed. */
public final class LocationValidationException extends RuntimeException {

    private final List<PreferenceFieldViolation> violations;

    public LocationValidationException(List<PreferenceFieldViolation> violations) {
        super("Location change rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<PreferenceFieldViolation> violations() {
        return violations;
    }
}
