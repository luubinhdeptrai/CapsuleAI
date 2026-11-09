package com.capsuleai.identity.adapter.out.email;

import com.capsuleai.identity.application.ports.PasswordResetInstructionSender;
import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import com.capsuleai.identity.domain.EmailAddress;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(OutputCaptureExtension.class)
class AsyncPasswordResetInstructionSenderTest {

    private static final PasswordResetInstructions INSTRUCTIONS = new PasswordResetInstructions(
            new EmailAddress("person@example.com"), "secret-reset-token", Instant.parse("2026-01-05T08:30:00Z"));

    @Test
    void theCallerReturnsBeforeDeliveryAndDeliveryStillHappens() throws Exception {
        CountDownLatch deliveryMayFinish = new CountDownLatch(1);
        CountDownLatch delivered = new CountDownLatch(1);
        List<PasswordResetInstructions> received = new CopyOnWriteArrayList<>();
        PasswordResetInstructionSender slowDelivery = instructions -> {
            awaitQuietly(deliveryMayFinish);
            received.add(instructions);
            delivered.countDown();
        };

        try (AsyncPasswordResetInstructionSender sender =
                     AsyncPasswordResetInstructionSender.withBoundedQueue(slowDelivery, 10)) {
            sender.sendInstructions(INSTRUCTIONS);
            assertThat(received).isEmpty();

            deliveryMayFinish.countDown();
            assertThat(delivered.await(5, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(received).containsExactly(INSTRUCTIONS);
    }

    @Test
    void aFailingDeliveryIsLoggedWithoutTheSecretAndNeverReachesTheCaller(CapturedOutput output) throws Exception {
        CountDownLatch attempted = new CountDownLatch(1);
        PasswordResetInstructionSender brokenDelivery = instructions -> {
            attempted.countDown();
            throw new IllegalStateException("provider rejected " + instructions.resetToken());
        };

        try (AsyncPasswordResetInstructionSender sender =
                     AsyncPasswordResetInstructionSender.withBoundedQueue(brokenDelivery, 10)) {
            assertThatCode(() -> sender.sendInstructions(INSTRUCTIONS)).doesNotThrowAnyException();
            assertThat(attempted.await(5, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(output.getAll())
                .contains("Password-reset instruction delivery failed (IllegalStateException)")
                .doesNotContain("secret-reset-token")
                .doesNotContain("person@example.com");
    }

    @Test
    void aFullQueueDropsTheDeliveryWithAWarningInsteadOfBlockingTheRequest(CapturedOutput output) throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch firstStarted = new CountDownLatch(1);
        PasswordResetInstructionSender blockedDelivery = instructions -> {
            firstStarted.countDown();
            awaitQuietly(release);
        };

        try (AsyncPasswordResetInstructionSender sender =
                     AsyncPasswordResetInstructionSender.withBoundedQueue(blockedDelivery, 1)) {
            sender.sendInstructions(INSTRUCTIONS);
            assertThat(firstStarted.await(5, TimeUnit.SECONDS)).isTrue();
            sender.sendInstructions(INSTRUCTIONS);

            assertThatCode(() -> sender.sendInstructions(INSTRUCTIONS)).doesNotThrowAnyException();
            release.countDown();
        }
        assertThat(output.getAll())
                .contains("Password-reset instructions were not queued for delivery (queue full or shutting down)");
    }

    @Test
    void afterShutdownNewDeliveriesAreRefusedQuietly() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AsyncPasswordResetInstructionSender sender =
                new AsyncPasswordResetInstructionSender(instructions -> { }, executor);
        sender.close();

        assertThatCode(() -> sender.sendInstructions(INSTRUCTIONS)).doesNotThrowAnyException();
        assertThat(executor.isShutdown()).isTrue();
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
