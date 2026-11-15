package com.capsuleai.wardrobe.application;

/**
 * No staged photo with this id belongs to the requesting owner: unknown, another account's or a
 * malformed id. Deliberately does not say which (NFR-SEC-001, S3-IMG-05).
 */
public final class PhotoUploadNotFoundException extends RuntimeException {

    public PhotoUploadNotFoundException() {
        super("No such staged photo for the requesting owner");
    }
}