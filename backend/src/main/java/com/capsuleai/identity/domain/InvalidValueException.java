package com.capsuleai.identity.domain;

/**
 * Signals that a value breaks an Identity rule.
 *
 * <p>The message names only the problem, never the rejected value, because
 * the value may be a password.
 */
public final class InvalidValueException extends RuntimeException {

    private final ValueProblem problem;

    public InvalidValueException(ValueProblem problem) {
        super("Value rejected: " + problem);
        this.problem = problem;
    }

    public ValueProblem problem() {
        return problem;
    }
}