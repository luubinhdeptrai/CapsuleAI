package com.capsuleai.identity.application;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OPEN-S2-05: measures successful Forgot Password requests for a registered and an
 * unregistered address through the real application, with a 150 ms minimum response time.
 *
 * <p>The assertion bounds are deliberately generous: this is evidence that the pacing hides
 * the extra issuance work under normal load, not a proof of constant time. The measured
 * medians are logged so a reviewer can compare runs.
 */
@TestPropertySource(properties = "capsuleai.identity.password-reset.protection.minimum-request-response-time=PT0.15S")
class PasswordResetTimingIT extends IdentityIntegrationTestSupport {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetTimingIT.class);
    private static final Duration MINIMUM = Duration.ofMillis(150);
    private static final int WARM_UP = 3;
    private static final int MEASURED = 12;

    @Test
    void registeredAndUnregisteredRequestsTakeComparableTime() throws Exception {
        registerAccount("timed@example.com", PASSWORD);
        List<Long> registered = new ArrayList<>();
        List<Long> unregistered = new ArrayList<>();

        for (int round = 0; round < WARM_UP + MEASURED; round++) {
            long registeredNanos = timedRequest("timed@example.com");
            long unregisteredNanos = timedRequest("missing" + round + "@example.com");
            if (round >= WARM_UP) {
                registered.add(registeredNanos);
                unregistered.add(unregisteredNanos);
            }
        }

        long registeredMedian = median(registered);
        long unregisteredMedian = median(unregistered);
        log.info("Forgot Password median response: registered {} ms, unregistered {} ms (minimum {} ms)",
                registeredMedian / 1_000_000, unregisteredMedian / 1_000_000, MINIMUM.toMillis());
        assertThat(registered).allSatisfy(nanos -> assertThat(nanos).isGreaterThanOrEqualTo(MINIMUM.toNanos()));
        assertThat(unregistered).allSatisfy(nanos -> assertThat(nanos).isGreaterThanOrEqualTo(MINIMUM.toNanos()));
        assertThat(Math.abs(registeredMedian - unregisteredMedian))
                .as("difference of the medians")
                .isLessThan(MINIMUM.toNanos() / 2);
    }

    private long timedRequest(String email) throws Exception {
        long started = System.nanoTime();
        postJson(PASSWORD_RESET_REQUEST, Map.of("email", email)).andExpect(status().isAccepted());
        return System.nanoTime() - started;
    }

    private static long median(List<Long> values) {
        List<Long> sorted = values.stream().sorted().toList();
        return sorted.get(sorted.size() / 2);
    }
}
