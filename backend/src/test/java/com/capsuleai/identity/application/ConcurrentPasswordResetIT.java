package com.capsuleai.identity.application;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-AUTH-005 races on real PostgreSQL row locks. Each test starts its competing
 * actions together and then checks the committed outcome, which must be the same
 * whichever action the database lets go first.
 */
class ConcurrentPasswordResetIT extends IdentityIntegrationTestSupport {

    @Autowired
    private CompletePasswordResetService completePasswordResetService;

    @Autowired
    private RequestPasswordResetService requestPasswordResetService;

    @Autowired
    private CheckPasswordResetService checkPasswordResetService;

    @Autowired
    private RefreshSessionService refreshSessionService;

    @Test
    void competingCompletionsOfOneLinkChangeThePasswordExactlyOnce() throws Exception {
        registerAccount("race@example.com", PASSWORD);
        String resetToken = requestPasswordResetToken("race@example.com");
        List<String> candidatePasswords = List.of(
                "first candidate passphrase", "second candidate passphrase",
                "third candidate passphrase", "fourth candidate passphrase");

        List<Object> outcomes = runConcurrently(candidatePasswords.stream()
                .<Callable<Object>>map(candidate -> () -> {
                    completePasswordResetService.complete(new CompletePasswordResetCommand(resetToken, candidate, "198.51.100.7"));
                    return candidate;
                })
                .toList());

        List<Object> successes = outcomes.stream().filter(String.class::isInstance).toList();
        assertThat(successes).hasSize(1);
        assertThat(outcomes).filteredOn(PasswordResetRejectedException.class::isInstance).hasSize(3);
        logIn("race@example.com", (String) successes.get(0));
        for (String candidate : candidatePasswords) {
            if (!candidate.equals(successes.get(0))) {
                postJson(LOGIN, Map.of("email", "race@example.com", "password", candidate))
                        .andExpect(status().isUnauthorized());
            }
        }
        assertThat(jdbc.sql("SELECT count(*) FROM identity.identity_password_reset WHERE consumed_at IS NOT NULL")
                .query(Long.class).single()).isOne();
    }

    @Test
    void resetRacingARenewalLeavesNoUsableSessionWhicheverCommitsFirst() throws Exception {
        IssuedTokens login = registerAndLogIn("renewal-race@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        String resetToken = requestPasswordResetToken("renewal-race@example.com");

        List<Object> outcomes = runConcurrently(List.<Callable<Object>>of(
                () -> {
                    completePasswordResetService.complete(
                            new CompletePasswordResetCommand(resetToken, "the passphrase after reset", "198.51.100.7"));
                    return "reset completed";
                },
                () -> refreshSessionService.renew(new RenewSessionCommand(login.refreshToken()))));

        assertThat(outcomes.get(0)).isEqualTo("reset completed");
        assertThat(jdbc.sql("SELECT revocation_reason FROM identity.identity_session WHERE id = :id")
                .param("id", sessionId).query(String.class).single()).isEqualTo("PASSWORD_RESET");
        if (outcomes.get(1) instanceof SessionTokens renewed) {
            // Renewal committed first; the reset then revoked the session the new tokens belong to.
            getProtectedProbe(renewed.accessToken()).andExpect(status().isUnauthorized());
            assertThatThrownBy(() -> refreshSessionService.renew(new RenewSessionCommand(renewed.refreshToken())))
                    .isInstanceOf(AuthenticationRequiredException.class);
        } else {
            assertThat(outcomes.get(1)).isInstanceOf(AuthenticationRequiredException.class);
        }
        getProtectedProbe(login.accessToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void competingRequestsLeaveExactlyOneUsableLink() throws Exception {
        registerAccount("issuance-race@example.com", PASSWORD);
        Callable<Object> request = () -> {
            requestPasswordResetService.request(new RequestPasswordResetCommand("issuance-race@example.com", "198.51.100.7"));
            return "requested";
        };

        List<Object> outcomes = runConcurrently(List.of(request, request, request, request));

        assertThat(outcomes).containsOnly("requested");
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM identity.identity_password_reset
                        WHERE consumed_at IS NULL AND replaced_at IS NULL
                        """)
                .query(Long.class).single()).isOne();
        List<String> sentTokens = passwordResetInstructions.sent().stream()
                .map(sent -> sent.resetToken())
                .toList();
        assertThat(sentTokens).hasSize(4);
        assertThat(sentTokens).filteredOn(this::isUsable).hasSize(1);
    }

    private boolean isUsable(String resetToken) {
        try {
            checkPasswordResetService.check(new CheckPasswordResetCommand(resetToken, "198.51.100.7"));
            return true;
        } catch (PasswordResetRejectedException rejected) {
            return false;
        }
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
                outcomes.add(future.get(60, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
        }
    }
}
