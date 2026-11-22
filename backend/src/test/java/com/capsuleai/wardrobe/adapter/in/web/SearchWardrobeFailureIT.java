package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.adapter.out.persistence.JdbcWardrobeSearchRepository;
import com.capsuleai.wardrobe.application.WardrobeSearchQuery;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.application.ports.WardrobeSearchRepository;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.List;
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
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-WAR-003 AC-04/AC-06: a failed search is unavailable, never "no match" or an empty wardrobe, and a retry
 * shows the actual result. The unfiltered overview's failures stay covered by WardrobeFailureIT.
 */
@Import(SearchWardrobeFailureIT.FailureInjectionConfiguration.class)
class SearchWardrobeFailureIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private FailingWardrobeSearchRepository failingWardrobeSearchRepository;

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureInjectionConfiguration {

        @Bean
        @Primary
        FailingWardrobeSearchRepository failingWardrobeSearchRepository(JdbcClient jdbcClient) {
            return new FailingWardrobeSearchRepository(new JdbcWardrobeSearchRepository(jdbcClient));
        }
    }

    /** TEST-ONLY. The real search repository, which can be told to lose the database connection. */
    static final class FailingWardrobeSearchRepository implements WardrobeSearchRepository {

        private final WardrobeSearchRepository delegate;
        private final AtomicBoolean failSearches = new AtomicBoolean();
        private final AtomicBoolean failExistenceChecks = new AtomicBoolean();

        FailingWardrobeSearchRepository(WardrobeSearchRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Garment> findCurrentMatching(UUID ownerAccountId, WardrobeSearchQuery query,
                                                 Optional<WardrobePosition> after, int maximumCount) {
            if (failSearches.get()) {
                throw new DataAccessResourceFailureException("connection lost during the search (simulated)");
            }
            return delegate.findCurrentMatching(ownerAccountId, query, after, maximumCount);
        }

        @Override
        public boolean hasCurrentGarment(UUID ownerAccountId) {
            if (failExistenceChecks.get()) {
                throw new DataAccessResourceFailureException("connection lost during the check (simulated)");
            }
            return delegate.hasCurrentGarment(ownerAccountId);
        }

        void reset() {
            failSearches.set(false);
            failExistenceChecks.set(false);
        }
    }

    @AfterEach
    void stopInjectingFailures() {
        failingWardrobeSearchRepository.reset();
    }

    @Test
    void aFailedSearchIsUnavailableNotNoMatchAndARetryShowsTheMatches() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-failure@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        failingWardrobeSearchRepository.failSearches.set(true);

        mockMvc.perform(get(GARMENTS).queryParam("q", "navy")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.resultState").doesNotExist());

        failingWardrobeSearchRepository.reset();
        mockMvc.perform(get(GARMENTS).queryParam("q", "navy")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultState").value("HAS_RESULTS"))
                .andExpect(jsonPath("$.garments[0].id").value(garmentId.toString()));
    }

    @Test
    void aFailedExistenceCheckIsUnavailableNotAnEmptyWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-failure-existence@example.com");
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));
        failingWardrobeSearchRepository.failExistenceChecks.set(true);

        mockMvc.perform(get(GARMENTS).queryParam("q", "navy")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.resultState").doesNotExist());
    }
}
