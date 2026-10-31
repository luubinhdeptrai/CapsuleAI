package com.capsuleai.identity.application;

/**
 * An account already exists for the normalized email (FR-AUTH-016).
 * Nothing was created and the existing account was not changed.
 */
public final class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("An account already exists for this normalized email");
    }
}