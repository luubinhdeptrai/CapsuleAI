package com.capsuleai.personalcontext.application;

import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import com.capsuleai.personalcontext.support.ControllableWeatherProvider;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-CTX-001 (S4-CTX-01/02/04; OPEN-S4-18/19/21): commit first, acquire after; reuse, refresh and staleness;
 * clearing; conflicts; validation; no-op.
 */
class ReviseLocationServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");

    private InMemoryLocationRepository locationRepository;
    private ControllableWeatherProvider weatherProvider;
    private MutableClock clock;
    private ReviseLocationService reviseLocationService;
    private ViewLocationContextService viewLocationContextService;

    @BeforeEach
    void setUp() {
        locationRepository = new InMemoryLocationRepository();
        weatherProvider = new ControllableWeatherProvider();
        clock = new MutableClock(NOW);
        ImmediatePersonalContextTransactions transactions = new ImmediatePersonalContextTransactions();
        EnvironmentalContextService environmentalContextService =
                new EnvironmentalContextService(locationRepository, weatherProvider, transactions, clock);
        reviseLocationService = new ReviseLocationService(locationRepository, environmentalContextService,
                transactions, clock);
        viewLocationContextService = new ViewLocationContextService(locationRepository, environmentalContextService);
    }

    @Test
    void aFirstSelectionIsRoundedStoredAndItsWeatherAcquiredAfterCommit() {
        weatherProvider.respondWithReadingObservedAt(NOW.minus(Duration.ofMinutes(10)));

        LocationContextView view = select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        LocationSelection selection = view.profile().orElseThrow().selection().orElseThrow();
        assertThat(view.profile().orElseThrow().revision()).isEqualTo(1);
        assertThat(selection.latitude()).isEqualTo(new BigDecimal("10.78"));
        assertThat(selection.longitude()).isEqualTo(new BigDecimal("106.70"));
        assertThat(view.environment()).isInstanceOf(EnvironmentalContext.Current.class);
        assertThat(weatherProvider.queries())
                .containsExactly(new WeatherQuery(new BigDecimal("10.78"), new BigDecimal("106.70")));
        assertThat(locationRepository.findEvidence(OWNER)).isPresent();
    }

    @Test
    void aProviderFailureKeepsTheAcceptedSelectionAndReportsUnavailable() {
        LocationContextView view = select(new ExpectedRevision.Unset(), manual("21.0285", "105.8048", "Hà Nội"));

        assertThat(view.profile().orElseThrow().selection()).isPresent();
        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
    }

    @Test
    void clearingRemovesTheSelectionAndItsEvidenceWithoutAProviderCall() {
        weatherProvider.respondWithReadingObservedAt(NOW);
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        LocationContextView cleared = reviseLocationService.revise(new ReviseLocationCommand(OWNER,
                new ExpectedRevision.At(1), ValueChange.clear()));

        assertThat(cleared.profile().orElseThrow().selection()).isEmpty();
        assertThat(cleared.profile().orElseThrow().revision()).isEqualTo(2);
        assertThat(cleared.environment()).isInstanceOf(EnvironmentalContext.NoSelection.class);
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();
        assertThat(weatherProvider.queries()).hasSize(1);
    }

    @Test
    void currentEvidenceIsReusedUpToExactlyThirtyMinutesAndRefreshedAfter() {
        weatherProvider.respondWithReadingObservedAt(NOW);
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        clock.advance(Duration.ofMinutes(30));
        assertThat(viewLocationContextService.view(OWNER).environment()).isInstanceOf(EnvironmentalContext.Current.class);
        assertThat(weatherProvider.queries()).as("exactly 30 minutes old is current: no refresh").hasSize(1);

        clock.advance(Duration.ofSeconds(1));
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        EnvironmentalContext refreshed = viewLocationContextService.view(OWNER).environment();

        assertThat(weatherProvider.queries()).hasSize(2);
        assertThat(((EnvironmentalContext.Current) refreshed).reading().observedAt()).isEqualTo(clock.instant());
    }

    @Test
    void aNewlyFetchedButStaleReadingIsNeverCurrentAndIsNotStored() {
        weatherProvider.respondWithReadingObservedAt(NOW.minus(Duration.ofMinutes(31)));

        LocationContextView view = select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.STALE_PROVIDER_DATA));
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();
    }

    @Test
    void aStaleRevisionAndASecondFirstSelectionAreConflicts() {
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(), manual("21.0285", "105.8048", "Hà Nội")))
                .isInstanceOf(LocationRevisionConflictException.class);
        assertThatThrownBy(() -> select(new ExpectedRevision.At(3), manual("21.0285", "105.8048", "Hà Nội")))
                .isInstanceOf(LocationRevisionConflictException.class);
    }

    @Test
    void invalidSelectionsAreRejectedWithEveryProblem() {
        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(),
                new SubmittedLocation(new BigDecimal("91"), null, null, "SATELLITE", List.of())))
                .isInstanceOfSatisfying(LocationValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("selection.latitude", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("selection.longitude", PreferenceValueProblem.REQUIRED),
                                new PreferenceFieldViolation("selection.source", PreferenceValueProblem.UNSUPPORTED_VALUE)));
        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", null)))
                .isInstanceOfSatisfying(LocationValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("selection.label", PreferenceValueProblem.REQUIRED)));
        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(),
                new SubmittedLocation(null, null, null, null, List.of("selection.latitude"))))
                .isInstanceOfSatisfying(LocationValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("selection.latitude", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("selection.longitude", PreferenceValueProblem.REQUIRED),
                                new PreferenceFieldViolation("selection.source", PreferenceValueProblem.REQUIRED)));
        assertThat(locationRepository.find(OWNER)).isEmpty();
    }

    @Test
    void theSameSelectionAfterRoundingWritesNothing() {
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        LocationContextView unchanged = select(new ExpectedRevision.At(1), manual("10.7801", "106.6950", "Hồ Chí Minh"));

        assertThat(unchanged.profile().orElseThrow().revision()).isEqualTo(1);
        assertThat(locationRepository.selectionWrites()).isOne();
    }

    /** A provider defect that throws despite the port contract is contained: the selection stays, nothing is invented. */
    @Test
    void aProviderThatThrowsIsReportedAsUnavailableAndKeepsTheSelection() {
        ImmediatePersonalContextTransactions transactions = new ImmediatePersonalContextTransactions();
        EnvironmentalContextService failing = new EnvironmentalContextService(locationRepository, query -> {
            throw new IllegalStateException("adapter defect");
        }, transactions, clock);
        ReviseLocationService revise = new ReviseLocationService(locationRepository, failing, transactions, clock);

        LocationContextView view = revise.revise(new ReviseLocationCommand(OWNER, new ExpectedRevision.Unset(),
                ValueChange.replace(manual("21.0285", "105.8048", "Hà Nội"))));

        assertThat(view.profile().orElseThrow().selection()).isPresent();
        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();
    }

    /** OPEN-S4-21: up to 5 minutes in the future is clock skew (current); further is invalid and never stored. */
    @Test
    void evidenceFarInTheFutureIsInvalidWhileSmallClockSkewIsTolerated() {
        weatherProvider.respondWithReadingObservedAt(NOW.plus(Duration.ofMinutes(6)));

        LocationContextView view = select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.INVALID_PROVIDER_DATA));
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();

        weatherProvider.respondWithReadingObservedAt(NOW.plus(Duration.ofMinutes(5)));
        assertThat(viewLocationContextService.view(OWNER).environment()).isInstanceOf(EnvironmentalContext.Current.class);
    }

    /** Evidence older than 30 minutes whose refresh fails is reported UNAVAILABLE, never presented as current. */
    @Test
    void staleStoredEvidenceWhoseRefreshFailsIsNeverPresentedAsCurrent() {
        weatherProvider.respondWithReadingObservedAt(NOW);
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));
        clock.advance(Duration.ofMinutes(31));
        weatherProvider.respondWith(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED));

        EnvironmentalContext environment = viewLocationContextService.view(OWNER).environment();

        assertThat(environment).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.DEADLINE_EXCEEDED));
        assertThat(weatherProvider.queries()).hasSize(2);
    }

    private LocationContextView select(ExpectedRevision expectedRevision, SubmittedLocation submitted) {
        return reviseLocationService.revise(new ReviseLocationCommand(OWNER, expectedRevision,
                ValueChange.replace(submitted)));
    }

    private static SubmittedLocation manual(String latitude, String longitude, String label) {
        return new SubmittedLocation(new BigDecimal(latitude), new BigDecimal(longitude), label, "MANUAL", List.of());
    }
}
