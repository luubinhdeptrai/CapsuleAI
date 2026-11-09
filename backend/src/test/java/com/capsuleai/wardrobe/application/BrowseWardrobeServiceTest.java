package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.ValueProvenance;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrowseWardrobeServiceTest {

    private static final Instant START = Instant.parse("2026-01-05T08:00:00Z");

    private final InMemoryGarmentRepository garments = new InMemoryGarmentRepository();
    private final BrowseWardrobeService service = new BrowseWardrobeService(garments);
    private final UUID owner = UUID.randomUUID();

    @Test
    void aWardrobeWithNoGarmentIsAnEmptyLastPage() {
        WardrobePage page = firstPage(BrowseWardrobeService.DEFAULT_PAGE_SIZE);

        assertThat(page.garments()).isEmpty();
        assertThat(page.next()).isEmpty();
    }

    @Test
    void onlyTheOwnersCurrentGarmentsAreReturnedNewestFirst() {
        Garment older = garment(owner, START, null);
        Garment newer = garment(owner, START.plus(Duration.ofMinutes(1)), null);
        Garment removed = garment(owner, START, START.plus(Duration.ofHours(1)));
        Garment someoneElses = garment(UUID.randomUUID(), START, null);
        for (Garment garment : new Garment[] {older, newer, removed, someoneElses}) {
            garments.add(garment);
        }

        assertThat(firstPage(BrowseWardrobeService.DEFAULT_PAGE_SIZE).garments()).containsExactly(newer, older);
    }

    @Test
    void pagesFollowEachOtherWithoutGapsOrRepeatsAndTheLastPageHasNoNext() {
        List<Garment> oldestFirst = addOneMinuteApart(5);

        WardrobePage first = firstPage(2);
        WardrobePage second = service.browse(owner, 2, first.next());
        WardrobePage third = service.browse(owner, 2, second.next());

        assertThat(first.garments()).containsExactly(oldestFirst.get(4), oldestFirst.get(3));
        assertThat(first.next()).contains(WardrobePosition.of(oldestFirst.get(3)));
        assertThat(second.garments()).containsExactly(oldestFirst.get(2), oldestFirst.get(1));
        assertThat(third.garments()).containsExactly(oldestFirst.get(0));
        assertThat(third.next()).isEmpty();
    }

    @Test
    void aWardrobeThatFillsItsLastPageExactlyEndsWithoutAnEmptyExtraPage() {
        addOneMinuteApart(4);

        WardrobePage second = service.browse(owner, 2, firstPage(2).next());

        assertThat(second.garments()).hasSize(2);
        assertThat(second.next()).isEmpty();
    }

    @Test
    void garmentsCreatedAtTheSameInstantAreEachListedExactlyOnce() {
        List<Garment> sameInstant = new ArrayList<>();
        for (int count = 0; count < 3; count++) {
            Garment garment = garment(owner, START, null);
            garments.add(garment);
            sameInstant.add(garment);
        }

        List<Garment> visited = new ArrayList<>();
        Optional<WardrobePosition> after = Optional.empty();
        do {
            WardrobePage page = service.browse(owner, 1, after);
            visited.addAll(page.garments());
            after = page.next();
        } while (after.isPresent());

        assertThat(visited).hasSize(3).containsExactlyInAnyOrderElementsOf(sameInstant);
    }

    @Test
    void thePageSizeMustBeFromOneToTheMaximum() {
        assertThatThrownBy(() -> firstPage(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> firstPage(BrowseWardrobeService.MAXIMUM_PAGE_SIZE + 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(firstPage(BrowseWardrobeService.MAXIMUM_PAGE_SIZE).garments()).isEmpty();
    }

    private WardrobePage firstPage(int pageSize) {
        return service.browse(owner, pageSize, Optional.empty());
    }

    private List<Garment> addOneMinuteApart(int count) {
        List<Garment> oldestFirst = new ArrayList<>();
        for (int minute = 0; minute < count; minute++) {
            Garment garment = garment(owner, START.plus(Duration.ofMinutes(minute)), null);
            garments.add(garment);
            oldestFirst.add(garment);
        }
        return oldestFirst;
    }

    private static Garment garment(UUID owner, Instant createdAt, Instant removedAt) {
        return new Garment(UUID.randomUUID(), owner, GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.RED),
                ValueProvenance.USER_ENTERED, createdAt, removedAt);
    }
}
