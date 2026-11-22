package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The opaque {@code cursor} of the wardrobe overview (OPEN-S2-10): base64url, without
 * padding, of {@code v1|<createdAt>|<garment id>} for the last garment of a page.
 *
 * <p>Clients must treat it as opaque: send back exactly what {@code nextCursor} gave.
 * It is neither a secret nor an authorization. Every page is still restricted to the
 * authenticated owner's current garments, so an altered or foreign cursor can only
 * move the position within the caller's own wardrobe. Only the canonical encoding is
 * accepted, so each position has exactly one cursor.
 */
final class WardrobeCursor {

    /** Far above any canonical cursor (about 90 characters); longer input is rejected unread. */
    static final int MAXIMUM_LENGTH = 200;

    private static final String VERSION = "v1";

    /** US-WAR-003 (OPEN-S4-14): a cursor issued for a page with conditions carries their fingerprint. */
    private static final String FILTERED_VERSION = "v2";
    private static final Pattern FINGERPRINT = Pattern.compile("[0-9a-f]{16}");
    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    /**
     * A garment's creation time always has a four-digit year. An instant outside these bounds
     * parses, but it cannot have been issued and PostgreSQL could not compare it, so it is
     * rejected here as an unusable cursor instead of failing later as a database error.
     */
    private static final Instant EARLIEST_POSITION = Instant.parse("0000-01-01T00:00:00Z");
    private static final Instant LATEST_POSITION = Instant.parse("9999-12-31T23:59:59.999999999Z");

    private WardrobeCursor() {
    }

    static String encode(WardrobePosition position) {
        String text = VERSION + "|" + position.createdAt() + "|" + position.garmentId();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /** @return the position, or empty when the value is not a cursor this API issued */
    static Optional<WardrobePosition> decode(String cursor) {
        if (cursor == null || cursor.isEmpty() || cursor.length() > MAXIMUM_LENGTH) {
            return Optional.empty();
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = text.split("\\|", -1);
            if (parts.length != 3 || !parts[0].equals(VERSION) || !CANONICAL_UUID.matcher(parts[2]).matches()) {
                return Optional.empty();
            }
            Instant createdAt = Instant.parse(parts[1]);
            if (createdAt.isBefore(EARLIEST_POSITION) || createdAt.isAfter(LATEST_POSITION)) {
                return Optional.empty();
            }
            WardrobePosition position = new WardrobePosition(createdAt, UUID.fromString(parts[2]));
            return encode(position).equals(cursor) ? Optional.of(position) : Optional.empty();
        } catch (IllegalArgumentException | DateTimeParseException notACursor) {
            return Optional.empty();
        }
    }

    /** US-WAR-003: the outcome of reading a cursor for given search conditions. */
    sealed interface Reading permits Reading.At, Reading.Malformed, Reading.ConditionsChanged {

        /** A usable position for these conditions. */
        record At(WardrobePosition position) implements Reading {
        }

        /** Not a cursor this API issued. */
        record Malformed() implements Reading {
        }

        /** A real cursor, issued for other conditions (or without any): start again from the first page. */
        record ConditionsChanged() implements Reading {
        }
    }

    /** A {@code v2|<createdAt>|<garment id>|<conditions fingerprint>} cursor for a filtered page. */
    static String encodeFiltered(WardrobePosition position, String conditionsFingerprint) {
        String text = FILTERED_VERSION + "|" + position.createdAt() + "|" + position.garmentId() + "|"
                + conditionsFingerprint;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads a cursor for a request whose conditions have {@code conditionsFingerprint} (empty: no conditions).
     * An unfiltered request accepts only v1 cursors and a filtered one only v2 cursors with its own fingerprint.
     */
    static Reading read(String cursor, Optional<String> conditionsFingerprint) {
        if (cursor == null || cursor.isEmpty() || cursor.length() > MAXIMUM_LENGTH) {
            return new Reading.Malformed();
        }
        Optional<WardrobePosition> unfiltered = decode(cursor);
        if (unfiltered.isPresent()) {
            return conditionsFingerprint.isEmpty() ? new Reading.At(unfiltered.get()) : new Reading.ConditionsChanged();
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = text.split("\\|", -1);
            if (parts.length != 4 || !parts[0].equals(FILTERED_VERSION) || !CANONICAL_UUID.matcher(parts[2]).matches()
                    || !FINGERPRINT.matcher(parts[3]).matches()) {
                return new Reading.Malformed();
            }
            Instant createdAt = Instant.parse(parts[1]);
            if (createdAt.isBefore(EARLIEST_POSITION) || createdAt.isAfter(LATEST_POSITION)) {
                return new Reading.Malformed();
            }
            WardrobePosition position = new WardrobePosition(createdAt, UUID.fromString(parts[2]));
            if (!encodeFiltered(position, parts[3]).equals(cursor)) {
                return new Reading.Malformed();
            }
            return conditionsFingerprint.filter(parts[3]::equals).isPresent()
                    ? new Reading.At(position) : new Reading.ConditionsChanged();
        } catch (IllegalArgumentException | DateTimeParseException notACursor) {
            return new Reading.Malformed();
        }
    }
}