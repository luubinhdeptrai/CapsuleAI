package com.capsuleai.identity.application;

import com.capsuleai.identity.adapter.out.persistence.JdbcPasswordResetAttemptLimiter;
import com.capsuleai.identity.adapter.out.persistence.JdbcPasswordResetRepository;
import com.capsuleai.identity.adapter.out.persistence.JdbcSessionRepository;
import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetAttemptScope;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-AUTH-005 AC-14 and ERR-DEP-001 through the real API and PostgreSQL: the
 * database connection is lost part-way through. A reset is all or nothing, a
 * failed issuance sends no email, and both are reported as unconfirmed, never as done.
 */
@Import(PasswordResetFailureIT.FailureInjectionConfiguration.class)
class PasswordResetFailureIT extends IdentityIntegrationTestSupport {

    private static final String NEW_PASSWORD = "a brand new passphrase";

    @Autowired
    private FailingSessionRepository failingSessionRepository;

    @Autowired
    private FailingPasswordResetRepository failingPasswordResetRepository;

    @Autowired
    private FailingAttemptLimiter failingAttemptLimiter;

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureInjectionConfiguration {

        @Bean
        @Primary
        FailingSessionRepository failingSessionRepository(JdbcClient jdbcClient) {
            return new FailingSessionRepository(new JdbcSessionRepository(jdbcClient));
        }

        @Bean
        @Primary
        FailingPasswordResetRepository failingPasswordResetRepository(JdbcClient jdbcClient) {
            return new FailingPasswordResetRepository(new JdbcPasswordResetRepository(jdbcClient));
        }

        @Bean
        @Primary
        FailingAttemptLimiter failingAttemptLimiter(JdbcClient jdbcClient) {
            return new FailingAttemptLimiter(new JdbcPasswordResetAttemptLimiter(jdbcClient));
        }
    }

    /** TEST-ONLY. The real session repository, which can lose the connection while revoking. */
    static final class FailingSessionRepository implements SessionRepository {

        private final SessionRepository delegate;
        private final AtomicBoolean failRevocation = new AtomicBoolean();

        FailingSessionRepository(SessionRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
            delegate.create(session, firstRefreshToken);
        }

        @Override
        public Optional<LoginSession> findById(UUID sessionId) {
            return delegate.findById(sessionId);
        }

        @Override
        public boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt) {
            return delegate.revoke(sessionId, accountId, reason, revokedAt);
        }

        @Override
        public int revokeAllActiveForAccount(UUID accountId, RevocationReason reason, Instant revokedAt) {
            int revoked = delegate.revokeAllActiveForAccount(accountId, reason, revokedAt);
            if (failRevocation.getAndSet(false)) {
                throw new DataAccessResourceFailureException("connection lost after the revocation (simulated)");
            }
            return revoked;
        }

        @Override
        public Optional<LockedRefreshCredential> lockByRefreshToken(RefreshTokenHash hash) {
            return delegate.lockByRefreshToken(hash);
        }

        @Override
        public void consumeRefreshToken(UUID refreshTokenId, Instant consumedAt) {
            delegate.consumeRefreshToken(refreshTokenId, consumedAt);
        }

        @Override
        public void addRefreshToken(UUID sessionId, RefreshTokenHash hash, Instant issuedAt) {
            delegate.addRefreshToken(sessionId, hash, issuedAt);
        }
    }

    /** TEST-ONLY. The real reset repository, which can lose the connection while storing an issuance. */
    /** Fails every attempt count while {@code failCounting} is set: the counter store is unreachable. */
    static final class FailingAttemptLimiter implements PasswordResetAttemptLimiter {

        private final PasswordResetAttemptLimiter delegate;
        private final AtomicBoolean failCounting = new AtomicBoolean();

        FailingAttemptLimiter(PasswordResetAttemptLimiter delegate) {
            this.delegate = delegate;
        }

        @Override
        public int recordAttempt(PasswordResetAttemptScope scope, String subject, Instant windowStart) {
            if (failCounting.get()) {
                throw new DataAccessResourceFailureException("injected: attempt counters unreachable");
            }
            return delegate.recordAttempt(scope, subject, windowStart);
        }

        @Override
        public int forgetWindowsStartedBefore(Instant cutoff) {
            return delegate.forgetWindowsStartedBefore(cutoff);
        }
    }

    static final class FailingPasswordResetRepository implements PasswordResetRepository {

        private final PasswordResetRepository delegate;
        private final AtomicBoolean failAdd = new AtomicBoolean();

        FailingPasswordResetRepository(PasswordResetRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public int replaceOpenTokens(UUID accountId, Instant replacedAt) {
            return delegate.replaceOpenTokens(accountId, replacedAt);
        }

        @Override
        public void add(PasswordResetToken token, PasswordResetTokenHash hash) {
            if (failAdd.getAndSet(false)) {
                throw new DataAccessResourceFailureException("connection lost before the insert (simulated)");
            }
            delegate.add(token, hash);
        }

        @Override
        public Optional<PasswordResetToken> findByTokenHash(PasswordResetTokenHash hash) {
            return delegate.findByTokenHash(hash);
        }

        @Override
        public Optional<PasswordResetToken> lockByTokenHash(PasswordResetTokenHash hash) {
            return delegate.lockByTokenHash(hash);
        }

        @Override
        public void consume(UUID tokenId, Instant consumedAt) {
            delegate.consume(tokenId, consumedAt);
        }

        @Override
        public int deleteEndedBefore(Instant cutoff, int batchSize) {
            return delegate.deleteEndedBefore(cutoff, batchSize);
        }
    }

    @AfterEach
    void stopInjectingFailures() {
        failingSessionRepository.failRevocation.set(false);
        failingPasswordResetRepository.failAdd.set(false);
        failingAttemptLimiter.failCounting.set(false);
    }

    @Test
    void aResetThatFailsAfterChangingThePasswordIsRolledBackEntirely() throws Exception {
        IssuedTokens tokens = registerAndLogIn("rollback@example.com");
        String resetToken = requestPasswordResetToken("rollback@example.com");
        failingSessionRepository.failRevocation.set(true);

        completePasswordReset(resetToken, NEW_PASSWORD)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"));

        // Nothing of the failed reset survived: old password, sessions and the link all still work.
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());
        logIn("rollback@example.com", PASSWORD);
        postJson(PASSWORD_RESET_CHECK, Map.of("token", resetToken)).andExpect(status().isNoContent());
        assertThat(jdbc.sql("SELECT count(*) FROM identity.identity_password_reset WHERE consumed_at IS NOT NULL")
                .query(Long.class).single()).isZero();

        completePasswordReset(resetToken, NEW_PASSWORD).andExpect(status().isNoContent());
        logIn("rollback@example.com", NEW_PASSWORD);
    }

    @Test
    void anIssuanceThatCannotBeStoredSendsNoEmailAndCanBeRetried() throws Exception {
        registerAccount("issuance-failure@example.com", PASSWORD);
        failingPasswordResetRepository.failAdd.set(true);

        postJson(PASSWORD_RESET_REQUEST, Map.of("email", "issuance-failure@example.com"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"));
        assertThat(passwordResetInstructions.sent()).isEmpty();
        assertThat(countRows("identity.identity_password_reset")).isZero();

        String resetToken = requestPasswordResetToken("issuance-failure@example.com");
        postJson(PASSWORD_RESET_CHECK, Map.of("token", resetToken)).andExpect(status().isNoContent());
    }

    // ---- OPEN-S2-05: failure paths of the Forgot Password request

    /** The counters run before the account lookup, so an outage there looks the same for every address. */
    @Test
    void aCounterStoreFailureIsTheSameTruthful503ForRegisteredAndUnregisteredAddresses() throws Exception {
        registerAccount("known-during-outage@example.com", PASSWORD);
        failingAttemptLimiter.failCounting.set(true);

        String registered = postJson(PASSWORD_RESET_REQUEST, Map.of("email", "known-during-outage@example.com"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        String unregistered = postJson(PASSWORD_RESET_REQUEST, Map.of("email", "unknown-during-outage@example.com"))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(registered.replaceAll("\"instance\":\"[^\"]*\",?", ""))
                .isEqualTo(unregistered.replaceAll("\"instance\":\"[^\"]*\",?", ""));
        assertThat(passwordResetInstructions.sent()).isEmpty();
    }

    /**
     * Documented residual risk: only a registered address reaches the issuance writes, so a
     * failure exactly there is a truthful 503 that an unregistered address does not produce.
     */
    @Test
    void aFailureWhileIssuingIsATruthful503ThatOnlyARegisteredAddressCanReach() throws Exception {
        registerAccount("known-partial@example.com", PASSWORD);
        failingPasswordResetRepository.failAdd.set(true);

        postJson(PASSWORD_RESET_REQUEST, Map.of("email", "known-partial@example.com"))
                .andExpect(status().isServiceUnavailable());
        postJson(PASSWORD_RESET_REQUEST, Map.of("email", "unknown-partial@example.com"))
                .andExpect(status().isAccepted());
        assertThat(countRows("identity.identity_password_reset")).isZero();
    }
}
