package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-001 with real transactions (S3-PROF-04): one first declaration and one revision win; failures roll back. */
class ConcurrentPreferencesIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private RevisePreferencesService service;

    @Autowired
    private PreferenceProfileRepository profiles;

    @Autowired
    private PersonalContextTransactions transactions;

    @Test
    void ofTwoFirstDeclarationsExactlyOneIsAccepted() throws Exception {
        UUID owner = registerAndLogIn("race-first@example.com").accountId();

        List<String> outcomes = race(change(owner, new ExpectedRevision.Unset(), "MINIMAL", null),
                change(owner, new ExpectedRevision.Unset(), "FORMAL", null));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(jdbc.sql("SELECT count(*) FROM personal_context.personal_context_preferences")
                .query(Long.class).single()).isOne();
    }

    @Test
    void ofTwoRevisionsOfOneRevisionExactlyOneWinsAndTheStoredNeedsMatchTheWinner() throws Exception {
        UUID owner = registerAndLogIn("race-revise@example.com").accountId();
        service.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(),
                new PreferenceChanges(ValueChange.replace(List.of("CASUAL")), Map.of(), false)));

        List<String> outcomes = race(change(owner, new ExpectedRevision.At(1), null, 1),
                change(owner, new ExpectedRevision.At(1), null, 3));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(jdbc.sql("SELECT revision FROM personal_context.personal_context_preferences")
                .query(Long.class).single()).isEqualTo(2L);
        assertThat(jdbc.sql("SELECT count(*) FROM personal_context.personal_context_common_need WHERE need = 'TRAVEL'")
                .query(Long.class).single()).isOne();
    }

    @Test
    void aFailureAfterTheRevisionStatementRollsBackStylesAndNeeds() {
        UUID owner = registerAndLogInQuietly("race-rollback@example.com");
        service.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(), new PreferenceChanges(
                ValueChange.replace(List.of("CLASSIC")), Map.of("TRAVEL", ValueChange.replace(1)), false)));
        DeclaredPreferences replacement = new DeclaredPreferences(Set.of(ProfileStyle.SPORTY),
                Map.of(CommonNeed.SPORT_ACTIVE, NeedPriority.HIGH));

        assertThatThrownBy(() -> transactions.inTransaction(() -> {
            assertThat(profiles.reviseIfCurrentAt(owner, 1, replacement, Instant.now())).isTrue();
            throw new IllegalStateException("simulated failure after the sets were rewritten");
        })).isInstanceOf(IllegalStateException.class);

        DeclaredPreferences stored = profiles.find(owner).orElseThrow().preferences();
        assertThat(stored.styles()).containsExactly(ProfileStyle.CLASSIC);
        assertThat(stored.priorityOf(CommonNeed.TRAVEL)).contains(NeedPriority.LOW);
        assertThat(stored.priorityOf(CommonNeed.SPORT_ACTIVE)).isEmpty();
        assertThat(profiles.find(owner).orElseThrow().revision()).isEqualTo(1L);
    }

    private RevisePreferencesCommand change(UUID owner, ExpectedRevision expected, String style, Integer travel) {
        return new RevisePreferencesCommand(owner, expected, new PreferenceChanges(
                style == null ? ValueChange.keep() : ValueChange.replace(List.of(style)),
                travel == null ? Map.of() : Map.of("TRAVEL", ValueChange.replace(travel)), false));
    }

    private List<String> race(RevisePreferencesCommand first, RevisePreferencesCommand second) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var a = executor.submit(attempt(start, first));
            var b = executor.submit(attempt(start, second));
            return List.of(a.get(20, SECONDS), b.get(20, SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<String> attempt(CyclicBarrier start, RevisePreferencesCommand command) {
        return () -> {
            start.await(10, SECONDS);
            try {
                service.revise(command);
                return "accepted";
            } catch (PreferencesRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private UUID registerAndLogInQuietly(String email) {
        try {
            return registerAndLogIn(email).accountId();
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }
}
