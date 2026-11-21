package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.List;
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
 * US-GAR-005 with real transactions on PostgreSQL READ COMMITTED (S4-GAR-01, S4-INT-01, OPEN-S4-06): of two
 * removals exactly one removes and the other reports the same removal; a removal and an edit of the same
 * reviewed revision have exactly one winner. No deadlock within the timeout.
 */
class ConcurrentGarmentRemovalIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private RemoveGarmentService removeGarmentService;

    @Autowired
    private ReviseGarmentService reviseGarmentService;

    @Test
    void ofTwoConcurrentRemovalsOneRemovesAndTheOtherReportsAlreadyRemoved() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-removal-twice@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(removal(start, tokens.accountId(), garmentId));
            Future<String> second = executor.submit(removal(start, tokens.accountId(), garmentId));

            assertThat(List.of(first.get(20, SECONDS), second.get(20, SECONDS)))
                    .containsExactlyInAnyOrder("removed", "already-removed");
        } finally {
            executor.shutdownNow();
        }
        assertThat(jdbc.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(Long.class).single()).isEqualTo(2L);
        assertThat(countRows("wardrobe.wardrobe_garment_removal_cleanup")).isOne();
    }

    @Test
    void ofARemovalAndAnEditOfTheSameRevisionExactlyOneWins() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-removal-edit@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> removal = executor.submit(removal(start, tokens.accountId(), garmentId));
            Future<String> edit = executor.submit(edit(start, tokens.accountId(), garmentId));
            List<String> outcomes = List.of(removal.get(20, SECONDS), edit.get(20, SECONDS));

            assertThat(outcomes)
                    .as("removal wins (the edit finds it gone) or the edit wins (the removal conflicts)")
                    .isIn(List.of("removed", "gone"), List.of("conflict", "edited"));
        } finally {
            executor.shutdownNow();
        }
        assertThat(jdbc.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(Long.class).single()).isEqualTo(2L);
    }

    private Callable<String> removal(CyclicBarrier start, UUID owner, UUID garmentId) {
        return () -> {
            start.await(10, SECONDS);
            try {
                GarmentRemoval removal = removeGarmentService.remove(
                        new RemoveGarmentCommand(owner, garmentId.toString(), true, "1"));
                return removal.newlyRemoved() ? "removed" : "already-removed";
            } catch (GarmentRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private Callable<String> edit(CyclicBarrier start, UUID owner, UUID garmentId) {
        GarmentChanges recolor = new GarmentChanges(FieldChange.keep(), FieldChange.keep(), FieldChange.replace("RED"),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep());
        return () -> {
            start.await(10, SECONDS);
            try {
                reviseGarmentService.revise(new ReviseGarmentCommand(owner, garmentId.toString(), true, "1", recolor));
                return "edited";
            } catch (GarmentNoLongerCurrentException gone) {
                return "gone";
            } catch (GarmentRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }
}
