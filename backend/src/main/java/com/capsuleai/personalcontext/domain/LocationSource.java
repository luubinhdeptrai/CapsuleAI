package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * How the user chose a location (US-CTX-001 AC-01). The backend cannot attest that a permission dialog was
 * shown; DEVICE only states that the client submitted a device-derived point, rounded by the server.
 */
public enum LocationSource {
    DEVICE,
    MANUAL;

    /** The value whose code equals {@code code} exactly; no case folding or fallback. */
    public static Optional<LocationSource> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.name().equals(code)).findFirst();
    }
}