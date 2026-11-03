package com.capsuleai.identity.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    /** 😀 — one Unicode code point stored as two Java chars. */
    private static final String EMOJI = "\uD83D\uDE00";

    @ParameterizedTest(name = "{0} characters are accepted")
    @ValueSource(ints = {12, 128})
    void acceptsTheLengthBoundaries(int length) {
        assertThatCode(() -> PasswordPolicy.validateNewPassword("p".repeat(length)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsElevenCharacters() {
        assertProblem("p".repeat(11), ValueProblem.TOO_SHORT);
    }

    @Test
    void rejects129Characters() {
        assertProblem("p".repeat(129), ValueProblem.TOO_LONG);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "only lowercase letters here",
            "123456789012",
            "            ",
            "M\u1EADt kh\u1EA9u r\u1EA5t d\u00E0i 2026",
            "\u043F\u0430\u0440\u043E\u043B\u044C-\u043F\u0430\u0440\u043E\u043B\u044C"
    })
    void acceptsAnyMixOfCharactersWithoutCompositionRules(String candidate) {
        assertThatCode(() -> PasswordPolicy.validateNewPassword(candidate))
                .doesNotThrowAnyException();
    }

    @Test
    void countsUnicodeCodePointsRatherThanJavaChars() {
        String twelveEmoji = EMOJI.repeat(12);
        assertThat(twelveEmoji.length()).isEqualTo(24);

        assertThatCode(() -> PasswordPolicy.validateNewPassword(twelveEmoji)).doesNotThrowAnyException();
        assertProblem(EMOJI.repeat(11), ValueProblem.TOO_SHORT);
        assertThatCode(() -> PasswordPolicy.validateNewPassword(EMOJI.repeat(128))).doesNotThrowAnyException();
        assertProblem(EMOJI.repeat(129), ValueProblem.TOO_LONG);
    }

    @Test
    void surroundingSpacesCountBecauseTheValueIsNeverTrimmed() {
        // 1 space + 11 letters = 12 characters; trimming would make it 11 and fail.
        assertThatCode(() -> PasswordPolicy.validateNewPassword(" " + "p".repeat(11)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnpairedSurrogates() {
        assertProblem("valid-prefix-" + "\uD83D", ValueProblem.INVALID_CHARACTERS);
        assertProblem("\uDE00" + "valid-suffix-", ValueProblem.INVALID_CHARACTERS);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void missingPasswordIsRequired(String candidate) {
        assertProblem(candidate, ValueProblem.REQUIRED);
    }

    private static void assertProblem(String candidate, ValueProblem expected) {
        assertThatThrownBy(() -> PasswordPolicy.validateNewPassword(candidate))
                .isInstanceOfSatisfying(InvalidValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(expected));
    }
}