package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.ValueProvenance;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspectGarmentServiceTest {

    private static final Instant CREATED = Instant.parse("2026-01-05T08:00:00Z");

    private final InMemoryGarmentRepository garments = new InMemoryGarmentRepository();
    private final InspectGarmentService service = new InspectGarmentService(garments);
    private final UUID owner = UUID.randomUUID();

    @Test
    void theOwnersCurrentGarmentIsReturned() {
        Garment garment = stored(owner, null);

        assertThat(service.inspect(owner, garment.id().toString())).isEqualTo(garment);
    }

    @Test
    void anotherOwnersGarmentIsNotFoundExactlyLikeAnUnknownOne() {
        Garment someoneElses = stored(UUID.randomUUID(), null);

        assertThatThrownBy(() -> service.inspect(owner, someoneElses.id().toString()))
                .isInstanceOf(GarmentNotFoundException.class);
        assertThatThrownBy(() -> service.inspect(owner, UUID.randomUUID().toString()))
                .isInstanceOf(GarmentNotFoundException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "not-a-uuid", "1-1-1-1-1"})
    void aMalformedIdIsNotFound(String garmentId) {
        assertThatThrownBy(() -> service.inspect(owner, garmentId)).isInstanceOf(GarmentNotFoundException.class);
    }

    @Test
    void theOwnersRemovedGarmentIsNoLongerCurrent() {
        Garment removed = stored(owner, CREATED.plusSeconds(60));

        assertThatThrownBy(() -> service.inspect(owner, removed.id().toString()))
                .isInstanceOf(GarmentNoLongerCurrentException.class);
    }

    private Garment stored(UUID garmentOwner, Instant removedAt) {
        Garment garment = new Garment(UUID.randomUUID(), garmentOwner,
                GarmentProfile.minimum(GarmentCategory.PANTS, GarmentColor.KHAKI), ValueProvenance.USER_ENTERED,
                CREATED, removedAt);
        garments.add(garment);
        return garment;
    }
}
