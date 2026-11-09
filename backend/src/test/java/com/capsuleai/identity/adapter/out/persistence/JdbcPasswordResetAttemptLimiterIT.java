package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/** FR-AUTH-019: the counters are exact under concurrency and never store the subject itself. */
class JdbcPasswordResetAttemptLimiterIT extends IdentityIntegrationTestSupport {

    private static final Instant WINDOW = Instant.parse("2026-01-05T08:00:00Z");

    @Autowired
    private PasswordResetAttemptLimiter limiter;

    @Test
    void concurrentAttemptsEachReceiveADistinctCount() throws Exception {
        int threads = 16;
        CyclicBarrier start = new CyclicBarrier(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Integer>> counts = new ArrayList<>();
            for (int thread = 0; thread < threads; thread++) {
                counts.add(executor.submit(() -> {
                    start.await();
                    return limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "race@example.com", WINDOW);
                }));
            }
            List<Integer> observed = new ArrayList<>();
            for (Future<Integer> count : counts) {
                observed.add(count.get());
            }

            assertThat(observed).containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, threads).boxed().toList());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void scopesSubjectsAndWindowsAreCountedSeparately() {
        limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com", WINDOW);

        assertThat(limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com", WINDOW)).isEqualTo(2);
        assertThat(limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "other@example.com", WINDOW)).isOne();
        assertThat(limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "person@example.com", WINDOW)).isOne();
        assertThat(limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "person@example.com",
                WINDOW.plusSeconds(900))).isOne();
    }

    @Test
    void theSubjectIsStoredOnlyAsItsHash() {
        limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_EMAIL, "private.person@example.com", WINDOW);

        List<String> stored = jdbc.sql("SELECT subject_hash FROM identity.identity_password_reset_attempt")
                .query(String.class).list();
        assertThat(stored).containsExactly(JdbcPasswordResetAttemptLimiter.sha256("private.person@example.com"));
        assertThat(stored.get(0)).doesNotContain("private.person");
    }

    @Test
    void forgettingRemovesOnlyWindowsThatStartedBeforeTheCutoff() {
        limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "198.51.100.7", WINDOW);
        limiter.recordAttempt(PasswordResetAttemptScope.REQUEST_PER_CLIENT, "198.51.100.7", WINDOW.plusSeconds(900));

        assertThat(limiter.forgetWindowsStartedBefore(WINDOW.plusSeconds(900))).isOne();
        assertThat(countRows("identity.identity_password_reset_attempt")).isOne();
    }
}
