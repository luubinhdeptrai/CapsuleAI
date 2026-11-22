package com.capsuleai.personalcontext.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-PROF-002 (S4-PROF-01/02, OPEN-S4-16): absence, revisions and exact codes of optional context. */
class OptionalContextTest {

    private static final Instant DECLARED_AT = Instant.parse("2026-11-16T09:30:00Z");

    @Test
    void absenceIsTheDefaultAndDeclaresNothing() {
        assertThat(OptionalContext.none().declaresNothing()).isTrue();
        assertThat(new OptionalContext(Optional.of(GenderContext.NON_BINARY), Optional.empty()).declaresNothing())
                .isFalse();
    }

    @Test
    void aRevisionAdvancesByOneAndNeverMovesTheUpdateBeforeTheCreation() {
        OptionalContextProfile first = OptionalContextProfile.firstDeclaration(UUID.randomUUID(),
                new OptionalContext(Optional.empty(), Optional.of(BodyShape.ROUND)), DECLARED_AT);

        OptionalContextProfile revised = first.revisedTo(OptionalContext.none(), DECLARED_AT.minusSeconds(30));

        assertThat(first.revision()).isEqualTo(OptionalContextProfile.FIRST_REVISION);
        assertThat(revised.revision()).isEqualTo(2);
        assertThat(revised.updatedAt()).isEqualTo(DECLARED_AT);
        assertThat(revised.values().declaresNothing()).isTrue();
    }

    @Test
    void codesAreExactWithoutCaseFoldingOrTrimming() {
        assertThat(GenderContext.fromCode("WOMAN")).contains(GenderContext.WOMAN);
        assertThat(GenderContext.fromCode("woman")).isEmpty();
        assertThat(GenderContext.fromCode(" WOMAN")).isEmpty();
        assertThat(BodyShape.fromCode("Round")).isEmpty();
        assertThat(BodyShape.fromCode(null)).isEmpty();
    }

    /** Pins the delegated vocabulary (OPEN-S4-16) so that any change is a deliberate, reviewed decision. */
    @Test
    void theVocabulariesAreExactlyTheSelectedDelegatedDefaults() {
        assertThat(Arrays.stream(GenderContext.values()).map(Enum::name))
                .containsExactly("WOMAN", "MAN", "NON_BINARY", "ANOTHER_GENDER");
        assertThat(Arrays.stream(BodyShape.values()).map(Enum::name))
                .containsExactly("RECTANGLE", "TRIANGLE", "INVERTED_TRIANGLE", "HOURGLASS", "ROUND");
    }
}
