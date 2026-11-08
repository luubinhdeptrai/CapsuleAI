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
}