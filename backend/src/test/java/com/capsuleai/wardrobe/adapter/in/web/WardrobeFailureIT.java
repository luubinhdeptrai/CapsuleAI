package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.adapter.out.persistence.JdbcGarmentRepository;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        private final AtomicBoolean failReads = new AtomicBoolean();
        private final AtomicBoolean failAfterRevision = new AtomicBoolean();

        FailingGarmentRepository(GarmentRepository delegate) {
            this.delegate = delegate;
        }

        void failNextWriteAfterItRan() {
            failAfterWrite.set(true);
        }

        void failReadsUntilReset() {
            failReads.set(true);
        }

        void failNextRevisionAfterItRan() {
            failAfterRevision.set(true);
        }

        void reset() {
            failAfterWrite.set(false);
            failReads.set(false);
            failAfterRevision.set(false);
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

        @Override
        public List<Garment> findCurrentByOwner(
                UUID ownerAccountId, Optional<WardrobePosition> after, int maximumCount) {
            failReadIfRequested();
            return delegate.findCurrentByOwner(ownerAccountId, after, maximumCount);
        }

        @Override
        public Optional<Garment> findOwned(UUID ownerAccountId, UUID garmentId) {
            failReadIfRequested();
            return delegate.findOwned(ownerAccountId, garmentId);
        }

        @Override
        public boolean reviseIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision,
                                         GarmentProfile revisedProfile) {
            boolean revised = delegate.reviseIfCurrentAt(ownerAccountId, garmentId, expectedRevision, revisedProfile);
            if (failAfterRevision.getAndSet(false)) {
                throw new DataAccessResourceFailureException("connection lost after the revision (simulated)");
            }
            return revised;
        }

        private void failReadIfRequested() {
            if (failReads.get()) {
                throw new DataAccessResourceFailureException("connection lost during the read (simulated)");
            }
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

    /** US-WAR-001 AC-05: unavailable data is not an empty wardrobe, and retry shows the real one. */
    @Test
    void aFailedOverviewIsUnavailableNotEmptyAndARetryShowsTheActualWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("failed-overview@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("BAGS", "GOLD"));
        failingGarmentRepository.failReadsUntilReset();

        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.garments").doesNotExist());

        failingGarmentRepository.reset();
        mockMvc.perform(get(GARMENTS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[0].id").value(garmentId.toString()));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /** OPEN-S2-10: a later page that cannot be read is unavailable, and the same cursor works on retry. */
    @Test
    void aFailedLaterPageIsUnavailableNotTheEndAndTheSameCursorWorksOnRetry() throws Exception {
        IssuedTokens tokens = registerAndLogIn("failed-page@example.com");
        UUID older = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BROWN"));
        clock.advance(Duration.ofSeconds(1));
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "WHITE"));
        String cursor = JsonPath.read(mockMvc.perform(get(GARMENTS).queryParam("limit", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.nextCursor");
        failingGarmentRepository.failReadsUntilReset();

        mockMvc.perform(get(GARMENTS).queryParam("limit", "1").queryParam("cursor", cursor)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.garments").doesNotExist())
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

        failingGarmentRepository.reset();
        mockMvc.perform(get(GARMENTS).queryParam("limit", "1").queryParam("cursor", cursor)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[0].id").value(older.toString()));
    }

    /** US-WAR-002 AC-08: a failed detail is unavailable, not "deleted", and changes nothing. */
    @Test
    void aFailedDetailIsUnavailableNotDeletedAndARetryShowsTheGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("failed-detail@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "IVORY"));
        failingGarmentRepository.failReadsUntilReset();

        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"));

        failingGarmentRepository.reset();
        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(garmentId.toString()));
    }

    /** US-GAR-002 AC-05/AC-07: a failed edit is rolled back, not reported as saved, and the reread shows the truth. */
    @Test
    void aFailedEditIsRolledBackAndNotReportedAsSaved() throws Exception {
        IssuedTokens tokens = registerAndLogIn("failed-edit@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "WHITE", "materials", List.of("COTTON")));
        failingGarmentRepository.failNextRevisionAfterItRan();

        mockMvc.perform(patch(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,\"expectedRevision\":\"1\",\"color\":\"BLACK\","
                                + "\"materials\":[\"WOOL\",\"SILK\"]}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.revision").doesNotExist());

        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("WHITE"))
                .andExpect(jsonPath("$.materials.length()").value(1))
                .andExpect(jsonPath("$.materials[0]").value("COTTON"))
                .andExpect(jsonPath("$.revision").value("1"));
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
