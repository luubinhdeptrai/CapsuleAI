package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-GAR-001 AC-12 on real PostgreSQL: duplicated deliveries of one logical
 * addition race on the unique (owner, creation key) constraint; separate
 * intentions never collapse.
 */
class ConcurrentGarmentCreationIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private AddGarmentService addGarmentService;

    @Test
    void concurrentDeliveriesOfOneAdditionCreateExactlyOneGarment() throws Exception {
        UUID owner = registerAccount("duplicate-delivery@example.com", PASSWORD);
        String key = UUID.randomUUID().toString();
        Callable<Object> delivery = () -> addGarmentService.add(command(owner, key));

        List<Object> outcomes = runConcurrently(List.of(delivery, delivery, delivery, delivery));

        assertThat(outcomes).allMatch(GarmentCreation.class::isInstance);
        List<GarmentCreation> creations = outcomes.stream().map(GarmentCreation.class::cast).toList();
        assertThat(creations).filteredOn(GarmentCreation::newlyCreated).hasSize(1);
        assertThat(creations).extracting(creation -> creation.garment().id()).containsOnly(
                creations.get(0).garment().id());
        assertThat(countGarmentsOf(owner)).isOne();
    }

    @Test
    void concurrentSeparateAdditionsOfTheSameGarmentAreAllKept() throws Exception {
        UUID owner = registerAccount("separate-additions@example.com", PASSWORD);
        List<Callable<Object>> separateAdditions = List.of(
                () -> addGarmentService.add(command(owner, UUID.randomUUID().toString())),
                () -> addGarmentService.add(command(owner, UUID.randomUUID().toString())),
                () -> addGarmentService.add(command(owner, UUID.randomUUID().toString())));

        List<Object> outcomes = runConcurrently(separateAdditions);

        assertThat(outcomes).allMatch(outcome -> outcome instanceof GarmentCreation creation && creation.newlyCreated());
        assertThat(countGarmentsOf(owner)).isEqualTo(3);
    }

    private static AddGarmentCommand command(UUID owner, String key) {
        return new AddGarmentCommand(owner, key, true, "TOPS", "T_SHIRTS", "WHITE", List.of("COTTON"), null, null,
                null, null, null, null, null, null, null, null);
    }

    /** Starts all actions together; returns each result or the RuntimeException it threw. */
    private static List<Object> runConcurrently(List<? extends Callable<?>> actions) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(actions.size());
        CountDownLatch ready = new CountDownLatch(actions.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<?> action : actions) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        return action.call();
                    } catch (RuntimeException failure) {
                        return failure;
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) {
                outcomes.add(future.get(30, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
        }
    }
}
