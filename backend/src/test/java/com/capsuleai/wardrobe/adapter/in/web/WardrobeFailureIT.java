package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.adapter.out.persistence.JdbcGarmentRepository;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.List;
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
 * Known Wardrobe failures through the real API: the database connection is lost
 * after part of the work ran. Authentication still works (it uses Identity's own
 * repositories), so this isolates Wardrobe's rollback and error translation.
 */
@Import(WardrobeFailureIT.FailureInjectionConfiguration.class)
class WardrobeFailureIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private FailingGarmentRepository failingGarmentRepository;

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureInjectionConfiguration {

        @Bean
        @Primary
        FailingGarmentRepository failingGarmentRepository(JdbcClient jdbcClient) {
            return new FailingGarmentRepository(new JdbcGarmentRepository(jdbcClient));
        }
    }

    /**
     * TEST-ONLY. The real repository, which can be told to lose the database
     * connection right after it has written the garment row.
     */
    static final class FailingGarmentRepository implements GarmentRepository {

        private final GarmentRepository delegate;
        private final AtomicBoolean failAfterWrite = new AtomicBoolean();

        FailingGarmentRepository(GarmentRepository delegate) {
            this.delegate = delegate;
        }

        void failNextWriteAfterItRan() {
            failAfterWrite.set(true);
        }

        void reset() {
            failAfterWrite.set(false);
        }

        @Override
        public boolean addIfCreationKeyUnused(Garment garment, UUID creationKey, String creationFingerprint) {
            boolean stored = delegate.addIfCreationKeyUnused(garment, creationKey, creationFingerprint);
            if (failAfterWrite.getAndSet(false)) {
                throw new DataAccessResourceFailureException("connection lost after the insert (simulated)");
            }
            return stored;
        }

        @Override
        public Optional<RecordedGarmentCreation> findCreation(UUID ownerAccountId, UUID creationKey) {
            return delegate.findCreation(ownerAccountId, creationKey);
        }
    }

    @AfterEach
    void stopInjectingFailures() {
        failingGarmentRepository.reset();
    }

    @Test
    void aFailedSaveIsRolledBackAndNotReportedAsCreatedAndTheRetryCreatesItOnce() throws Exception {
        IssuedTokens tokens = registerAndLogIn("failed-save@example.com");
        String key = UUID.randomUUID().toString();
        failingGarmentRepository.failNextWriteAfterItRan();

        Map<String, Object> coat = garment("OUTERWEAR", "CAMEL", "materials", List.of("WOOL"),
                "styles", List.of("CLASSIC", "LUXURY"));

        addGarment(tokens.accessToken(), key, coat)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.id").doesNotExist());
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
        assertThat(countRows("wardrobe.wardrobe_garment_material")).isZero();
        assertThat(countRows("wardrobe.wardrobe_garment_style")).isZero();

        addGarment(tokens.accessToken(), key, coat).andExpect(status().isCreated());
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
        assertThat(countRows("wardrobe.wardrobe_garment_style")).isEqualTo(2);
    }

    @Test
    void earlierAcceptedGarmentsAreUntouchedByALaterFailedSave() throws Exception {
        IssuedTokens tokens = registerAndLogIn("existing-garments@example.com");
        UUID existing = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));
        failingGarmentRepository.failNextWriteAfterItRan();

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), minimumGarment("SHOES", "WHITE"))
                .andExpect(status().isServiceUnavailable());

        assertThat(jdbc.sql("SELECT id FROM wardrobe.wardrobe_garment WHERE owner_account_id = :owner")
                .param("owner", tokens.accountId()).query(UUID.class).list()).containsExactly(existing);
    }
}
