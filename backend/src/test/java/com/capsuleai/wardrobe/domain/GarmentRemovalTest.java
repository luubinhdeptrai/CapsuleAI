package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-005 (S4-GAR-01/02): the removal transition and the minimal removed representation. */
class GarmentRemovalTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-01T08:00:00Z");
    private static final Instant REMOVED_AT = Instant.parse("2026-11-16T09:30:00Z");

    @Test
    void removalKeepsIdentityProfileAndPhotoAndAdvancesTheRevisionOnce() {
        Garment current = currentGarmentAtRevision(3);

        Garment removed = current.withRemovalAt(REMOVED_AT);

        assertThat(removed.isCurrent()).isFalse();
        assertThat(removed.removedAt()).isEqualTo(REMOVED_AT);
        assertThat(removed.revision()).isEqualTo(4);
        assertThat(removed.id()).isEqualTo(current.id());
        assertThat(removed.ownerAccountId()).isEqualTo(current.ownerAccountId());
        assertThat(removed.profile()).isEqualTo(current.profile());
        assertThat(removed.createdAt()).isEqualTo(CREATED_AT);
        assertThat(removed.photo()).isEqualTo(current.photo());
    }

    @Test
    void anAlreadyRemovedGarmentCannotBeRemovedAgain() {
        Garment removed = currentGarmentAtRevision(1).withRemovalAt(REMOVED_AT);

        assertThatThrownBy(() -> removed.withRemovalAt(REMOVED_AT.plusSeconds(60)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aRemovalTimeBeforeTheCreationTimeIsRecordedAsTheCreationTime() {
        Garment removed = currentGarmentAtRevision(1).withRemovalAt(CREATED_AT.minusSeconds(5));

        assertThat(removed.removedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void theRemovedRepresentationKeepsOnlyTheMinimalMeaningAndRefusesACurrentGarment() {
        Garment current = currentGarmentAtRevision(2);
        Garment removed = current.withRemovalAt(REMOVED_AT);

        RemovedGarment removedGarment = RemovedGarment.of(removed);

        assertThat(removedGarment.id()).isEqualTo(current.id());
        assertThat(removedGarment.ownerAccountId()).isEqualTo(current.ownerAccountId());
        assertThat(removedGarment.category()).isEqualTo(GarmentCategory.TOPS);
        assertThat(removedGarment.color()).isEqualTo(GarmentColor.NAVY);
        assertThat(removedGarment.removedAt()).isEqualTo(REMOVED_AT);
        assertThat(RemovedGarment.HISTORICAL_LABEL).isEqualTo("REMOVED_FROM_WARDROBE");
        assertThatThrownBy(() -> RemovedGarment.of(current)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Garment currentGarmentAtRevision(long revision) {
        return new Garment(UUID.randomUUID(), UUID.randomUUID(),
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.NAVY), ValueProvenance.USER_ENTERED,
                CREATED_AT, null, revision,
                Optional.of(new GarmentPhoto(UUID.randomUUID(), "image/jpeg", 800, 600)));
    }
}
