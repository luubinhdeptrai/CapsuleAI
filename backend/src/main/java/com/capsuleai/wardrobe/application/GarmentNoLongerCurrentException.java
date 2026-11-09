package com.capsuleai.wardrobe.application;

/**
 * The garment belongs to the requesting owner but is no longer in the current
 * wardrobe (US-WAR-002 AC-09, BRULE-HIST-002). Its historical meaning stays
 * separate and does not restore current ownership.
 */
public final class GarmentNoLongerCurrentException extends RuntimeException {

    public GarmentNoLongerCurrentException() {
        super("The garment is no longer in the owner's current wardrobe");
    }
}