package com.capsuleai.identity.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAddressTest {

    @Test
    void stripsSurroundingWhitespaceAndLowerCases() {
        assertThat(EmailAddress.normalize("  Person@Example.COM \t").value())
                .isEqualTo("person@example.com");
    }

    @Test
    void caseAndWhitespaceVariantsAreTheSameIdentity() {
        assertThat(EmailAddress.normalize(" Person@Example.com "))
                .isEqualTo(EmailAddress.normalize("person@example.com"));
    }

    @ParameterizedTest
    @CsvSource({
            "person+news@example.com, person@example.com",
            "p.erson@gmail.com, person@gmail.com",
            "person@googlemail.com, person@gmail.com"
    })
    void providerAliasesAreNotMerged(String alias, String other) {
        assertThat(EmailAddress.normalize(alias)).isNotEqualTo(EmailAddress.normalize(other));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void missingEmailIsRequired(String raw) {
        assertProblem(raw, ValueProblem.REQUIRED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"person", "@example.com", "person@", "per son@example.com", "a@b@example.com"})
    void emailWithoutTheMinimalShapeIsRejected(String raw) {
        assertProblem(raw, ValueProblem.INVALID_FORMAT);
    }

    @Test
    void acceptsTheMaximumLengthAndRejectsOneMore() {
        String domain = "@example.com";
        String atLimit = "a".repeat(EmailAddress.MAX_LENGTH - domain.length()) + domain;

        assertThat(EmailAddress.normalize(atLimit).value()).hasSize(254);
        assertProblem("a" + atLimit, ValueProblem.TOO_LONG);
    }

    private static void assertProblem(String raw, ValueProblem expected) {
        assertThatThrownBy(() -> EmailAddress.normalize(raw))
                .isInstanceOfSatisfying(InvalidValueException.class,
                        rejected -> assertThat(rejected.problem()).isEqualTo(expected));
    }
}