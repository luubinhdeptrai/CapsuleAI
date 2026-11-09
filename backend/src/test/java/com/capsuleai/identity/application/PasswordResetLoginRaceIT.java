package com.capsuleai.identity.application;

import com.capsuleai.identity.adapter.out.security.Pbkdf2PasswordHasher;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-AUTH-005 versus login, interleaved deterministically: a login has verified the
 * OLD password and is paused before it creates its session; a reset then commits;
 * the login resumes. LogInService verifies outside its transaction, so without the
 * recheck under the account lock this login would create a session that the reset
 * never revoked.
 */
@Import(PasswordResetLoginRaceIT.PausingPasswordHasherConfiguration.class)
class PasswordResetLoginRaceIT extends IdentityIntegrationTestSupport {

    private static final String OLD_PASSWORD = PASSWORD;
    private static final String NEW_PASSWORD = "the passphrase set by reset";

    @Autowired
    private PausingPasswordHasher pausingPasswordHasher;

    @Autowired
    private LogInService logInService;

    @TestConfiguration(proxyBeanMethods = false)
    static class PausingPasswordHasherConfiguration {

        @Bean
        @Primary
        PausingPasswordHasher pausingPasswordHasher() {
            return new PausingPasswordHasher(new Pbkdf2PasswordHasher());
        }
    }

    /**
     * TEST-ONLY. Delegates to the real PBKDF2 hasher; when armed, a successful
     * verification announces itself and then waits until the test releases it.
     */
    static final class PausingPasswordHasher implements PasswordHasher {

        private final PasswordHasher delegate;
        private final AtomicBoolean armed = new AtomicBoolean();
        private volatile CountDownLatch verified = new CountDownLatch(1);
        private volatile CountDownLatch mayContinue = new CountDownLatch(1);

        PausingPasswordHasher(PasswordHasher delegate) {
            this.delegate = delegate;
        }

        void armForOneVerification() {
            verified = new CountDownLatch(1);
            mayContinue = new CountDownLatch(1);
            armed.set(true);
        }

        boolean awaitVerification() throws InterruptedException {
            return verified.await(30, TimeUnit.SECONDS);
        }

        void release() {
            armed.set(false);
            mayContinue.countDown();
        }

        @Override
        public PasswordHash hash(String rawPassword) {
            return delegate.hash(rawPassword);
        }

        @Override
        public boolean matches(String rawPassword, PasswordHash storedHash) {
            boolean matched = delegate.matches(rawPassword, storedHash);
            if (matched && armed.compareAndSet(true, false)) {
                verified.countDown();
                try {
                    mayContinue.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            return matched;
        }
    }

    @AfterEach
    void releaseAnyPausedLogin() {
        pausingPasswordHasher.release();
    }

    @Test
    void aLoginThatVerifiedTheOldPasswordBeforeAResetCommitsGetsNoSession() throws Exception {
        registerAccount("race@example.com", OLD_PASSWORD);
        String resetToken = requestPasswordResetToken("race@example.com");
        pausingPasswordHasher.armForOneVerification();

        CompletableFuture<Object> pausedLogin = CompletableFuture.supplyAsync(() -> {
            try {
                return logInService.logIn(new LogInCommand("race@example.com", OLD_PASSWORD));
            } catch (RuntimeException rejected) {
                return rejected;
            }
        });
        assertThat(pausingPasswordHasher.awaitVerification()).as("login verified the old password").isTrue();

        completePasswordReset(resetToken, NEW_PASSWORD).andExpect(status().isNoContent());
        pausingPasswordHasher.release();

        assertThat(pausedLogin.get(30, TimeUnit.SECONDS)).isInstanceOf(InvalidCredentialsException.class);
        assertThat(countRows("identity.identity_session")).isZero();
        postJson(LOGIN, Map.of("email", "race@example.com", "password", OLD_PASSWORD))
                .andExpect(status().isUnauthorized());
        logIn("race@example.com", NEW_PASSWORD);
    }

    @Test
    void aPausedLoginWithoutAResetStillSucceeds() throws Exception {
        registerAccount("no-race@example.com", OLD_PASSWORD);
        pausingPasswordHasher.armForOneVerification();

        CompletableFuture<SessionTokens> pausedLogin = CompletableFuture.supplyAsync(
                () -> logInService.logIn(new LogInCommand("no-race@example.com", OLD_PASSWORD)));
        assertThat(pausingPasswordHasher.awaitVerification()).isTrue();
        pausingPasswordHasher.release();

        SessionTokens tokens = pausedLogin.get(30, TimeUnit.SECONDS);
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());
    }
}
