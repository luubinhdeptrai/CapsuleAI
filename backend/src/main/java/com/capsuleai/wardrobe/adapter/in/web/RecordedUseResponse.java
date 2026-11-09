package com.capsuleai.wardrobe.adapter.in.web;

/**
 * Whether reported-use evidence is available for a garment (US-WAR-002 AC-06/AC-07,
 * BRULE-HIST-001). No Wear capability exists in Sprint 2, so it is always
 * {@code UNAVAILABLE}: not zero uses, not "never worn", and no last-worn date.
 * A future Wear contract adds AVAILABLE with its own qualified indicators.
 */
public record RecordedUseResponse(String availability) {

    static final RecordedUseResponse UNAVAILABLE = new RecordedUseResponse("UNAVAILABLE");
}