package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-002: the garment revision is metadata that only accepted edits advance (S3-GAR-02). */
class GarmentTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant CREATED = Instant.parse("2026-11-01T08:00:00Z");

    @Test
    void aNewlyConfirmedGarmentIsAtTheFirstRevision() {
        Garment garment = Garment.confirmManualEntry(UUID.randomUUID(), OWNER,
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.WHITE), CREATED);

        assertThat(garment.revision()).isEqualTo(Garment.FIRST_REVISION).isEqualTo(1L);
    }

    @Test
    void aRevisionBelowTheFirstIsRejected() {
        GarmentProfile profile = GarmentProfile.minimum(GarmentCategory.SHOES, GarmentColor.BLACK);

        assertThatThrownBy(() -> new Garment(UUID.randomUUID(), OWNER, profile, ValueProvenance.USER_ENTERED,
                CREATED, null, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Garment(UUID.randomUUID(), OWNER, profile, ValueProvenance.USER_ENTERED,
                CREATED, null, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRevisedProfileKeepsIdentityOwnerProvenanceAndCreationTimeAndAdvancesTheRevisionByOne() {
        Garment original = Garment.confirmManualEntry(UUID.randomUUID(), OWNER,
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.WHITE), CREATED);
        GarmentProfile corrected = new GarmentProfile(GarmentCategory.TOPS, Optional.of(GarmentSubtype.SHIRTS),
                GarmentColor.NAVY, Set.of(GarmentMaterial.LINEN), Optional.empty(), Set.of(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty());

        Garment revised = original.withRevisedProfile(corrected);

        assertThat(revised.id()).isEqualTo(original.id());
        assertThat(revised.ownerAccountId()).isEqualTo(OWNER);
        assertThat(revised.provenance()).isEqualTo(ValueProvenance.USER_ENTERED);
        assertThat(revised.createdAt()).isEqualTo(CREATED);
        assertThat(revised.isCurrent()).isTrue();
        assertThat(revised.profile()).isEqualTo(corrected);
        assertThat(revised.revision()).isEqualTo(2L);
        assertThat(original.revision()).as("the original value object is unchanged").isEqualTo(1L);
    }

    @Test
    void aGarmentThatLeftTheCurrentWardrobeCannotBeRevised() {
        Garment removed = new Garment(UUID.randomUUID(), OWNER,
                GarmentProfile.minimum(GarmentCategory.BAGS, GarmentColor.RED), ValueProvenance.USER_ENTERED,
                CREATED, CREATED.plusSeconds(60), 3);

        assertThatThrownBy(() -> removed.withRevisedProfile(GarmentProfile.minimum(GarmentCategory.BAGS,
                GarmentColor.BLUE)))
                .isInstanceOf(IllegalStateException.class);
    }
}
