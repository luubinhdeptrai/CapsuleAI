package com.capsuleai.personalcontext.application;

/**
 * The declaration changed (or was first created) after the user read it (S3-PROF-04). Nothing changed;
 * the client rereads and lets the user review. It never resubmits automatically.
 */
public final class PreferencesRevisionConflictException extends RuntimeException {

    public PreferencesRevisionConflictException() {
        super("The preferences changed after they were read; the change was not applied");
    }
}