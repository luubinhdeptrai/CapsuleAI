package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.application.ports.WardrobeSearchRepository;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-WAR-003 (S4-WAR-03, OPEN-S4-14): which result state a page has and when the snapshot is used. */
class SearchWardrobeServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-11-01T08:00:00Z");
    private static final WardrobeSearchCriteria NAVY_TEXT =
            new WardrobeSearchCriteria(Optional.of("navy"), Set.of(), Set.of());

    /** A search repository that returns prepared rows and counts existence checks. */
    private static final class PreparedSearchRepository implements WardrobeSearchRepository {

        private List<Garment> matches = List.of();
        private boolean ownerHasCurrentGarments;
        private int existenceChecks;

        @Override
        public List<Garment> findCurrentMatching(UUID ownerAccountId, WardrobeSearchQuery query,
                                                 Optional<WardrobePosition> after, int maximumCount) {
            return matches.stream().limit(maximumCount).toList();
        }

        @Override
        public boolean hasCurrentGarment(UUID ownerAccountId) {
            existenceChecks++;
            return ownerHasCurrentGarments;
        }
    }

    private InMemoryGarmentRepository garmentRepository;
    private PreparedSearchRepository wardrobeSearchRepository;
    private ImmediateWardrobeTransactions wardrobeTransactions;
    private SearchWardrobeService searchWardrobeService;

    @BeforeEach
    void setUp() {
        garmentRepository = new InMemoryGarmentRepository();
        wardrobeSearchRepository = new PreparedSearchRepository();
        wardrobeTransactions = new ImmediateWardrobeTransactions();
        searchWardrobeService = new SearchWardrobeService(garmentRepository, wardrobeSearchRepository,
                wardrobeTransactions);
    }

    @Test
    void aFilteredFirstPageWithMoreMatchesHasResultsAndANextPosition() {
        wardrobeSearchRepository.matches = List.of(garment(3), garment(2), garment(1));

        WardrobeSearchPage page = searchWardrobeService.search(OWNER, NAVY_TEXT, 2, Optional.empty());

        assertThat(page.garments()).hasSize(2);
        assertThat(page.next()).contains(WardrobePosition.of(page.garments().getLast()));
        assertThat(page.resultState()).isEqualTo(WardrobeResultState.HAS_RESULTS);
        assertThat(wardrobeTransactions.count()).as("a filtered first page reads in one snapshot").isOne();
    }

    @Test
    void anUnrestrictedEmptyFirstPageIsAnEmptyWardrobeWithoutAnExistenceCheck() {
        WardrobeSearchPage page = searchWardrobeService.search(OWNER, WardrobeSearchCriteria.unrestricted(), 20,
                Optional.empty());

        assertThat(page.resultState()).isEqualTo(WardrobeResultState.EMPTY_WARDROBE);
        assertThat(wardrobeSearchRepository.existenceChecks).isZero();
        assertThat(wardrobeTransactions.count()).isZero();
    }

    @Test
    void aFilteredEmptyFirstPageIsNoMatchWhenTheOwnerHasCurrentGarments() {
        wardrobeSearchRepository.ownerHasCurrentGarments = true;

        WardrobeSearchPage page = searchWardrobeService.search(OWNER, NAVY_TEXT, 20, Optional.empty());

        assertThat(page.resultState()).isEqualTo(WardrobeResultState.NO_MATCH);
        assertThat(page.garments()).isEmpty();
        assertThat(wardrobeSearchRepository.existenceChecks).isOne();
    }

    @Test
    void aFilteredEmptyFirstPageIsAnEmptyWardrobeWhenTheOwnerHasNone() {
        WardrobeSearchPage page = searchWardrobeService.search(OWNER,
                new WardrobeSearchCriteria(Optional.empty(), Set.of(GarmentCategory.SHOES), Set.of()), 20,
                Optional.empty());

        assertThat(page.resultState()).isEqualTo(WardrobeResultState.EMPTY_WARDROBE);
    }

    @Test
    void anEmptyLaterPageIsTheEndOfResults() {
        WardrobePosition position = WardrobePosition.of(garment(1));

        WardrobeSearchPage page = searchWardrobeService.search(OWNER, NAVY_TEXT, 20, Optional.of(position));

        assertThat(page.resultState()).isEqualTo(WardrobeResultState.END_OF_RESULTS);
        assertThat(page.next()).isEmpty();
        assertThat(wardrobeSearchRepository.existenceChecks).isZero();
    }

    private static Garment garment(int secondsAfterCreation) {
        return Garment.confirmManualEntry(UUID.randomUUID(), OWNER,
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.NAVY),
                CREATED_AT.plusSeconds(secondsAfterCreation));
    }
}
