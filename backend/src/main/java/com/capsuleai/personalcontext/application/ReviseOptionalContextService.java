package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.OptionalContextRepository;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.domain.BodyShape;
import com.capsuleai.personalcontext.domain.GenderContext;
import com.capsuleai.personalcontext.domain.OptionalContext;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * US-PROF-002 — accepts one submitted change of the owner's optional gender and body-shape context
 * (FR-PROF-005–007, BRULE-PROF-001; S4-PROF-01/02/03, OPEN-S4-16/17).
 *
 * <p>Presence-aware: an omitted field keeps its value, null clears it (absent), a supported code sets it. The
 * change is accepted only against the record the user read: {@code expectedRevision} null for the first
 * declaration (guarded: one wins), the current revision afterwards, both enforced atomically in the database.
 * A change that alters nothing writes nothing; in particular an empty first operation creates no record. The
 * preferences of US-PROF-001 are never read or written here.
 */
public final class ReviseOptionalContextService {

    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String GENDER_CONTEXT_FIELD = "genderContext";
    static final String BODY_SHAPE_FIELD = "bodyShape";

    private final OptionalContextRepository optionalContextRepository;
    private final PersonalContextTransactions personalContextTransactions;
    private final Clock clock;

    public ReviseOptionalContextService(OptionalContextRepository optionalContextRepository,
                                        PersonalContextTransactions personalContextTransactions, Clock clock) {
        this.optionalContextRepository = Objects.requireNonNull(optionalContextRepository, "optionalContextRepository");
        this.personalContextTransactions =
                Objects.requireNonNull(personalContextTransactions, "personalContextTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws OptionalContextValidationException       if the request or a value is not acceptable (nothing changed)
     * @throws OptionalContextRevisionConflictException if the record changed after the user read it
     */
    public OptionalContextRevision revise(ReviseOptionalContextCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        OptionalContextChanges changes = Objects.requireNonNull(command.changes(), "changes");
        checkShape(command.expectedRevision(), changes);

        Optional<OptionalContextProfile> current = optionalContextRepository.find(ownerAccountId);
        if (!preconditionHolds(command.expectedRevision(), current)) {
            throw new OptionalContextRevisionConflictException();
        }
        OptionalContext base = current.map(OptionalContextProfile::values).orElse(OptionalContext.none());
        OptionalContext resulting = apply(base, changes);
        if (resulting.equals(base)) {
            return new OptionalContextRevision(current, false);
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (current.isEmpty()) {
            OptionalContextProfile created = OptionalContextProfile.firstDeclaration(ownerAccountId, resulting, now);
            if (!personalContextTransactions.inTransaction(() -> optionalContextRepository.createIfAbsent(created))) {
                throw new OptionalContextRevisionConflictException();
            }
            return new OptionalContextRevision(Optional.of(created), true);
        }
        OptionalContextProfile existing = current.get();
        OptionalContextProfile revised = existing.revisedTo(resulting, now);
        if (!personalContextTransactions.inTransaction(() -> optionalContextRepository.reviseIfCurrentAt(
                ownerAccountId, existing.revision(), resulting, revised.updatedAt()))) {
            throw new OptionalContextRevisionConflictException();
        }
        return new OptionalContextRevision(Optional.of(revised), true);
    }

    /** Problems visible without reading anything. */
    private static void checkShape(ExpectedRevision expectedRevision, OptionalContextChanges changes) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        if (expectedRevision == null || expectedRevision instanceof ExpectedRevision.Missing) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.REQUIRED));
        } else if (expectedRevision instanceof ExpectedRevision.Malformed) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        }
        if (changes.genderContext() instanceof ValueChange.Malformed<?>) {
            violations.add(new PreferenceFieldViolation(GENDER_CONTEXT_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (changes.bodyShape() instanceof ValueChange.Malformed<?>) {
            violations.add(new PreferenceFieldViolation(BODY_SHAPE_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new OptionalContextValidationException(violations);
        }
    }

    private static boolean preconditionHolds(ExpectedRevision expected, Optional<OptionalContextProfile> current) {
        if (expected instanceof ExpectedRevision.Unset) {
            return current.isEmpty();
        }
        if (expected instanceof ExpectedRevision.At at) {
            return current.isPresent() && current.get().revision() == at.revision();
        }
        throw new IllegalStateException("A missing or malformed precondition was already rejected");
    }

    private static OptionalContext apply(OptionalContext base, OptionalContextChanges changes) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        Optional<GenderContext> genderContext = resolve(base.genderContext(), changes.genderContext(),
                GenderContext::fromCode, GENDER_CONTEXT_FIELD, violations);
        Optional<BodyShape> bodyShape = resolve(base.bodyShape(), changes.bodyShape(),
                BodyShape::fromCode, BODY_SHAPE_FIELD, violations);
        if (!violations.isEmpty()) {
            throw new OptionalContextValidationException(violations);
        }
        return new OptionalContext(genderContext, bodyShape);
    }

    private static <V> Optional<V> resolve(Optional<V> current, ValueChange<String> change,
                                           Function<String, Optional<V>> fromCode, String field,
                                           List<PreferenceFieldViolation> violations) {
        if (change instanceof ValueChange.Clear<?>) {
            return Optional.empty();
        }
        if (change instanceof ValueChange.Replace<String> replace) {
            Optional<V> value = fromCode.apply(replace.value());
            if (value.isEmpty()) {
                violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
            return value;
        }
        return current;
    }
}