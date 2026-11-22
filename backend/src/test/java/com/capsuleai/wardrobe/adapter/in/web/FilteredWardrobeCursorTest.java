package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-WAR-003 (S4-WAR-03, OPEN-S4-14): the criteria-bound v2 cursor beside the unchanged v1 cursor. */
class FilteredWardrobeCursorTest {

    private static final WardrobePosition POSITION =
            new WardrobePosition(Instant.parse("2026-11-05T09:30:00.123456Z"), UUID.randomUUID());
    private static final String FINGERPRINT = "0123456789abcdef";

    @Test
    void aFilteredCursorRoundTripsForItsOwnConditions() {
        String cursor = WardrobeCursor.encodeFiltered(POSITION, FINGERPRINT);

        assertThat(cursor).startsWith("djJ8").doesNotContain("=");
        assertThat(WardrobeCursor.read(cursor, Optional.of(FINGERPRINT)))
                .isEqualTo(new WardrobeCursor.Reading.At(POSITION));
    }

    @Test
    void aFilteredCursorIsRefusedForOtherConditionsAndWithoutConditions() {
        String cursor = WardrobeCursor.encodeFiltered(POSITION, FINGERPRINT);

        assertThat(WardrobeCursor.read(cursor, Optional.of("fedcba9876543210")))
                .isInstanceOf(WardrobeCursor.Reading.ConditionsChanged.class);
        assertThat(WardrobeCursor.read(cursor, Optional.empty()))
                .isInstanceOf(WardrobeCursor.Reading.ConditionsChanged.class);
    }

    @Test
    void theUnfilteredCursorIsUnchangedAndRefusedForConditions() {
        String cursor = WardrobeCursor.encode(POSITION);

        assertThat(cursor).startsWith("djF8");
        assertThat(WardrobeCursor.read(cursor, Optional.empty())).isEqualTo(new WardrobeCursor.Reading.At(POSITION));
        assertThat(WardrobeCursor.read(cursor, Optional.of(FINGERPRINT)))
                .isInstanceOf(WardrobeCursor.Reading.ConditionsChanged.class);
    }

    @Test
    void alteredFilteredCursorsAreMalformed() {
        String upperCaseFingerprint = encoded("v2|" + POSITION.createdAt() + "|" + POSITION.garmentId()
                + "|0123456789ABCDEF");
        String missingFingerprint = encoded("v2|" + POSITION.createdAt() + "|" + POSITION.garmentId());
        String padded = WardrobeCursor.encodeFiltered(POSITION, FINGERPRINT) + "=";

        assertThat(WardrobeCursor.read(upperCaseFingerprint, Optional.of(FINGERPRINT)))
                .isInstanceOf(WardrobeCursor.Reading.Malformed.class);
        assertThat(WardrobeCursor.read(missingFingerprint, Optional.of(FINGERPRINT)))
                .isInstanceOf(WardrobeCursor.Reading.Malformed.class);
        assertThat(WardrobeCursor.read(padded, Optional.of(FINGERPRINT)))
                .isInstanceOf(WardrobeCursor.Reading.Malformed.class);
        assertThat(WardrobeCursor.read("x".repeat(201), Optional.of(FINGERPRINT)))
                .isInstanceOf(WardrobeCursor.Reading.Malformed.class);
    }

    private static String encoded(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
