package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class WardrobeCursorTest {

    private static final String ID = "4b9f3c7e-2a1d-4c8e-9f6a-1e2d3c4b5a69";
    private static final WardrobePosition POSITION =
            new WardrobePosition(Instant.parse("2026-11-05T09:30:00.123456Z"), UUID.fromString(ID));

    @Test
    void anIssuedCursorIsUrlSafeAndDecodesToExactlyItsPosition() {
        String cursor = WardrobeCursor.encode(POSITION);

        assertThat(cursor).matches("[A-Za-z0-9_-]+").hasSizeLessThan(WardrobeCursor.MAXIMUM_LENGTH);
        assertThat(WardrobeCursor.decode(cursor)).contains(POSITION);
    }

    @Test
    void wholeSecondsAndNanosecondsRoundTripToo() {
        for (String instant : new String[] {"2026-11-05T09:30:00Z", "2026-11-05T09:30:00.123456789Z"}) {
            WardrobePosition position = new WardrobePosition(Instant.parse(instant), UUID.fromString(ID));
            assertThat(WardrobeCursor.decode(WardrobeCursor.encode(position))).contains(position);
        }
    }

    @Test
    void theEarliestAndLatestAcceptedTimesRoundTrip() {
        for (String instant : new String[] {"0000-01-01T00:00:00Z", "9999-12-31T23:59:59.999999999Z"}) {
            WardrobePosition position = new WardrobePosition(Instant.parse(instant), UUID.fromString(ID));
            assertThat(WardrobeCursor.decode(WardrobeCursor.encode(position))).contains(position);
        }
    }

    @ParameterizedTest
    @MethodSource("valuesThatAreNotIssuedCursors")
    void anythingThisApiDidNotIssueIsNotACursor(String candidate) {
        assertThat(WardrobeCursor.decode(candidate)).isEmpty();
    }

    static Stream<String> valuesThatAreNotIssuedCursors() {
        String issued = WardrobeCursor.encode(POSITION);
        return Stream.of(
                "",
                "not-a-cursor",
                "!!!!",
                issued + "==",
                issued.substring(0, issued.length() - 2),
                base64url("v2|2026-11-05T09:30:00.123456Z|" + ID),
                base64url("v1|yesterday|" + ID),
                base64url("v1|2026-11-05T09:30:00.000Z|" + ID),
                base64url("v1|2026-11-05T09:30:00Z|" + ID.toUpperCase()),
                base64url("v1|2026-11-05T09:30:00Z|not-a-uuid"),
                base64url("v1|2026-11-05T09:30:00Z"),
                base64url("v1|2026-11-05T09:30:00Z|" + ID + "|extra"),
                base64url("v1|+294277-01-01T00:00:00Z|" + ID),
                base64url("v1|-4714-01-01T00:00:00Z|" + ID),
                base64url("v1|" + Instant.MAX + "|" + ID),
                "A".repeat(WardrobeCursor.MAXIMUM_LENGTH + 1));
    }

    private static String base64url(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
