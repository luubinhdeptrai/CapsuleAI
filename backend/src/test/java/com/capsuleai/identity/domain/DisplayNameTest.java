package com.capsuleai.identity.domain;

import java.text.Normalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisplayNameTest {

    /** "Nguyễn Thị Ánh" with precomposed letters (NFC). */
    private static final String NFC_NAME = "Nguy\u1EC5n Th\u1ECB \u00C1nh";
    /** The same visible text written with combining marks (NFD). */
    private static final String NFD_NAME = Normalizer.normalize(NFC_NAME, Normalizer.Form.NFD);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void omittedNameIsAbsent(String raw) {
        assertThat(DisplayName.optional(raw)).isEmpty();
    }

    @Test
    void keepsVietnameseDiacriticsExactly() {
        assertThat(DisplayName.optional(NFC_NAME)).map(DisplayName::value).contains(NFC_NAME);
    }

    @Test
    void doesNotNormalizeTheUnicodeForm() {
        assertThat(NFD_NAME).isNotEqualTo(NFC_NAME);
        assertThat(DisplayName.optional(NFD_NAME)).map(DisplayName::value).contains(NFD_NAME);
    }

    @Test
    void keepsSurroundingSpacesAsEntered() {
        assertThat(DisplayName.optional(" Minh ")).map(DisplayName::value).contains(" Minh ");
    }

    @Test
    void acceptsOneHundredCharactersAndRejectsMore() {
        assertThat(DisplayName.optional("n".repeat(100))).isPresent();
        assertThatThrownBy(() -> DisplayName.optional("n".repeat(101)))
                .isInstanceOfSatisfying(InvalidValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(ValueProblem.TOO_LONG));
    }
}