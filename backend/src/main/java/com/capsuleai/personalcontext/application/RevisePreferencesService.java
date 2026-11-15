package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * US-PROF-001 — accepts one submitted change of the owner's styles and common needs (FR-PROF-002/003,
 * BRULE-PROF-002; S3-PROF-02/03/04).
 *
 * <p>Presence-aware: untouched styles and needs keep their values; a cleared need becomes unset; an
 * explicit 0 stays Not Relevant. The change is accepted only against the declaration the user read:
 * {@code expectedRevision} null for the first declaration (guarded: one wins), the current revision
 * afterwards, both enforced atomically in the database. A stale change is refused, never merged.
 */
public final class RevisePreferencesService {

    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String STYLES_FIELD = "styles";
    static final String COMMON_NEEDS_FIELD = "commonNeeds";

    private final PreferenceProfileRepository profiles;
    private final PersonalContextTransactions transactions;
    private final Clock clock;

    public RevisePreferencesService(PreferenceProfileRepository profiles, PersonalContextTransactions transactions,
                                    Clock clock) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws PreferencesValidationException       if the request or a value is not acceptable (nothing changed)
     * @throws PreferencesRevisionConflictException if the declaration changed after the user read it
     */
    public PreferencesRevision revise(RevisePreferencesCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        PreferenceChanges changes = Objects.requireNonNull(command.changes(), "changes");
        checkShape(command.expectedRevision(), changes);

        Optional<PreferenceProfile> current = profiles.find(owner);
        if (!preconditionHolds(command.expectedRevision(), current)) {
            throw new PreferencesRevisionConflictException();
        }
        DeclaredPreferences base = current.map(PreferenceProfile::preferences).orElse(DeclaredPreferences.none());
        DeclaredPreferences resulting = apply(base, changes);
        if (resulting.equals(base)) {
            return new PreferencesRevision(current, false);
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (current.isEmpty()) {
            PreferenceProfile created = PreferenceProfile.firstDeclaration(owner, resulting, now);
            if (!transactions.inTransaction(() -> profiles.createIfAbsent(created))) {
                throw new PreferencesRevisionConflictException();
            }
            return new PreferencesRevision(Optional.of(created), true);
        }
        PreferenceProfile existing = current.get();
        PreferenceProfile revised = existing.revisedTo(resulting, now);
        if (!transactions.inTransaction(() -> profiles.reviseIfCurrentAt(owner, existing.revision(), resulting,
                revised.updatedAt()))) {
            throw new PreferencesRevisionConflictException();
        }
        return new PreferencesRevision(Optional.of(revised), true);
    }

    /** Problems visible without reading anything. */
    private static void checkShape(ExpectedRevision expectedRevision, PreferenceChanges changes) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        if (expectedRevision == null || expectedRevision instanceof ExpectedRevision.Missing) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.REQUIRED));
        } else if (expectedRevision instanceof ExpectedRevision.Malformed) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        }
        if (changes.styles() instanceof ValueChange.Malformed<?>) {
            violations.add(new PreferenceFieldViolation(STYLES_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (changes.commonNeedsMalformed()) {
            violations.add(new PreferenceFieldViolation(COMMON_NEEDS_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new PreferencesValidationException(violations);
        }
    }

    private static boolean preconditionHolds(ExpectedRevision expected, Optional<PreferenceProfile> current) {
        if (expected instanceof ExpectedRevision.Unset) {
            return current.isEmpty();
        }
        if (expected instanceof ExpectedRevision.At at) {
            return current.isPresent() && current.get().revision() == at.revision();
        }
        throw new IllegalStateException("A missing or malformed precondition was already rejected");
    }

    private static DeclaredPreferences apply(DeclaredPreferences base, PreferenceChanges changes) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        Set<ProfileStyle> styles = styles(base.styles(), changes.styles(), violations);
        Map<CommonNeed, NeedPriority> needs = new EnumMap<>(CommonNeed.class);
        needs.putAll(base.commonNeeds());
        boolean unknownNeed = false;
        for (Map.Entry<String, ValueChange<Integer>> entry : changes.commonNeeds().entrySet()) {
            Optional<CommonNeed> need = CommonNeed.fromCode(entry.getKey());
            if (need.isEmpty()) {
                unknownNeed = true;
                continue;
            }
            String field = COMMON_NEEDS_FIELD + "." + need.get().name();
            ValueChange<Integer> change = entry.getValue();
            if (change instanceof ValueChange.Clear<?>) {
                needs.remove(need.get());
            } else if (change instanceof ValueChange.Replace<Integer> replace) {
                Optional<NeedPriority> priority = NeedPriority.fromWeight(replace.value());
                if (priority.isPresent()) {
                    needs.put(need.get(), priority.get());
                } else {
                    violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
                }
            } else if (change instanceof ValueChange.Malformed<?>) {
                violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
        }
        if (unknownNeed) {
            violations.add(new PreferenceFieldViolation(COMMON_NEEDS_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new PreferencesValidationException(violations);
        }
        return new DeclaredPreferences(styles, needs);
    }

    private static Set<ProfileStyle> styles(Set<ProfileStyle> current, ValueChange<List<String>> change,
                                            List<PreferenceFieldViolation> violations) {
        if (change instanceof ValueChange.Clear<?>) {
            return Set.of();
        }
        if (!(change instanceof ValueChange.Replace<List<String>> replace)) {
            return current;
        }
        Set<ProfileStyle> styles = EnumSet.noneOf(ProfileStyle.class);
        List<String> codes = replace.value();
        for (int index = 0; index < codes.size(); index++) {
            Optional<ProfileStyle> style = ProfileStyle.fromCode(codes.get(index));
            if (style.isPresent()) {
                styles.add(style.get());
            } else {
                violations.add(new PreferenceFieldViolation(STYLES_FIELD + "[" + index + "]",
                        PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
        }
        return styles;
    }
}