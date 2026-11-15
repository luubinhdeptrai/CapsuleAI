package com.capsuleai.wardrobe.application.ports;

/**
 * Private photo storage could not complete an operation (unreachable, timed out, refused). The
 * outcome of the operation is not confirmed; callers keep their durable state recoverable.
 */
public final class PhotoStorageUnavailableException extends RuntimeException {

    public PhotoStorageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}