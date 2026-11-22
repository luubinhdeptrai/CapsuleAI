package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.WardrobeSearchCriteria;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** US-WAR-003 (OPEN-S4-12/13/14): parsing and validation of the overview's search parameters. */
class WardrobeSearchRequestTest {

    private static final WardrobePosition POSITION =
            new WardrobePosition(Instant.parse("2026-11-05T09:30:00.123456Z"), UUID.randomUUID());

    @Test
    void noParametersIsTheUnrestrictedFirstPage() {
        WardrobeSearchRequest request = WardrobeSearchRequest.parse(null, null, null, null, null);

        assertThat(request.criteria().isUnrestricted()).isTrue();
        assertThat(request.pageSize()).isEqualTo(20);
        assertThat(request.after()).isEmpty();
    }

    @Test
    void categoryAndColorCodesAreCollectedWithoutDuplicates() {
        WardrobeSearchRequest request = WardrobeSearchRequest.parse(null, List.of("TOPS", "PANTS", "TOPS"),
                List.of("NAVY"), "5", null);

        assertThat(request.criteria().categories()).containsExactlyInAnyOrder(GarmentCategory.TOPS, GarmentCategory.PANTS);
        assertThat(request.criteria().colors()).containsExactly(GarmentColor.NAVY);
        assertThat(request.pageSize()).isEqualTo(5);
    }

    @Test
    void unknownOrLowerCaseCodesAreUnsupported() {
        assertThat(errorsOf(() -> WardrobeSearchRequest.parse(null, List.of("SOCKS"), List.of("navy"), null, null)))
                .containsExactly(new InvalidWardrobePageException.FieldError("category", "UNSUPPORTED_VALUE"),
                        new InvalidWardrobePageException.FieldError("color", "UNSUPPORTED_VALUE"));
    }

    @Test
    void moreThanFiftyValuesInOneGroupAreOutOfRange() {
        assertThat(errorsOf(() -> WardrobeSearchRequest.parse(null, Collections.nCopies(51, "TOPS"), null, null,
                null))).containsExactly(new InvalidWardrobePageException.FieldError("category", "OUT_OF_RANGE"));
    }

    @Test
    void anOverlongOrControlCharacterQueryIsRejectedAndABlankQueryIsNoCondition() {
        assertThat(errorsOf(() -> WardrobeSearchRequest.parse("a".repeat(101), null, null, null, null)))
                .containsExactly(new InvalidWardrobePageException.FieldError("q", "OUT_OF_RANGE"));
        assertThat(errorsOf(() -> WardrobeSearchRequest.parse("a\u0007", null, null, null, null)))
                .containsExactly(new InvalidWardrobePageException.FieldError("q", "INVALID_FORMAT"));
        assertThat(WardrobeSearchRequest.parse("   ", null, null, null, null).criteria().isUnrestricted()).isTrue();
    }

    @Test
    void aCursorIssuedForOtherConditionsIsConditionsChanged() {
        String cursor = WardrobeCursor.encodeFiltered(POSITION, criteria("navy", Set.of()).fingerprint());

        assertThat(errorsOf(() -> WardrobeSearchRequest.parse("linen", null, null, null, cursor)))
                .containsExactly(new InvalidWardrobePageException.FieldError("cursor", "CONDITIONS_CHANGED"));
    }

    @Test
    void aMalformedCursorIsInvalidFormat() {
        assertThat(errorsOf(() -> WardrobeSearchRequest.parse("navy", null, null, null, "not-a-cursor")))
                .containsExactly(new InvalidWardrobePageException.FieldError("cursor", "INVALID_FORMAT"));
    }

    @Test
    void aCursorForTheSameConditionsContinuesWhateverTheOrderOrCase() {
        String cursor = WardrobeCursor.encodeFiltered(POSITION,
                criteria("Navy", Set.of(GarmentCategory.TOPS, GarmentCategory.PANTS)).fingerprint());

        WardrobeSearchRequest request = WardrobeSearchRequest.parse("navy", List.of("PANTS", "TOPS"), null, null,
                cursor);

        assertThat(request.after()).contains(POSITION);
    }

    @Test
    void unfilteredAndFilteredCursorsAreNotInterchangeable() {
        String unfiltered = WardrobeCursor.encode(POSITION);
        String filtered = WardrobeCursor.encodeFiltered(POSITION, criteria("navy", Set.of()).fingerprint());

        assertThat(errorsOf(() -> WardrobeSearchRequest.parse("navy", null, null, null, unfiltered)))
                .containsExactly(new InvalidWardrobePageException.FieldError("cursor", "CONDITIONS_CHANGED"));
        assertThat(errorsOf(() -> WardrobeSearchRequest.parse(null, null, null, null, filtered)))
                .containsExactly(new InvalidWardrobePageException.FieldError("cursor", "CONDITIONS_CHANGED"));
    }

    @Test
    void aCursorIssuedUnderTheEnglishOnlyMatchingPolicyIsConditionsChanged() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest("m1|q=áo thun|c=|k=".getBytes(StandardCharsets.UTF_8));
        String englishOnlyCursor = WardrobeCursor.encodeFiltered(POSITION,
                HexFormat.of().formatHex(digest).substring(0, 16));

        assertThat(errorsOf(() -> WardrobeSearchRequest.parse("áo thun", null, null, null, englishOnlyCursor)))
                .as("the same conditions under another matching policy start again from the first page")
                .containsExactly(new InvalidWardrobePageException.FieldError("cursor", "CONDITIONS_CHANGED"));
        assertThat(WardrobeSearchRequest.parse("áo thun", null, null, null,
                WardrobeCursor.encodeFiltered(POSITION, criteria("áo thun", Set.of()).fingerprint())).after())
                .contains(POSITION);
    }

    private static WardrobeSearchCriteria criteria(String text, Set<GarmentCategory> categories) {
        return new WardrobeSearchCriteria(Optional.of(text), categories, Set.of());
    }

    private static List<InvalidWardrobePageException.FieldError> errorsOf(Executable parse) {
        return assertThrows(InvalidWardrobePageException.class, parse).errors();
    }
}
