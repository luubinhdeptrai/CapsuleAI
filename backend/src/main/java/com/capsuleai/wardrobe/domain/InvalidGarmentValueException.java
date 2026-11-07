package com.capsuleai.wardrobe.domain;

/** Signals that a submitted garment value breaks a Wardrobe rule. The message never echoes the value. */
public final class InvalidGarmentValueException extends RuntimeException {

    private final GarmentValueProblem problem;

    public InvalidGarmentValueException(GarmentValueProblem problem) {
        super("Garment value rejected: " + problem);
        this.problem = problem;
    }

    public GarmentValueProblem problem() {
        return problem;
    }
}