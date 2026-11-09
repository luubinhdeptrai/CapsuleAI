package com.capsuleai.wardrobe.domain;

/**
 * How a garment's accepted values were established (BRULE-GAR-008, SRS §4.10).
 * Sprint 2 has only manual creation, so every accepted value is User Entered and
 * confirmed. AI Suggested, User Confirmed and User Corrected arrive with the
 * assisted-entry PBIs; no automated value can replace an accepted one (FR-GAR-011).
 */
public enum ValueProvenance {

    /** Entered by the user and explicitly confirmed on manual creation. */
    USER_ENTERED
}