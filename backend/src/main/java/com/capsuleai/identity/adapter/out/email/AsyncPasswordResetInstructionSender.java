package com.capsuleai.identity.adapter.out.email;

import com.capsuleai.identity.application.ports.PasswordResetInstructionSender;
import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hands instructions to the delivery adapter on a background thread, so the
 * Forgot Password response never waits for the mail server and takes about as
 * long for registered as for unregistered emails (FR-AUTH-003, NFR-SEC-003).
 *
 * <p>The queue is bounded. When it is full, or the application is stopping, the
 * instructions are not sent and a warning is logged; the issued interaction stays
 * valid and the user can simply request again (AC-13).
 */
public final class AsyncPasswordResetInstructionSender implements PasswordResetInstructionSender, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(AsyncPasswordResetInstructionSender.class);

    private final PasswordResetInstructionSender delivery;
    private final ExecutorService executor;

    public AsyncPasswordResetInstructionSender(PasswordResetInstructionSender delivery, ExecutorService executor) {
        this.delivery = Objects.requireNonNull(delivery, "delivery");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    /** One delivery thread and at most {@code queueCapacity} waiting deliveries. */
    public static AsyncPasswordResetInstructionSender withBoundedQueue(
            PasswordResetInstructionSender delivery, int queueCapacity) {
        ExecutorService singleDeliveryThread = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                Thread.ofPlatform().name("password-reset-mail-", 0).daemon(true).factory());
        return new AsyncPasswordResetInstructionSender(delivery, singleDeliveryThread);
    }

    @Override
    public void sendInstructions(PasswordResetInstructions instructions) {
        try {
            executor.execute(() -> deliver(instructions));
        } catch (RejectedExecutionException deliveryQueueUnavailable) {
            log.warn("Password-reset instructions were not queued for delivery (queue full or shutting down)");
        }
    }

    private void deliver(PasswordResetInstructions instructions) {
        try {
            delivery.sendInstructions(instructions);
        } catch (RuntimeException unexpectedFailure) {
            log.warn("Password-reset instruction delivery failed ({})", unexpectedFailure.getClass().getSimpleName());
        }
    }

    /** Lets queued deliveries finish for a short while when the application stops. */
    @Override
    public void close() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}