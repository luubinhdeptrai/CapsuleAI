package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.LocationSource;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 — accepts one change of the owner's location selection (FR-WEATHER-001/002, BRULE-PROF-003;
 * S4-CTX-01/02, OPEN-S4-18/19).
 *
 * <p>The selection is validated and rounded to two decimals, then committed under the owner/revision guard
 * (guarded first selection; a change or clear deletes the old evidence in the same transaction). Only after the
 * commit is weather acquired, outside any transaction, so a provider failure never undoes the accepted
 * selection: the response reports the selection and an explicit UNAVAILABLE environment. Clearing and an
 * unchanged selection never call the provider for a new point. Preferences and optional context are untouched.
 */
public final class ReviseLocationService {

    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String SELECTION_FIELD = "selection";
    static final String LATITUDE_FIELD = SubmittedLocation.LATITUDE_FIELD;
    static final String LONGITUDE_FIELD = SubmittedLocation.LONGITUDE_FIELD;
    static final String LABEL_FIELD = SubmittedLocation.LABEL_FIELD;
    static final String SOURCE_FIELD = SubmittedLocation.SOURCE_FIELD;

    private final LocationRepository locationRepository;
    private final EnvironmentalContextService environmentalContextService;
    private final PersonalContextTransactions personalContextTransactions;
    private final Clock clock;

    public ReviseLocationService(LocationRepository locationRepository,
                                 EnvironmentalContextService environmentalContextService,
                                 PersonalContextTransactions personalContextTransactions, Clock clock) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
        this.environmentalContextService =
                Objects.requireNonNull(environmentalContextService, "environmentalContextService");
        this.personalContextTransactions =
                Objects.requireNonNull(personalContextTransactions, "personalContextTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws LocationValidationException       if the request or the selection is not acceptable (nothing changed)
     * @throws LocationRevisionConflictException if the location changed after the user read it (nothing changed)
     */
    public LocationContextView revise(ReviseLocationCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ValueChange<SubmittedLocation> change = Objects.requireNonNull(command.selection(), "selection");
        checkShape(command.expectedRevision(), change);

        Optional<LocationProfile> current = locationRepository.find(ownerAccountId);
        if (!preconditionHolds(command.expectedRevision(), current)) {
            throw new LocationRevisionConflictException();
        }
        Optional<LocationSelection> currentSelection = current.flatMap(LocationProfile::selection);
        Optional<LocationSelection> resulting = resulting(currentSelection, change);
        if (resulting.equals(currentSelection)) {
            return new LocationContextView(current, environmentalContextService.currentFor(ownerAccountId, current));
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        LocationProfile saved;
        if (current.isEmpty()) {
            LocationProfile created = LocationProfile.firstSelection(ownerAccountId, resulting.orElseThrow(), now);
            if (!personalContextTransactions.inTransaction(() -> locationRepository.createIfAbsent(created))) {
                throw new LocationRevisionConflictException();
            }
            saved = created;
        } else {
            LocationProfile existing = current.get();
            LocationProfile revised = existing.revisedTo(resulting, now);
            if (!personalContextTransactions.inTransaction(() -> locationRepository.reviseIfCurrentAt(
                    ownerAccountId, existing.revision(), resulting, revised.updatedAt()))) {
                throw new LocationRevisionConflictException();
            }
            saved = revised;
        }
        Optional<LocationProfile> savedProfile = Optional.of(saved);
        return new LocationContextView(savedProfile, environmentalContextService.currentFor(ownerAccountId, savedProfile));
    }

    private static void checkShape(ExpectedRevision expectedRevision, ValueChange<SubmittedLocation> change) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        if (expectedRevision == null || expectedRevision instanceof ExpectedRevision.Missing) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.REQUIRED));
        } else if (expectedRevision instanceof ExpectedRevision.Malformed) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        }
        if (change instanceof ValueChange.Malformed<?>) {
            violations.add(new PreferenceFieldViolation(SELECTION_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new LocationValidationException(violations);
        }
    }

    private static boolean preconditionHolds(ExpectedRevision expected, Optional<LocationProfile> current) {
        if (expected instanceof ExpectedRevision.Unset) {
            return current.isEmpty();
        }
        if (expected instanceof ExpectedRevision.At at) {
            return current.isPresent() && current.get().revision() == at.revision();
        }
        throw new IllegalStateException("A missing or malformed precondition was already rejected");
    }

    private static Optional<LocationSelection> resulting(Optional<LocationSelection> current,
                                                         ValueChange<SubmittedLocation> change) {
        if (change instanceof ValueChange.Clear<?>) {
            return Optional.empty();
        }
        if (change instanceof ValueChange.Replace<SubmittedLocation> replace) {
            return Optional.of(validated(replace.value()));
        }
        return current;
    }

    /** Validates every member at once and returns the rounded selection (OPEN-S4-18). */
    private static LocationSelection validated(SubmittedLocation submitted) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        BigDecimal latitude = coordinate(submitted.latitude(), LATITUDE_FIELD, submitted, violations, true);
        BigDecimal longitude = coordinate(submitted.longitude(), LONGITUDE_FIELD, submitted, violations, false);
        Optional<LocationSource> source = Optional.empty();
        if (submitted.wrongTypeFields().contains(SOURCE_FIELD)) {
            violations.add(new PreferenceFieldViolation(SOURCE_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        } else if (submitted.source() == null) {
            violations.add(new PreferenceFieldViolation(SOURCE_FIELD, PreferenceValueProblem.REQUIRED));
        } else {
            source = LocationSource.fromCode(submitted.source());
            if (source.isEmpty()) {
                violations.add(new PreferenceFieldViolation(SOURCE_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
        }
        Optional<String> label = LocationSelection.normalizeLabel(submitted.label());
        if (submitted.wrongTypeFields().contains(LABEL_FIELD)
                || (label.isPresent() && !LocationSelection.isAcceptableLabel(label.get()))) {
            violations.add(new PreferenceFieldViolation(LABEL_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        } else if (label.isEmpty() && source.equals(Optional.of(LocationSource.MANUAL))) {
            violations.add(new PreferenceFieldViolation(LABEL_FIELD, PreferenceValueProblem.REQUIRED));
        }
        if (!violations.isEmpty()) {
            throw new LocationValidationException(violations);
        }
        return new LocationSelection(latitude, longitude, label, source.orElseThrow());
    }

    private static BigDecimal coordinate(BigDecimal submitted, String field, SubmittedLocation location,
                                         List<PreferenceFieldViolation> violations, boolean isLatitude) {
        if (location.wrongTypeFields().contains(field)) {
            violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            return null;
        }
        if (submitted == null) {
            violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.REQUIRED));
            return null;
        }
        BigDecimal rounded = LocationSelection.round(submitted);
        boolean inRange = isLatitude ? LocationSelection.isLatitude(rounded) : LocationSelection.isLongitude(rounded);
        if (!inRange) {
            violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            return null;
        }
        return rounded;
    }
}
