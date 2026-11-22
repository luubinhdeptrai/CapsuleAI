package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.BodyShape;
import com.capsuleai.personalcontext.domain.GenderContext;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-002 (S4-PROF-03, OPEN-S4-17): presence-aware changes, clearing, no-ops, conflicts and validation. */
class ReviseOptionalContextServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");

    private InMemoryOptionalContextRepository optionalContextRepository;
    private ReviseOptionalContextService reviseOptionalContextService;

    @BeforeEach
    void setUp() {
        optionalContextRepository = new InMemoryOptionalContextRepository();
        reviseOptionalContextService = new ReviseOptionalContextService(optionalContextRepository,
                new ImmediatePersonalContextTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void aFirstDeclarationCreatesRevisionOne() {
        OptionalContextRevision revision = revise(new ExpectedRevision.Unset(),
                ValueChange.replace("NON_BINARY"), ValueChange.keep());

        assertThat(revision.changed()).isTrue();
        OptionalContextProfile profile = revision.profile().orElseThrow();
        assertThat(profile.revision()).isEqualTo(1);
        assertThat(profile.values().genderContext()).contains(GenderContext.NON_BINARY);
        assertThat(profile.values().bodyShape()).isEmpty();
    }

    @Test
    void anOmittedFieldIsKeptAndNullClearsOnlyThatField() {
        revise(new ExpectedRevision.Unset(), ValueChange.replace("WOMAN"), ValueChange.replace("ROUND"));

        OptionalContextProfile cleared = revise(new ExpectedRevision.At(1), ValueChange.keep(), ValueChange.clear())
                .profile().orElseThrow();

        assertThat(cleared.revision()).isEqualTo(2);
        assertThat(cleared.values().genderContext()).contains(GenderContext.WOMAN);
        assertThat(cleared.values().bodyShape()).isEmpty();
    }

    @Test
    void clearingEverythingKeepsTheRecordWithAbsentValuesAndAdvancesTheRevision() {
        revise(new ExpectedRevision.Unset(), ValueChange.replace("MAN"), ValueChange.replace("TRIANGLE"));

        OptionalContextProfile cleared = revise(new ExpectedRevision.At(1), ValueChange.clear(), ValueChange.clear())
                .profile().orElseThrow();

        assertThat(cleared.values().declaresNothing()).isTrue();
        assertThat(cleared.revision()).isEqualTo(2);
    }

    @Test
    void anEmptyFirstOperationCreatesNothing() {
        OptionalContextRevision revision = revise(new ExpectedRevision.Unset(), ValueChange.clear(), ValueChange.keep());

        assertThat(revision.changed()).isFalse();
        assertThat(revision.profile()).isEmpty();
        assertThat(optionalContextRepository.writes()).isZero();
    }

    @Test
    void aChangeToTheCurrentValuesKeepsTheRevisionAndWritesNothing() {
        revise(new ExpectedRevision.Unset(), ValueChange.replace("NON_BINARY"), ValueChange.keep());

        OptionalContextRevision repeated = revise(new ExpectedRevision.At(1), ValueChange.replace("NON_BINARY"),
                ValueChange.keep());

        assertThat(repeated.changed()).isFalse();
        assertThat(repeated.profile().orElseThrow().revision()).isEqualTo(1);
        assertThat(optionalContextRepository.writes()).isOne();
    }

    @Test
    void aStaleRevisionAndASecondFirstDeclarationAreConflicts() {
        revise(new ExpectedRevision.Unset(), ValueChange.replace("WOMAN"), ValueChange.keep());

        assertThatThrownBy(() -> revise(new ExpectedRevision.Unset(), ValueChange.replace("MAN"), ValueChange.keep()))
                .isInstanceOf(OptionalContextRevisionConflictException.class);
        assertThatThrownBy(() -> revise(new ExpectedRevision.At(4), ValueChange.replace("MAN"), ValueChange.keep()))
                .isInstanceOf(OptionalContextRevisionConflictException.class);
        assertThat(optionalContextRepository.find(OWNER).orElseThrow().values().genderContext())
                .contains(GenderContext.WOMAN);
    }

    @Test
    void unsupportedValuesAreRejectedPerFieldAndChangeNothing() {
        assertThatThrownBy(() -> revise(new ExpectedRevision.Unset(), ValueChange.replace("woman"),
                ValueChange.replace("SLIM")))
                .isInstanceOfSatisfying(OptionalContextValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("genderContext", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("bodyShape", PreferenceValueProblem.UNSUPPORTED_VALUE)));
        assertThatThrownBy(() -> revise(new ExpectedRevision.Unset(), ValueChange.malformed(), ValueChange.keep()))
                .isInstanceOf(OptionalContextValidationException.class);
        assertThat(optionalContextRepository.find(OWNER)).isEmpty();
        assertThat(BodyShape.fromCode("SLIM")).isEmpty();
    }

    @Test
    void aMissingOrMalformedExpectedRevisionIsRejectedBeforeAnyRead() {
        assertThatThrownBy(() -> revise(new ExpectedRevision.Missing(), ValueChange.replace("MAN"), ValueChange.keep()))
                .isInstanceOfSatisfying(OptionalContextValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("expectedRevision", PreferenceValueProblem.REQUIRED)));
        assertThatThrownBy(() -> revise(new ExpectedRevision.Malformed(), ValueChange.keep(), ValueChange.keep()))
                .isInstanceOfSatisfying(OptionalContextValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("expectedRevision", PreferenceValueProblem.INVALID_FORMAT)));
    }

    /**
     * Every combination of omitted, null, the current code and another code, field by field, against a record
     * that declares both values: each field follows only its own change, and the revision advances exactly when
     * the resulting values differ (S4-PROF-03).
     */
    @Test
    void everyCombinationOfOmittedNullAndACodeIsAppliedFieldByField() {
        List<ValueChange<String>> genderChanges = List.of(ValueChange.keep(), ValueChange.clear(),
                ValueChange.replace("WOMAN"), ValueChange.replace("MAN"));
        List<ValueChange<String>> bodyShapeChanges = List.of(ValueChange.keep(), ValueChange.clear(),
                ValueChange.replace("ROUND"), ValueChange.replace("HOURGLASS"));
        for (ValueChange<String> genderChange : genderChanges) {
            for (ValueChange<String> bodyShapeChange : bodyShapeChanges) {
                setUp();
                revise(new ExpectedRevision.Unset(), ValueChange.replace("WOMAN"), ValueChange.replace("ROUND"));
                Optional<GenderContext> expectedGender =
                        expected(Optional.of(GenderContext.WOMAN), genderChange, GenderContext::fromCode);
                Optional<BodyShape> expectedBodyShape =
                        expected(Optional.of(BodyShape.ROUND), bodyShapeChange, BodyShape::fromCode);
                boolean changes = !expectedGender.equals(Optional.of(GenderContext.WOMAN))
                        || !expectedBodyShape.equals(Optional.of(BodyShape.ROUND));
                String combination = genderChange + " / " + bodyShapeChange;

                OptionalContextRevision revision = revise(new ExpectedRevision.At(1), genderChange, bodyShapeChange);

                OptionalContextProfile profile = revision.profile().orElseThrow();
                assertThat(profile.values().genderContext()).as(combination).isEqualTo(expectedGender);
                assertThat(profile.values().bodyShape()).as(combination).isEqualTo(expectedBodyShape);
                assertThat(revision.changed()).as(combination).isEqualTo(changes);
                assertThat(profile.revision()).as(combination).isEqualTo(changes ? 2 : 1);
                assertThat(optionalContextRepository.writes()).as(combination).isEqualTo(changes ? 2 : 1);
                assertThat(optionalContextRepository.find(OWNER)).as(combination).contains(profile);
            }
        }
    }

    /** Without a record, a first declaration is created only when some code is submitted; null only is a no-op. */
    @Test
    void aFirstDeclarationIsCreatedOnlyWhenACodeIsSubmitted() {
        List<ValueChange<String>> genderChanges = List.of(ValueChange.keep(), ValueChange.clear(),
                ValueChange.replace("ANOTHER_GENDER"));
        List<ValueChange<String>> bodyShapeChanges = List.of(ValueChange.keep(), ValueChange.clear(),
                ValueChange.replace("INVERTED_TRIANGLE"));
        for (ValueChange<String> genderChange : genderChanges) {
            for (ValueChange<String> bodyShapeChange : bodyShapeChanges) {
                setUp();
                Optional<GenderContext> expectedGender = expected(Optional.empty(), genderChange, GenderContext::fromCode);
                Optional<BodyShape> expectedBodyShape = expected(Optional.empty(), bodyShapeChange, BodyShape::fromCode);
                boolean declares = expectedGender.isPresent() || expectedBodyShape.isPresent();
                String combination = genderChange + " / " + bodyShapeChange;

                OptionalContextRevision revision = revise(new ExpectedRevision.Unset(), genderChange, bodyShapeChange);

                assertThat(revision.changed()).as(combination).isEqualTo(declares);
                assertThat(optionalContextRepository.writes()).as(combination).isEqualTo(declares ? 1 : 0);
                assertThat(revision.profile()).as(combination).isEqualTo(optionalContextRepository.find(OWNER));
                if (declares) {
                    OptionalContextProfile created = revision.profile().orElseThrow();
                    assertThat(created.revision()).as(combination).isEqualTo(OptionalContextProfile.FIRST_REVISION);
                    assertThat(created.values().genderContext()).as(combination).isEqualTo(expectedGender);
                    assertThat(created.values().bodyShape()).as(combination).isEqualTo(expectedBodyShape);
                } else {
                    assertThat(revision.profile()).as(combination).isEmpty();
                }
            }
        }
    }

    /** An empty string is not an undocumented way to clear (S4-PROF-03): it is unsupported and changes nothing. */
    @Test
    void anEmptyStringIsAnUnsupportedValueNotAClear() {
        revise(new ExpectedRevision.Unset(), ValueChange.replace("WOMAN"), ValueChange.keep());

        assertThatThrownBy(() -> revise(new ExpectedRevision.At(1), ValueChange.replace(""), ValueChange.keep()))
                .isInstanceOfSatisfying(OptionalContextValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("genderContext", PreferenceValueProblem.UNSUPPORTED_VALUE)));
        OptionalContextProfile current = optionalContextRepository.find(OWNER).orElseThrow();
        assertThat(current.values().genderContext()).contains(GenderContext.WOMAN);
        assertThat(current.revision()).isEqualTo(1);
    }

    /** The specification of one field: omitted keeps, null clears, a code sets. */
    private static <V> Optional<V> expected(Optional<V> current, ValueChange<String> change,
                                            Function<String, Optional<V>> fromCode) {
        if (change instanceof ValueChange.Clear<?>) {
            return Optional.empty();
        }
        if (change instanceof ValueChange.Replace<String> replace) {
            return Optional.of(fromCode.apply(replace.value()).orElseThrow());
        }
        return current;
    }

    private OptionalContextRevision revise(ExpectedRevision expectedRevision, ValueChange<String> genderContext,
                                           ValueChange<String> bodyShape) {
        return reviseOptionalContextService.revise(new ReviseOptionalContextCommand(OWNER, expectedRevision,
                new OptionalContextChanges(genderContext, bodyShape)));
    }
}
