package com.capsuleai.wardrobe.application;

/**
 * The owner's staged photo cannot be used for this action: it is still being received, was
 * cancelled or expired, or already belongs to a garment (US-GAR-003 AC-03/AC-06). Nothing changed.
 */
public final class PhotoUploadNotUsableException extends RuntimeException {

    public PhotoUploadNotUsableException() {
        super("The staged photo cannot be used for this action");
    }
}