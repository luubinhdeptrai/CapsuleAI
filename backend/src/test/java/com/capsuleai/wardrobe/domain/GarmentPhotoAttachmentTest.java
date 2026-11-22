package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Provisional US-GAR-006 (S4-GAR-05): attaching a photo keeps the garment and advances its revision once. */
class GarmentPhotoAttachmentTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-01T08:00:00Z");
    private static final GarmentPhoto PHOTO = new GarmentPhoto(UUID.randomUUID(), "image/jpeg", 1536, 2048);

    @Test
    void attachingKeepsEveryPropertyAndAdvancesTheRevisionOnce() {
        Garment imageless = imagelessGarmentAtRevision(2);

        Garment photographed = imageless.withAttachedPhoto(PHOTO);

        assertThat(photographed.photo()).contains(PHOTO);
        assertThat(photographed.revision()).isEqualTo(3);
        assertThat(photographed.profile()).isEqualTo(imageless.profile());
        assertThat(photographed.id()).isEqualTo(imageless.id());
        assertThat(photographed.createdAt()).isEqualTo(CREATED_AT);
        assertThat(photographed.provenance()).isEqualTo(ValueProvenance.USER_ENTERED);
        assertThat(photographed.isCurrent()).isTrue();
    }

    @Test
    void aGarmentWithAPhotoCannotReceiveAnother() {
        Garment photographed = imagelessGarmentAtRevision(1).withAttachedPhoto(PHOTO);

        assertThatThrownBy(() -> photographed.withAttachedPhoto(
                new GarmentPhoto(UUID.randomUUID(), "image/jpeg", 800, 600)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aRemovedGarmentCannotReceiveAPhoto() {
        Garment removed = imagelessGarmentAtRevision(1).withRemovalAt(CREATED_AT.plusSeconds(60));

        assertThatThrownBy(() -> removed.withAttachedPhoto(PHOTO)).isInstanceOf(IllegalStateException.class);
    }

    private static Garment imagelessGarmentAtRevision(long revision) {
        GarmentProfile profile = new GarmentProfile(GarmentCategory.TOPS, Optional.of(GarmentSubtype.T_SHIRTS),
                GarmentColor.NAVY, Set.of(GarmentMaterial.COTTON), Optional.empty(), Set.of(GarmentStyle.CASUAL),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(new Brand("Uniqlo")), Optional.empty());
        return new Garment(UUID.randomUUID(), UUID.randomUUID(), profile, ValueProvenance.USER_ENTERED,
                CREATED_AT, null, revision);
    }
}
