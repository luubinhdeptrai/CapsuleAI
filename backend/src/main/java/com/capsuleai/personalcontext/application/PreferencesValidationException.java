package com.capsuleai.personalcontext.application;

import java.util.List;

/** The change was rejected and nothing was stored (UC-005 E1); every problem is listed. */
public final class PreferencesValidationException extends RuntimeException {

    private final List<PreferenceFieldViolation> violations;

    public PreferencesValidationException(List<PreferenceFieldViolation> violations) {
        super("Preference change rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<PreferenceFieldViolation> violations() {
        return violations;
    }
}