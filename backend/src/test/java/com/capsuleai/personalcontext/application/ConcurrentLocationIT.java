package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.LocationRepository.WeatherPublication;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.LocationSource;
import com.capsuleai.personalcontext.support.ControllableWeatherProvider;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-CTX-001 on real PostgreSQL (S4-CTX-02/06, OPEN-S4-19): evidence is published only for the selection revision it
 * was acquired for; a change or clear during acquisition is reported, never overwritten; older evidence never replaces
 * newer evidence of the same selection.
 */
class ConcurrentLocationIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private ReviseLocationService reviseLocationService;

    @Autowired
    private ViewLocationContextService viewLocationContextService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PersonalContextTransactions personalContextTransactions;

    @Autowired
    private ReviseOptionalContextService reviseOptionalContextService;

    @Autowired
    private RevisePreferencesService revisePreferencesService;

    @Test
    void aLateReadingForAReplacedSelectionIsNeverPublished() throws Exception {
        UUID owner = registerAndLogIn("race-location-replaced@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        select(owner, new ExpectedRevision.At(1), "21.0285", "105.8048");

        WeatherPublication publication = publish(owner, 1, clock.instant());

        assertThat(publication).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
    }

    @Test
    void aLateReadingAfterClearingIsNeverPublished() throws Exception {
        UUID owner = registerAndLogIn("race-location-cleared@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        reviseLocationService.revise(new ReviseLocationCommand(owner, new ExpectedRevision.At(1), ValueChange.clear()));

        assertThat(publish(owner, 1, clock.instant())).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        assertThat(publish(owner, 2, clock.instant())).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
    }

    @Test
    void anOlderReadingNeverReplacesANewerOneOfTheSameSelection() throws Exception {
        UUID owner = registerAndLogIn("race-location-older@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        Instant newer = clock.instant();

        assertThat(publish(owner, 1, newer)).isEqualTo(WeatherPublication.PUBLISHED);
        assertThat(publish(owner, 1, newer.minus(Duration.ofMinutes(5)))).isEqualTo(WeatherPublication.KEPT_NEWER);
        assertThat(locationRepository.findEvidence(owner).orElseThrow().reading().observedAt()).isEqualTo(newer);
    }

    @Test
    void aSelectionChangedDuringAcquisitionIsReportedAndNotPublished() throws Exception {
        UUID owner = registerAndLogIn("race-location-during@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        weatherProvider.beforeNextAcquisition(() -> personalContextTransactions.inTransaction(() ->
                locationRepository.reviseIfCurrentAt(owner, 1, Optional.of(LocationSelection.rounded(
                        new BigDecimal("16.4637"), new BigDecimal("107.5909"), Optional.of("Huế"),
                        LocationSource.MANUAL)), clock.instant())));

        EnvironmentalContext environment = viewLocationContextService.view(owner).environment();

        assertThat(environment).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.SELECTION_CHANGED));
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(2);
    }

    /** Race C: two changes of the same revision; the conditional UPDATE lets exactly one win. */
    @Test
    void ofTwoChangesOfTheSameRevisionExactlyOneIsAccepted() throws Exception {
        UUID owner = registerAndLogIn("race-location-same-revision@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");

        List<String> outcomes = race(change(owner, new ExpectedRevision.At(1), "21.0285", "105.8048"),
                change(owner, new ExpectedRevision.At(1), "16.4637", "107.5909"));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(2);
    }

    /**
     * A publication holds the selection row in share mode until it commits: a concurrent change really waits for it
     * (seen in pg_stat_activity), then commits and deletes the evidence of the old selection in its own transaction.
     */
    @Test
    void aChangeWaitsForAPublicationHoldingTheSelectionAndThenDeletesItsEvidence() throws Exception {
        UUID owner = registerAndLogIn("race-location-publication-first@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        CountDownLatch published = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<WeatherPublication> publication = threads.submit(() -> personalContextTransactions.inTransaction(() -> {
                WeatherPublication outcome = locationRepository.publishEvidenceIfCurrent(owner, 1,
                        ControllableWeatherProvider.reading(clock.instant(), clock.instant()));
                published.countDown();
                awaitQuietly(release);
                return outcome;
            }));
            assertThat(published.await(10, SECONDS)).isTrue();
            Future<String> change = threads.submit(change(owner, new ExpectedRevision.At(1), "21.0285", "105.8048"));

            awaitABackendWaitingForALock();
            assertThat(change.isDone()).as("the change waits for the publication's share lock").isFalse();
            release.countDown();

            assertThat(publication.get(10, SECONDS)).isEqualTo(WeatherPublication.PUBLISHED);
            assertThat(change.get(10, SECONDS)).isEqualTo("accepted");
        } finally {
            release.countDown();
            threads.shutdownNow();
        }
        assertThat(locationRepository.findEvidence(owner)).as("the old selection's evidence went with the change")
                .isEmpty();
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(2);
    }

    /**
     * A publication that has to wait behind an uncommitted change re-reads the row once the change commits (READ
     * COMMITTED re-check under the lock), sees the new revision and publishes nothing.
     */
    @Test
    void aPublicationWaitingBehindAnUncommittedChangeSeesTheNewRevisionAndPublishesNothing() throws Exception {
        UUID owner = registerAndLogIn("race-location-change-first@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        CountDownLatch changed = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> change = threads.submit(() -> personalContextTransactions.inTransaction(() -> {
                boolean revised = locationRepository.reviseIfCurrentAt(owner, 1, Optional.of(LocationSelection.rounded(
                        new BigDecimal("16.4637"), new BigDecimal("107.5909"), Optional.of("Huế"), LocationSource.MANUAL)),
                        clock.instant());
                changed.countDown();
                awaitQuietly(release);
                return revised;
            }));
            assertThat(changed.await(10, SECONDS)).isTrue();
            Future<WeatherPublication> publication = threads.submit(() -> publish(owner, 1, clock.instant()));

            awaitABackendWaitingForALock();
            assertThat(publication.isDone()).as("the publication waits for the uncommitted change").isFalse();
            release.countDown();

            assertThat(change.get(10, SECONDS)).isTrue();
            assertThat(publication.get(10, SECONDS)).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        } finally {
            release.countDown();
            threads.shutdownNow();
        }
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
    }

    /** Race D: location, preferences and optional context are separate records; none blocks or overwrites another. */
    @Test
    void locationPreferenceAndOptionalContextChangesNeverConflict() throws Exception {
        UUID owner = registerAndLogIn("race-location-other-records@example.com").accountId();

        List<String> outcomes = race(change(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009"), () -> {
            reviseOptionalContextService.revise(new ReviseOptionalContextCommand(owner, new ExpectedRevision.Unset(),
                    new OptionalContextChanges(ValueChange.replace("WOMAN"), ValueChange.keep())));
            return "accepted";
        }, () -> {
            revisePreferencesService.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(),
                    new PreferenceChanges(ValueChange.replace(List.of("CLASSIC")), Map.of(), false)));
            return "accepted";
        });

        assertThat(outcomes).containsExactly("accepted", "accepted", "accepted");
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT gender_context || '/' || revision FROM personal_context.personal_context_optional_context "
                        + "WHERE owner_account_id = :owner")
                .param("owner", owner).query(String.class).single()).isEqualTo("WOMAN/1");
        assertThat(jdbc.sql("SELECT revision FROM personal_context.personal_context_preferences WHERE owner_account_id = :owner")
                .param("owner", owner).query(Long.class).single()).isEqualTo(1L);
    }

    private Callable<String> change(UUID owner, ExpectedRevision expectedRevision, String latitude, String longitude) {
        return () -> {
            try {
                select(owner, expectedRevision, latitude, longitude);
                return "accepted";
            } catch (LocationRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    @SafeVarargs
    private static List<String> race(Callable<String>... contenders) throws Exception {
        CyclicBarrier start = new CyclicBarrier(contenders.length);
        ExecutorService executor = Executors.newFixedThreadPool(contenders.length);
        try {
            List<Future<String>> outcomes = new ArrayList<>();
            for (Callable<String> contender : contenders) {
                outcomes.add(executor.submit(() -> {
                    start.await(10, SECONDS);
                    return contender.call();
                }));
            }
            List<String> results = new ArrayList<>();
            for (Future<String> outcome : outcomes) {
                results.add(outcome.get(20, SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    /** Polls PostgreSQL until another session of this database is blocked on a lock (at most 10 seconds). */
    private void awaitABackendWaitingForALock() throws InterruptedException {
        long giveUp = System.nanoTime() + SECONDS.toNanos(10);
        while (System.nanoTime() < giveUp) {
            long waiting = jdbc.sql("""
                            SELECT count(*) FROM pg_stat_activity
                            WHERE datname = current_database() AND wait_event_type = 'Lock'
                            """).query(Long.class).single();
            if (waiting > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("No session started waiting for the row lock within 10 seconds");
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(20, SECONDS)) {
                throw new IllegalStateException("The test never released the held transaction");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while holding the transaction", interrupted);
        }
    }

    private void select(UUID owner, ExpectedRevision expectedRevision, String latitude, String longitude) {
        reviseLocationService.revise(new ReviseLocationCommand(owner, expectedRevision, ValueChange.replace(
                new SubmittedLocation(new BigDecimal(latitude), new BigDecimal(longitude), "Test city", "MANUAL",
                        List.of()))));
    }

    private WeatherPublication publish(UUID owner, long locationRevision, Instant observedAt) {
        return personalContextTransactions.inTransaction(() -> locationRepository.publishEvidenceIfCurrent(owner,
                locationRevision, ControllableWeatherProvider.reading(observedAt, observedAt.plusSeconds(1))));
    }
}
