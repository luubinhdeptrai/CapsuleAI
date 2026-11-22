package com.capsuleai.wardrobe.application;

/**
 * The garment already has a photo, so another one is not attached (provisional US-GAR-006; replacement is
 * out of scope). Nothing changed.
 */
public final class GarmentAlreadyHasPhotoException extends RuntimeException {

    public GarmentAlreadyHasPhotoException() {
        super("The garment already has a photo; it was not replaced");
    }
}
