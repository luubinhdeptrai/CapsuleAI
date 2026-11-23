package com.capsuleai.personalcontext.application;

/** The location changed (or was first selected) after the user read it (AC-06). Nothing changed. */
public final class LocationRevisionConflictException extends RuntimeException {

    public LocationRevisionConflictException() {
        super("The location changed after it was read; the change was not applied");
    }
}
