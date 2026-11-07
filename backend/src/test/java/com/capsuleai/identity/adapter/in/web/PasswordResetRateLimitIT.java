package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FR-AUTH-019 / OPEN-S2-04 through the real filter chain, controller and PostgreSQL counters,
 * with small limits in a context of its own.
 */
@TestPropertySource(properties = {
        "capsuleai.identity.password-reset.protection.request-per-email-max-attempts=2",
        "capsuleai.identity.password-reset.protection.request-per-client-max-attempts=4",
        "capsuleai.identity.password-reset.protection.verification-per-client-max-attempts=3",
        "capsuleai.identity.password-reset.protection.window=PT15M"
})
class PasswordResetRateLimitIT extends IdentityIntegrationTestSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void registeredAndUnregisteredAddressesGetTheSameLimitedResponse() throws Exception {
        registerAccount("known@example.com", PASSWORD);
        List<String> limitedBodies = new ArrayList<>();
        for (String email : new String[] {"known@example.com", "unknown@example.com"}) {
            String client = email.startsWith("known") ? "198.51.100.10" : "198.51.100.11";
            requestFrom(client, email).andExpect(status().isAccepted());
            requestFrom(client, email).andExpect(status().isAccepted());
            MvcResult limited = requestFrom(client, email)
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().string(HttpHeaders.RETRY_AFTER, "900"))
                    .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                    .andReturn();
            limitedBodies.add(withoutInstance(limited.getResponse().getContentAsString(StandardCharsets.UTF_8)));
        }

        assertThat(limitedBodies.get(0)).isEqualTo(limitedBodies.get(1));
        assertThat(passwordResetInstructions.sent()).as("instructions stop once limited").hasSize(2);
    }

    @Test
    void oneClientIsLimitedAcrossDifferentAddresses() throws Exception {
        for (int request = 1; request <= 4; request++) {
            requestFrom("203.0.113.20", "person" + request + "@example.com").andExpect(status().isAccepted());
        }

        requestFrom("203.0.113.20", "person5@example.com").andExpect(status().isTooManyRequests());
        requestFrom("203.0.113.21", "person5@example.com").andExpect(status().isAccepted());
    }

    @Test
    void checksAndCompletionsShareOneLimitPerClient() throws Exception {
        registerAccount("verifier@example.com", PASSWORD);
        String token = requestPasswordResetToken("verifier@example.com");

        postFrom("203.0.113.30", PASSWORD_RESET_CHECK, Map.of("token", token)).andExpect(status().isNoContent());
        postFrom("203.0.113.30", PASSWORD_RESET_CHECK, Map.of("token", "guessed-token"))
                .andExpect(status().isBadRequest());
        postFrom("203.0.113.30", PASSWORD_RESET_COMPLETE, Map.of("token", "guessed-token", "newPassword", "a brand new passphrase"))
                .andExpect(status().isBadRequest());

        postFrom("203.0.113.30", PASSWORD_RESET_COMPLETE, Map.of("token", token, "newPassword", "a brand new passphrase"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
        postFrom("203.0.113.31", PASSWORD_RESET_COMPLETE, Map.of("token", token, "newPassword", "a brand new passphrase"))
                .andExpect(status().isNoContent());
    }

    @Test
    void theNextWindowAcceptsRequestsAgain() throws Exception {
        requestFrom("203.0.113.40", "patient@example.com").andExpect(status().isAccepted());
        requestFrom("203.0.113.40", "patient@example.com").andExpect(status().isAccepted());
        requestFrom("203.0.113.40", "patient@example.com").andExpect(status().isTooManyRequests());

        clock.advance(Duration.ofMinutes(15));

        requestFrom("203.0.113.40", "patient@example.com").andExpect(status().isAccepted());
    }

    @Test
    void concurrentRequestsCannotExceedTheLimit() throws Exception {
        int threads = 8;
        CyclicBarrier start = new CyclicBarrier(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Integer>> statuses = new ArrayList<>();
            for (int thread = 0; thread < threads; thread++) {
                String client = "198.51.100." + (100 + thread);
                statuses.add(executor.submit(() -> {
                    start.await();
                    return requestFrom(client, "burst@example.com").andReturn().getResponse().getStatus();
                }));
            }
            List<Integer> observed = new ArrayList<>();
            for (Future<Integer> status : statuses) {
                observed.add(status.get());
            }

            assertThat(observed).filteredOn(code -> code == 202).hasSize(2);
            assertThat(observed).filteredOn(code -> code == 429).hasSize(threads - 2);
        } finally {
            executor.shutdownNow();
        }
    }

    private ResultActions requestFrom(String client, String email) throws Exception {
        return postFrom(client, PASSWORD_RESET_REQUEST, Map.of("email", email));
    }

    private ResultActions postFrom(String client, String path, Map<String, ?> body) throws Exception {
        return mockMvc.perform(post(path)
                .with(request -> {
                    request.setRemoteAddr(client);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }

    private static String withoutInstance(String problemJson) {
        return problemJson.replaceAll("\"instance\":\"[^\"]*\",?", "");
    }
}
