package com.capsuleai.personalcontext.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Objects;
import java.util.Optional;

/**
 * US-CTX-001 (S4-CTX-01, OPEN-S4-18) — the user's one current location: a point at the reviewed precision, a
 * display label and its source. The server rounds every submitted point to {@link #SCALE} decimals (about
 * 1.1 km) before it is stored or sent to the weather provider; raw device precision is never kept. A MANUAL
 * selection needs a label (the city the client resolved); a DEVICE selection may have none.
 */
public record LocationSelection(BigDecimal latitude, BigDecimal longitude, Optional<String> label,
                                LocationSource source) {

    /** OPEN-S4-18: two decimal places. */
    public static final int SCALE = 2;

    /** OPEN-S4-18: the longest label, in Unicode code points after normalization. */
    public static final int MAXIMUM_LABEL_CODE_POINTS = 80;

    private static final BigDecimal MAXIMUM_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MAXIMUM_LONGITUDE = new BigDecimal("180");

    public LocationSelection {
        Objects.requireNonNull(latitude, "latitude");
        Objects.requireNonNull(longitude, "longitude");
        Objects.requireNonNull(label, "label must be Optional.empty() without a label");
        Objects.requireNonNull(source, "source");
        if (latitude.scale() != SCALE || longitude.scale() != SCALE) {
            throw new IllegalArgumentException("Coordinates must be rounded to " + SCALE + " decimals");
        }
        if (!withinRange(latitude, MAXIMUM_LATITUDE) || !withinRange(longitude, MAXIMUM_LONGITUDE)) {
            throw new IllegalArgumentException("Coordinates are outside the geographic range");
        }
        if (label.isPresent() && !isAcceptableLabel(label.get())) {
            throw new IllegalArgumentException("The label must be normalized, 1-80 code points, without control characters");
        }
        if (source == LocationSource.MANUAL && label.isEmpty()) {
            throw new IllegalArgumentException("A manual selection needs a label");
        }
    }

    /** A selection from submitted values, with both coordinates rounded half-even to {@link #SCALE} decimals. */
    public static LocationSelection rounded(BigDecimal latitude, BigDecimal longitude, Optional<String> label,
                                            LocationSource source) {
        return new LocationSelection(round(latitude), round(longitude), label, source);
    }

    public static BigDecimal round(BigDecimal coordinate) {
        return coordinate.setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    /** Whether a rounded latitude lies within -90 to 90. */
    public static boolean isLatitude(BigDecimal roundedLatitude) {
        return withinRange(roundedLatitude, MAXIMUM_LATITUDE);
    }

    /** Whether a rounded longitude lies within -180 to 180. */
    public static boolean isLongitude(BigDecimal roundedLongitude) {
        return withinRange(roundedLongitude, MAXIMUM_LONGITUDE);
    }

    /** The NFC-normalized label without outer whitespace, or empty when missing or blank. */
    public static Optional<String> normalizeLabel(String rawLabel) {
        if (rawLabel == null) {
            return Optional.empty();
        }
        String normalized = Normalizer.normalize(rawLabel, Normalizer.Form.NFC).strip();
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    /** Whether a normalized label is short enough and free of control characters. */
    public static boolean isAcceptableLabel(String normalizedLabel) {
        return !normalizedLabel.isEmpty()
                && normalizedLabel.codePointCount(0, normalizedLabel.length()) <= MAXIMUM_LABEL_CODE_POINTS
                && normalizedLabel.codePoints().noneMatch(Character::isISOControl)
                && normalizeLabel(normalizedLabel).equals(Optional.of(normalizedLabel));
    }

    private static boolean withinRange(BigDecimal value, BigDecimal maximum) {
        return value.compareTo(maximum) <= 0 && value.compareTo(maximum.negate()) >= 0;
    }
}