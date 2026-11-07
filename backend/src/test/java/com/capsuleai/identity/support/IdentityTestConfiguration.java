package com.capsuleai.identity.support;

import com.capsuleai.identity.api.AuthenticatedAccount;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Test infrastructure shared by all Identity integration tests. */
@TestConfiguration(proxyBeanMethods = false)
public class IdentityTestConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer identityPostgres() {
        return new PostgreSQLContainer("postgres:17.11-bookworm")
                .withDatabaseName("capsuleai_identity_test");
    }

    @Bean
    @Primary
    MutableClock mutableClock() {
        return new MutableClock(MutableClock.DEFAULT_START);
    }

    /** Replaces asynchronous SMTP delivery so tests can read the emailed reset secret. */
    @Bean
    @Primary
    RecordingPasswordResetInstructionSender recordingPasswordResetInstructionSender() {
        return new RecordingPasswordResetInstructionSender();
    }

    /**
     * TEST-ONLY protected operation. Production has no such endpoint. It returns
     * the principal resolved by the real security chain.
     */
    @RestController
    public static class ProtectedProbeController {

        public static final String PATH = "/test/identity/whoami";

        @GetMapping(PATH)
        public Map<String, String> whoAmI(@AuthenticationPrincipal AuthenticatedAccount account) {
            return Map.of(
                    "accountId", account.accountId().toString(),
                    "sessionId", account.sessionId().toString());
        }
    }
}
