package com.capsuleai.wardrobe.application;

/**
 * The owner's current garment has no photo that can be shown: none was attached, or its stored
 * object is missing (US-GAR-003 AC-06). The garment's accepted information is unaffected.
 */
public final class GarmentPhotoUnavailableException extends RuntimeException {

    public GarmentPhotoUnavailableException() {
        super("No photo can be shown for this garment");
    }
}