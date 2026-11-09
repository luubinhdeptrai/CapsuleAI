package com.capsuleai.wardrobe.application;

/**
 * No garment with this id belongs to the requesting owner: it does not exist,
 * belongs to another account, or the id is malformed. Deliberately does not say
 * which, so a guessed id reveals nothing about other wardrobes (NFR-SEC-001).
 */
public final class GarmentNotFoundException extends RuntimeException {

    public GarmentNotFoundException() {
        super("No such garment in the requesting owner's wardrobe");
    }
}