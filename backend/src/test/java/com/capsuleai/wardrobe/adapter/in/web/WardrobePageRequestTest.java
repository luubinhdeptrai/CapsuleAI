package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.BrowseWardrobeService;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WardrobePageRequestTest {

    @Test
    void noParametersMeanTheFirstPageOfTheDefaultSize() {
        WardrobePageRequest request = WardrobePageRequest.parse(null, null);

        assertThat(request.pageSize()).isEqualTo(BrowseWardrobeService.DEFAULT_PAGE_SIZE).isEqualTo(20);
        assertThat(request.after()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "20", "100", "007"})
    void aLimitFromOneToOneHundredIsThePageSize(String limit) {
        assertThat(WardrobePageRequest.parse(limit, null).pageSize()).isEqualTo(Integer.parseInt(limit));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "101", "999999999"})
    void aLimitOutsideOneToOneHundredIsOutOfRange(String limit) {
        assertRejected(limit, null, new InvalidWardrobePageException.FieldError("limit", "OUT_OF_RANGE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "ten", "1.5", "1e2", "+5", "1,2", "9999999999"})
    void aLimitThatIsNotAnIntegerHasAnInvalidFormat(String limit) {
        assertRejected(limit, null, new InvalidWardrobePageException.FieldError("limit", "INVALID_FORMAT"));
    }

    @Test
    void anIssuedCursorContinuesAfterItsPosition() {
        WardrobePosition position = new WardrobePosition(Instant.parse("2026-11-05T09:30:00Z"), UUID.randomUUID());

        assertThat(WardrobePageRequest.parse("5", WardrobeCursor.encode(position)).after()).contains(position);
    }

    @Test
    void anUnusableCursorIsRejectedRatherThanTreatedAsTheFirstPage() {
        assertRejected(null, "", new InvalidWardrobePageException.FieldError("cursor", "INVALID_FORMAT"));
        assertRejected(null, "abc", new InvalidWardrobePageException.FieldError("cursor", "INVALID_FORMAT"));
    }

    @Test
    void everyProblemIsReportedAtOnce() {
        assertRejected("0", "abc",
                new InvalidWardrobePageException.FieldError("limit", "OUT_OF_RANGE"),
                new InvalidWardrobePageException.FieldError("cursor", "INVALID_FORMAT"));
    }

    private static void assertRejected(String limit, String cursor, InvalidWardrobePageException.FieldError... expected) {
        assertThatThrownBy(() -> WardrobePageRequest.parse(limit, cursor))
                .isInstanceOfSatisfying(InvalidWardrobePageException.class,
                        rejected -> assertThat(rejected.errors()).containsExactly(expected));
    }
}
