package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConcurrentRefreshIT extends IdentityIntegrationTestSupport {

    @Autowired
    private RefreshSessionService refreshSessionService;

    @Autowired
    private LogOutService logOutService;

    @Test
    void competingRenewalsWithOneTokenAllowExactlyOneSuccessThenRevokeTheSession() throws Exception {
        IssuedTokens login = registerAndLogIn("race@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        Callable<Object> renewal = () -> refreshSessionService.renew(new RenewSessionCommand(login.refreshToken()));

        List<Object> outcomes = runConcurrently(List.of(renewal, renewal, renewal, renewal));

        List<SessionTokens> successes = outcomes.stream()
                .filter(SessionTokens.class::isInstance)
                .map(SessionTokens.class::cast)
                .toList();
        assertThat(successes).hasSize(1);
        assertThat(outcomes).filteredOn(AuthenticationRequiredException.class::isInstance).hasSize(3);
        assertThat(revocationReason(sessionId)).isEqualTo("REFRESH_TOKEN_REUSE");
        assertThatThrownBy(() -> refreshSessionService.renew(new RenewSessionCommand(successes.get(0).refreshToken())))
                .isInstanceOf(AuthenticationRequiredException.class);
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM identity_refresh_token
                        WHERE session_id = :id AND consumed_at IS NULL
                        """)
                .param("id", sessionId).query(Long.class).single()).isOne();
    }

    @Test
    void concurrentLogoutAndRenewalLeaveNoUsableAuthority() throws Exception {
        IssuedTokens login = registerAndLogIn("logout-race@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        AuthenticatedAccount current = new AuthenticatedAccount(login.accountId(), sessionId);

        List<Object> outcomes = runConcurrently(List.<Callable<Object>>of(
                () -> logOutService.logOut(current),
                () -> refreshSessionService.renew(new RenewSessionCommand(login.refreshToken()))));

        assertThat(outcomes.get(0)).isEqualTo(LogoutOutcome.REVOKED);
        assertThat(revocationReason(sessionId)).isEqualTo("LOGOUT");
        if (outcomes.get(1) instanceof SessionTokens renewed) {
            // Renewal committed first; logout then revoked the session the new tokens belong to.
            assertThatThrownBy(() -> refreshSessionService.renew(new RenewSessionCommand(renewed.refreshToken())))
                    .isInstanceOf(AuthenticationRequiredException.class);
            getProtectedProbe(renewed.accessToken()).andExpect(status().isUnauthorized());
        } else {
            assertThat(outcomes.get(1)).isInstanceOf(AuthenticationRequiredException.class);
        }
        getProtectedProbe(login.accessToken()).andExpect(status().isUnauthorized());
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

    private String revocationReason(UUID sessionId) {
        return jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId)
                .query(String.class)
                .single();
    }
}