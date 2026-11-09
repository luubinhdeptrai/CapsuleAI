package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.Garment;
import java.util.Objects;

/**
 * The garment an earlier accepted addition created, with the fingerprint of the
 * values it was submitted with, so a retry can be told apart from a new intention.
 */
public record RecordedGarmentCreation(Garment garment, String creationFingerprint) {

    public RecordedGarmentCreation {
        Objects.requireNonNull(garment, "garment");
        Objects.requireNonNull(creationFingerprint, "creationFingerprint");
    }
}