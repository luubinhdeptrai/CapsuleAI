package com.capsuleai.wardrobe.application.ports;

/**
 * The photo could not be processed right now: no decoding capacity became free in time, or the
 * HEIF decoder is not installed. This is a temporary, truthful limitation, never "unsupported
 * format" (S3-IMG-06: a failed decoder is unresolved work, not permission to drop a format).
 */
public final class PhotoProcessingUnavailableException extends RuntimeException {

    public PhotoProcessingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}