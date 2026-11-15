package com.capsuleai.wardrobe.application;

/** The upload Idempotency-Key was already used by this owner for a different photo. Nothing changed. */
public final class PhotoUploadKeyReusedException extends RuntimeException {

    public PhotoUploadKeyReusedException() {
        super("The upload Idempotency-Key was already used for a different photo");
    }
}