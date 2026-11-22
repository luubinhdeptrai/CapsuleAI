package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-PROF-002 with real transactions (S4-PROF-02/03): one first declaration and one revision of a revision win,
 * and optional-context and preference changes never block or overwrite each other.
 */
class ConcurrentOptionalContextIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private ReviseOptionalContextService reviseOptionalContextService;

    @Autowired
    private RevisePreferencesService revisePreferencesService;

    @Test
    void ofTwoFirstDeclarationsExactlyOneIsAccepted() throws Exception {
        UUID owner = registerAndLogIn("race-optional-first@example.com").accountId();

        List<String> outcomes = race(declare(owner, new ExpectedRevision.Unset(), "WOMAN"),
                declare(owner, new ExpectedRevision.Unset(), "MAN"));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(countRows("personal_context.personal_context_optional_context")).isOne();
    }

    @Test
    void ofTwoRevisionsOfTheSameRevisionExactlyOneIsAccepted() throws Exception {
        UUID owner = registerAndLogIn("race-optional-revision@example.com").accountId();
        declare(owner, new ExpectedRevision.Unset(), "WOMAN").call();

        List<String> outcomes = race(declare(owner, new ExpectedRevision.At(1), "MAN"),
                declare(owner, new ExpectedRevision.At(1), "NON_BINARY"));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(jdbc.sql("SELECT revision FROM personal_context.personal_context_optional_context "
                        + "WHERE owner_account_id = :owner")
                .param("owner", owner).query(Long.class).single()).isEqualTo(2L);
    }

    @Test
    void preferenceAndOptionalContextChangesNeverConflict() throws Exception {
        UUID owner = registerAndLogIn("race-optional-preferences@example.com").accountId();

        List<String> outcomes = race(declare(owner, new ExpectedRevision.Unset(), "ANOTHER_GENDER"), () -> {
            revisePreferencesService.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(),
                    new PreferenceChanges(ValueChange.replace(List.of("CLASSIC")), Map.of(), false)));
            return "accepted";
        });

        assertThat(outcomes).containsExactly("accepted", "accepted");
    }

    private Callable<String> declare(UUID owner, ExpectedRevision expectedRevision, String genderContext) {
        return () -> {
            try {
                reviseOptionalContextService.revise(new ReviseOptionalContextCommand(owner, expectedRevision,
                        new OptionalContextChanges(ValueChange.replace(genderContext), ValueChange.keep())));
                return "accepted";
            } catch (OptionalContextRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private static List<String> race(Callable<String> first, Callable<String> second) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> firstOutcome = executor.submit(() -> {
                start.await(10, SECONDS);
                return first.call();
            });
            Future<String> secondOutcome = executor.submit(() -> {
                start.await(10, SECONDS);
                return second.call();
            });
            return List.of(firstOutcome.get(20, SECONDS), secondOutcome.get(20, SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }
}
