package com.capsuleai.personalcontext.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * US-CTX-001 — a submitted selection exactly as received: missing values are null; {@code wrongTypeFields}
 * names the members whose JSON kind was wrong (for example a latitude given as text). Validation and rounding
 * happen in {@link ReviseLocationService}.
 */
public record SubmittedLocation(BigDecimal latitude, BigDecimal longitude, String label, String source,
                                List<String> wrongTypeFields) {

    /** Request field names, shared by the web mapping and the validation so that field errors always match. */
    public static final String LATITUDE_FIELD = "selection.latitude";
    public static final String LONGITUDE_FIELD = "selection.longitude";
    public static final String LABEL_FIELD = "selection.label";
    public static final String SOURCE_FIELD = "selection.source";

    public SubmittedLocation {
        wrongTypeFields = List.copyOf(wrongTypeFields);
    }
}
