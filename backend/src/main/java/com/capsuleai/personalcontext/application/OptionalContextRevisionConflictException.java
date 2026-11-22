package com.capsuleai.personalcontext.application;

/**
 * The optional context changed (or was first created) after the user read it (S4-PROF-03). Nothing changed;
 * the client rereads and lets the user review. It never resubmits automatically.
 */
public final class OptionalContextRevisionConflictException extends RuntimeException {

    public OptionalContextRevisionConflictException() {
        super("The optional context changed after it was read; the change was not applied");
    }
}