package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.adapter.out.persistence.JdbcOptionalContextRepository;
import com.capsuleai.personalcontext.application.ports.OptionalContextRepository;
import com.capsuleai.personalcontext.domain.OptionalContext;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.time.Instant;
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
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-PROF-002 known failures through the real API (AC-05; UC-005 E2/E3): an unreachable record is 503, never an
 * empty context, and a write whose connection is lost after its statement ran is rolled back and reported as not
 * confirmed, never as saved. Authentication keeps working, because it uses Identity's own repositories.
 */
@Import(OptionalContextFailureIT.FailureInjectionConfiguration.class)
class OptionalContextFailureIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private FailingOptionalContextRepository failingOptionalContextRepository;

    @AfterEach
    void stopFailing() {
        failingOptionalContextRepository.reset();
    }

    @Test
    void anUnreachableRecordIsUnavailableNeverEmpty() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-outage-read@example.com");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "WOMAN"))
                .andExpect(status().isOk());
        failingOptionalContextRepository.failReadsUntilReset();

        viewOptionalContext(tokens.accessToken())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.revision").doesNotExist())
                .andExpect(jsonPath("$.genderContext").doesNotExist());
    }

    @Test
    void aRevisionWhoseConnectionIsLostIsRolledBackAndAppliedOnceOnRetry() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-outage-revision@example.com");
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "genderContext", "WOMAN"))
                .andExpect(status().isOk());
        failingOptionalContextRepository.failNextWriteAfterItRan();

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "bodyShape", "ROUND"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"));

        viewOptionalContext(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.genderContext").value("WOMAN"))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));
        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", "1", "bodyShape", "ROUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.bodyShape").value("ROUND"));
    }

    @Test
    void aFirstDeclarationWhoseConnectionIsLostCreatesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("optional-outage-first@example.com");
        failingOptionalContextRepository.failNextWriteAfterItRan();

        reviseOptionalContext(tokens.accessToken(), json("expectedRevision", null, "bodyShape", "HOURGLASS"))
                .andExpect(status().isServiceUnavailable());

        assertThat(countRows("personal_context.personal_context_optional_context")).isZero();
        viewOptionalContext(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.bodyShape").value(nullValue()));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureInjectionConfiguration {

        @Bean
        @Primary
        FailingOptionalContextRepository failingOptionalContextRepository(JdbcClient jdbcClient) {
            return new FailingOptionalContextRepository(new JdbcOptionalContextRepository(jdbcClient));
        }
    }

    /** TEST-ONLY. The real repository, which can be told to lose the connection on reads or after a write ran. */
    static final class FailingOptionalContextRepository implements OptionalContextRepository {

        private final OptionalContextRepository delegate;
        private final AtomicBoolean failReads = new AtomicBoolean();
        private final AtomicBoolean failAfterWrite = new AtomicBoolean();

        FailingOptionalContextRepository(OptionalContextRepository delegate) {
            this.delegate = delegate;
        }

        void failReadsUntilReset() {
            failReads.set(true);
        }

        void failNextWriteAfterItRan() {
            failAfterWrite.set(true);
        }

        void reset() {
            failReads.set(false);
            failAfterWrite.set(false);
        }

        @Override
        public Optional<OptionalContextProfile> find(UUID ownerAccountId) {
            if (failReads.get()) {
                throw new DataAccessResourceFailureException("Simulated loss of the database connection");
            }
            return delegate.find(ownerAccountId);
        }

        @Override
        public boolean createIfAbsent(OptionalContextProfile profile) {
            boolean created = delegate.createIfAbsent(profile);
            failIfRequested();
            return created;
        }

        @Override
        public boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, OptionalContext values,
                                         Instant updatedAt) {
            boolean revised = delegate.reviseIfCurrentAt(ownerAccountId, expectedRevision, values, updatedAt);
            failIfRequested();
            return revised;
        }

        private void failIfRequested() {
            if (failAfterWrite.getAndSet(false)) {
                throw new DataAccessResourceFailureException("Simulated loss of the database connection after the write");
            }
        }
    }
}
