package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-002 with real transactions on PostgreSQL READ COMMITTED: one of two competing edits wins,
 * an edit cannot revive a concurrently removed garment, and a failure rolls back scalars and both
 * sets together (S3-GAR-02, S3-GAR-05).
 */
class ConcurrentGarmentRevisionIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private ReviseGarmentService service;

    @Autowired
    private WardrobeTransactions transactions;

    @Autowired
    private GarmentRepository garmentRepository;

    @Test
    void ofTwoEditsOfTheSameRevisionExactlyOneWinsAndTheSetsMatchTheWinner() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-two-edits@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE", "materials", List.of("COTTON")));
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> red = executor.submit(edit(start, tokens.accountId(), id, "RED", List.of("WOOL")));
            Future<String> blue = executor.submit(edit(start, tokens.accountId(), id, "BLUE",
                    List.of("LINEN", "SILK")));
            List<String> outcomes = List.of(red.get(20, SECONDS), blue.get(20, SECONDS));

            assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        } finally {
            executor.shutdownNow();
        }

        String color = jdbc.sql("SELECT color FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(String.class).single();
        List<String> materials = jdbc.sql("SELECT material FROM wardrobe.wardrobe_garment_material "
                        + "WHERE garment_id = :id ORDER BY material")
                .param("id", id).query(String.class).list();
        assertThat(jdbc.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(Long.class).single()).isEqualTo(2L);
        assertThat(color).isIn("RED", "BLUE");
        assertThat(materials)
                .as("the stored set belongs to the edit whose color won")
                .isEqualTo(color.equals("RED") ? List.of("WOOL") : List.of("LINEN", "SILK"));
    }

    @Test
    void anEditWaitingBehindAConcurrentRemovalIsRefusedAsNoLongerCurrent() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-removal@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        CountDownLatch removalHoldsTheRow = new CountDownLatch(1);
        CountDownLatch commitRemoval = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Object> removal = executor.submit(() -> transactions.inTransaction(() -> {
                jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at WHERE id = :id")
                        .param("id", id).update();
                removalHoldsTheRow.countDown();
                awaitQuietly(commitRemoval);
                return null;
            }));
            assertThat(removalHoldsTheRow.await(10, SECONDS)).isTrue();

            Future<GarmentRevision> edit = executor.submit(() -> service.revise(new ReviseGarmentCommand(
                    tokens.accountId(), id.toString(), true, "1", colorChange("BLACK"))));
            awaitAStatementWaitingForALock();
            commitRemoval.countDown();
            removal.get(10, SECONDS);

            assertThatThrownBy(() -> edit.get(10, SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(GarmentNoLongerCurrentException.class);
        } finally {
            commitRemoval.countDown();
            executor.shutdownNow();
        }
        assertThat(jdbc.sql("SELECT color FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(String.class).single()).isEqualTo("WHITE");
    }

    @Test
    void aFailureAfterTheRevisionStatementRollsBackScalarsAndBothSets() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-rollback@example.com");
        UUID id = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE",
                "materials", List.of("COTTON"), "styles", List.of("CASUAL")));
        GarmentProfile revised = new GarmentProfile(GarmentCategory.TOPS, Optional.empty(), GarmentColor.BLUE,
                Set.of(GarmentMaterial.WOOL), Optional.empty(), Set.of(GarmentStyle.CLASSIC), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty());

        assertThatThrownBy(() -> transactions.inTransaction(() -> {
            assertThat(garmentRepository.reviseIfCurrentAt(tokens.accountId(), id, 1, revised)).isTrue();
            throw new IllegalStateException("simulated failure after the sets were rewritten");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(jdbc.sql("SELECT color || ':' || revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", id).query(String.class).single()).isEqualTo("WHITE:1");
        assertThat(jdbc.sql("SELECT material FROM wardrobe.wardrobe_garment_material WHERE garment_id = :id")
                .param("id", id).query(String.class).list()).containsExactly("COTTON");
        assertThat(jdbc.sql("SELECT style FROM wardrobe.wardrobe_garment_style WHERE garment_id = :id")
                .param("id", id).query(String.class).list()).containsExactly("CASUAL");
    }

    private Callable<String> edit(CyclicBarrier start, UUID owner, UUID id, String color, List<String> materials) {
        GarmentChanges changes = new GarmentChanges(FieldChange.keep(), FieldChange.keep(),
                FieldChange.replace(color), FieldChange.replace(materials), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep());
        return () -> {
            start.await(10, SECONDS);
            try {
                service.revise(new ReviseGarmentCommand(owner, id.toString(), true, "1", changes));
                return "accepted";
            } catch (GarmentRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private static GarmentChanges colorChange(String color) {
        return new GarmentChanges(FieldChange.keep(), FieldChange.keep(), FieldChange.replace(color),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep());
    }

    /** Polls PostgreSQL until another session is blocked on a lock, so the race is deterministic. */
    private void awaitAStatementWaitingForALock() throws InterruptedException {
        long deadline = System.nanoTime() + SECONDS.toNanos(10);
        while (jdbc.sql("SELECT count(*) FROM pg_stat_activity WHERE wait_event_type = 'Lock' "
                        + "AND datname = current_database()")
                .query(Long.class).single() == 0) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("The edit never waited for the garment's row lock");
            }
            Thread.sleep(20);
        }
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(10, SECONDS)) {
                throw new IllegalStateException("The test never released the removal");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }
}
