package com.capsuleai.wardrobe.application;

/**
 * The garment's accepted profile changed after the editor read it: the expected revision is
 * no longer current (US-GAR-002, S3-GAR-02). Nothing changed. The client rereads the garment
 * and lets the user review before resubmitting; it never resubmits automatically (S3-GAR-04).
 */
public final class GarmentRevisionConflictException extends RuntimeException {

    public GarmentRevisionConflictException() {
        super("The garment changed after it was read; the edit was not applied");
    }
}