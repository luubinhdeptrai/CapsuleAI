package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-001 — presence-aware, validated, revision-protected preference changes (S3-PROF-02/03/04). */
class RevisePreferencesServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();

    private final InMemoryPreferenceProfileRepository profiles = new InMemoryPreferenceProfileRepository();
    private final RevisePreferencesService service = new RevisePreferencesService(profiles,
            new ImmediatePersonalContextTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aFirstDeclarationCreatesTheProfileAtRevisionOne() {
        PreferencesRevision revision = service.revise(command(new ExpectedRevision.Unset(),
                ValueChange.replace(List.of("MINIMAL", "SMART_CASUAL")), needs("WORK_PROFESSIONAL", 3)));

        PreferenceProfile profile = revision.profile().orElseThrow();
        assertThat(revision.changed()).isTrue();
        assertThat(profile.revision()).isEqualTo(1L);
        assertThat(profile.preferences().styles()).containsExactly(ProfileStyle.MINIMAL, ProfileStyle.SMART_CASUAL);
        assertThat(profile.preferences().priorityOf(CommonNeed.WORK_PROFESSIONAL)).contains(NeedPriority.HIGH);
        assertThat(profiles.find(OWNER)).contains(profile);
    }

    @Test
    void omittedStylesAndNeedsKeepTheirValuesAndNullUnsetsANeed() {
        declare(List.of("CLASSIC"), needs("WORK_PROFESSIONAL", 3, "TRAVEL", 1));

        DeclaredPreferences revised = service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("TRAVEL", null))).profile().orElseThrow().preferences();

        assertThat(revised.styles()).containsExactly(ProfileStyle.CLASSIC);
        assertThat(revised.priorityOf(CommonNeed.WORK_PROFESSIONAL)).contains(NeedPriority.HIGH);
        assertThat(revised.priorityOf(CommonNeed.TRAVEL)).as("unset, not zero").isEmpty();
    }

    @Test
    void explicitZeroIsKeptAsNotRelevantAndNeverReplacedByADefault() {
        declare(List.of(), needs("SPORT_ACTIVE", 0));

        DeclaredPreferences revised = service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("TRAVEL", 2))).profile().orElseThrow().preferences();

        assertThat(revised.priorityOf(CommonNeed.SPORT_ACTIVE)).contains(NeedPriority.NOT_RELEVANT);
        assertThat(revised.commonNeeds()).hasSize(2);
    }

    @Test
    void clearingStylesLeavesAnEmptySelection() {
        declare(List.of("STREETWEAR", "SPORTY"), Map.of());

        PreferencesRevision revision = service.revise(command(new ExpectedRevision.At(1), ValueChange.clear(), Map.of()));

        assertThat(revision.profile().orElseThrow().preferences().styles()).isEmpty();
        assertThat(revision.profile().orElseThrow().revision()).isEqualTo(2L);
    }

    @Test
    void unsupportedStylesNeedsAndPrioritiesAreRejectedWithoutChanges() {
        declare(List.of("CASUAL"), Map.of());
        Map<String, ValueChange<Integer>> needs = new LinkedHashMap<>();
        needs.put("TRAVEL", ValueChange.replace(4));
        needs.put("NIGHT_OUT", ValueChange.replace(1));

        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.At(1),
                ValueChange.replace(List.of("CASUAL", "STREET")), needs)))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("styles[1]", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("commonNeeds.TRAVEL", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("commonNeeds", PreferenceValueProblem.UNSUPPORTED_VALUE)));
        assertThat(profiles.find(OWNER).orElseThrow().revision()).isEqualTo(1L);
    }

    @Test
    void aMissingOrMalformedExpectedRevisionIsRejectedBeforeAnyLookup() {
        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.Missing(), ValueChange.keep(), Map.of())))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("expectedRevision", PreferenceValueProblem.REQUIRED)));
        assertThatThrownBy(() -> service.revise(command(ExpectedRevision.ofToken("0"), ValueChange.keep(), Map.of())))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("expectedRevision", PreferenceValueProblem.INVALID_FORMAT)));
    }

    @Test
    void aSecondFirstDeclarationIsAConflict() {
        declare(List.of("MINIMAL"), Map.of());

        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.Unset(),
                ValueChange.replace(List.of("FORMAL")), Map.of())))
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThat(profiles.find(OWNER).orElseThrow().preferences().styles()).containsExactly(ProfileStyle.MINIMAL);
    }

    @Test
    void aStaleRevisionIsAConflictAndChangesNothing() {
        declare(List.of(), needs("WORK_PROFESSIONAL", 1));
        service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(), needs("WORK_PROFESSIONAL", 2)));

        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("WORK_PROFESSIONAL", 3))))
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("WORK_PROFESSIONAL", 2))))
                .as("OPEN-S3-06: a stale precondition is a conflict even when nothing would change")
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThat(profiles.find(OWNER).orElseThrow().preferences().priorityOf(CommonNeed.WORK_PROFESSIONAL))
                .contains(NeedPriority.MEDIUM);
    }

    @Test
    void anUnchangedRevisionAndAnEmptyFirstDeclarationWriteNothing() {
        PreferencesRevision emptyFirst = service.revise(command(new ExpectedRevision.Unset(), ValueChange.clear(), Map.of()));
        assertThat(emptyFirst.changed()).isFalse();
        assertThat(emptyFirst.profile()).isEmpty();

        declare(List.of("CLASSIC"), needs("TRAVEL", 0));
        int writesBefore = profiles.writes();
        PreferencesRevision same = service.revise(command(new ExpectedRevision.At(1),
                ValueChange.replace(List.of("CLASSIC", "CLASSIC")), needs("TRAVEL", 0)));

        assertThat(same.changed()).isFalse();
        assertThat(same.profile().orElseThrow().revision()).isEqualTo(1L);
        assertThat(profiles.writes()).isEqualTo(writesBefore);
    }

    @Test
    void valuesOfTheWrongKindAndANullNeedObjectAreRejected() {
        assertThatThrownBy(() -> service.revise(new RevisePreferencesCommand(OWNER, new ExpectedRevision.Unset(),
                new PreferenceChanges(ValueChange.malformed(), Map.of("TRAVEL", ValueChange.malformed()), true))))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("styles", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("commonNeeds", PreferenceValueProblem.UNSUPPORTED_VALUE)));
    }

    @Test
    void aConcurrentFirstDeclarationDetectedAtWriteTimeIsAConflict() {
        InMemoryPreferenceProfileRepository racing = new InMemoryPreferenceProfileRepository() {
            @Override
            public synchronized boolean createIfAbsent(PreferenceProfile profile) {
                super.createIfAbsent(PreferenceProfile.firstDeclaration(OWNER,
                        new DeclaredPreferences(Set.of(ProfileStyle.FORMAL), Map.of()), NOW));
                return super.createIfAbsent(profile);
            }
        };
        RevisePreferencesService racingService = new RevisePreferencesService(racing,
                new ImmediatePersonalContextTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> racingService.revise(command(new ExpectedRevision.Unset(),
                ValueChange.replace(List.of("MINIMAL")), Map.of())))
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThat(racing.find(OWNER).orElseThrow().preferences().styles()).containsExactly(ProfileStyle.FORMAL);
    }

    private void declare(List<String> styles, Map<String, ValueChange<Integer>> needs) {
        service.revise(command(new ExpectedRevision.Unset(), ValueChange.replace(styles), needs));
    }

    private static RevisePreferencesCommand command(ExpectedRevision expected, ValueChange<List<String>> styles,
                                                    Map<String, ValueChange<Integer>> needs) {
        return new RevisePreferencesCommand(OWNER, expected, new PreferenceChanges(styles, needs, false));
    }

    /** Alternating need code and weight; a null weight means "unset this need". */
    private static Map<String, ValueChange<Integer>> needs(Object... codesAndWeights) {
        Map<String, ValueChange<Integer>> needs = new LinkedHashMap<>();
        for (int index = 0; index < codesAndWeights.length; index += 2) {
            Integer weight = (Integer) codesAndWeights[index + 1];
            needs.put((String) codesAndWeights[index], weight == null ? ValueChange.clear() : ValueChange.replace(weight));
        }
        return needs;
    }
}
