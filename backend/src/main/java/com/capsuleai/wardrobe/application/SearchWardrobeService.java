package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.application.ports.WardrobeSearchRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-WAR-003 — one page of the owner's current garments matching optional conditions, newest first, with
 * its meaning (FR-WAR-001/007/008; S4-WAR-01/02/03; OPEN-S4-11–14).
 *
 * <p>Without conditions it reads exactly like the overview (US-WAR-001), through {@link GarmentRepository}.
 * With conditions it uses {@link WardrobeSearchRepository}. An empty first page with conditions is resolved
 * in one read-only snapshot into NO_MATCH or EMPTY_WARDROBE. A later empty page is END_OF_RESULTS. Every
 * page reads current membership (live view): a garment removed meanwhile is simply absent. Read-only: it
 * changes no garment and records no feedback or Wear Event. A failure propagates and is never an empty page.
 */
public final class SearchWardrobeService {

    private final GarmentRepository garmentRepository;
    private final WardrobeSearchRepository wardrobeSearchRepository;
    private final WardrobeTransactions wardrobeTransactions;

    public SearchWardrobeService(GarmentRepository garmentRepository,
                                 WardrobeSearchRepository wardrobeSearchRepository,
                                 WardrobeTransactions wardrobeTransactions) {
        this.garmentRepository = Objects.requireNonNull(garmentRepository, "garmentRepository");
        this.wardrobeSearchRepository = Objects.requireNonNull(wardrobeSearchRepository, "wardrobeSearchRepository");
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
    }

    /**
     * @param pageSize from 1 to {@link BrowseWardrobeService#MAXIMUM_PAGE_SIZE}
     * @param after    the {@code next} position of the previous page with the same conditions; empty for the first page
     */
    public WardrobeSearchPage search(UUID ownerAccountId, WardrobeSearchCriteria criteria, int pageSize,
                                     Optional<WardrobePosition> after) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(criteria, "criteria");
        Objects.requireNonNull(after, "after must be Optional.empty() for the first page");
        if (pageSize < 1 || pageSize > BrowseWardrobeService.MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be from 1 to " + BrowseWardrobeService.MAXIMUM_PAGE_SIZE);
        }
        if (after.isPresent()) {
            List<Garment> found = find(ownerAccountId, criteria, after, pageSize + 1);
            return page(found, pageSize, found.isEmpty() ? WardrobeResultState.END_OF_RESULTS
                    : WardrobeResultState.HAS_RESULTS);
        }
        if (criteria.isUnrestricted()) {
            List<Garment> found = find(ownerAccountId, criteria, after, pageSize + 1);
            return page(found, pageSize, found.isEmpty() ? WardrobeResultState.EMPTY_WARDROBE
                    : WardrobeResultState.HAS_RESULTS);
        }
        WardrobeSearchQuery query = DescriptorMatcher.queryFor(criteria);
        return wardrobeTransactions.inReadOnlySnapshot(() -> {
            List<Garment> found = wardrobeSearchRepository.findCurrentMatching(ownerAccountId, query,
                    Optional.empty(), pageSize + 1);
            if (!found.isEmpty()) {
                return page(found, pageSize, WardrobeResultState.HAS_RESULTS);
            }
            WardrobeResultState emptyMeaning = wardrobeSearchRepository.hasCurrentGarment(ownerAccountId)
                    ? WardrobeResultState.NO_MATCH : WardrobeResultState.EMPTY_WARDROBE;
            return new WardrobeSearchPage(List.of(), Optional.empty(), emptyMeaning);
        });
    }

    private List<Garment> find(UUID ownerAccountId, WardrobeSearchCriteria criteria, Optional<WardrobePosition> after,
                               int maximumCount) {
        if (criteria.isUnrestricted()) {
            return garmentRepository.findCurrentByOwner(ownerAccountId, after, maximumCount);
        }
        return wardrobeSearchRepository.findCurrentMatching(ownerAccountId, DescriptorMatcher.queryFor(criteria),
                after, maximumCount);
    }

    /** One extra row tells whether another page follows, without a second query or a count. */
    private static WardrobeSearchPage page(List<Garment> found, int pageSize, WardrobeResultState resultState) {
        if (found.size() <= pageSize) {
            return new WardrobeSearchPage(found, Optional.empty(), resultState);
        }
        List<Garment> page = found.subList(0, pageSize);
        return new WardrobeSearchPage(page, Optional.of(WardrobePosition.of(page.getLast())), resultState);
    }
}