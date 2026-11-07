package com.capsuleai.wardrobe.application;

import java.util.List;

/**
 * The garment entry was rejected before anything was stored (ERR-GAR-001, AC-05).
 * Carries every field problem so the client can show all corrections at once
 * and keep the draft.
 */
public final class GarmentValidationException extends RuntimeException {

    private final List<GarmentFieldViolation> violations;

    public GarmentValidationException(List<GarmentFieldViolation> violations) {
        super("Garment entry rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<GarmentFieldViolation> violations() {
        return violations;
    }
}