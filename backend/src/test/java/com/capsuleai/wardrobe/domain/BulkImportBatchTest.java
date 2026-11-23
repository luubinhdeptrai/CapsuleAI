package com.capsuleai.wardrobe.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-BULK-001 (OPEN-S4-26): batch lifetime and stop, and the item state machine. */
class BulkImportBatchTest {

    private static final Instant NOW = Instant.parse("2026-11-16T09:00:00Z");
    private static final Duration LIFETIME = Duration.ofDays(7);

    @Test
    void aNewBatchIsOpenUntilItsLifetimeEndsAndThenExpired() {
        BulkImportBatch batch = newBatch();

        assertThat(batch.statusAt(NOW)).isEqualTo(BulkImportBatchStatus.OPEN);
        assertThat(batch.statusAt(NOW.plus(LIFETIME).minusNanos(1_000))).isEqualTo(BulkImportBatchStatus.OPEN);
        assertThat(batch.statusAt(NOW.plus(LIFETIME))).isEqualTo(BulkImportBatchStatus.EXPIRED);
        assertThat(batch.acceptsItemWorkAt(NOW.plus(LIFETIME))).isFalse();
    }

    @Test
    void stoppingIsFinalAndKeepsTheFirstStopTime() {
        BulkImportBatch stopped = newBatch().stop(NOW.plusSeconds(60));

        assertThat(stopped.statusAt(NOW.plusSeconds(61))).isEqualTo(BulkImportBatchStatus.STOPPED);
        assertThat(stopped.acceptsItemWorkAt(NOW.plusSeconds(61))).isFalse();
        assertThat(stopped.stop(NOW.plusSeconds(120)).stoppedAt()).contains(NOW.plusSeconds(60));
        assertThatThrownBy(() -> new BulkImportBatch(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1,
                BulkImportBatchStatus.EXPIRED, NOW, NOW.plus(LIFETIME), Optional.empty()))
                .as("EXPIRED is derived, never stored").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anItemTakesAPhotoAndIsAcceptedOnceWithEveryStepARevision() {
        UUID firstPhoto = UUID.randomUUID();
        UUID secondPhoto = UUID.randomUUID();
        UUID garment = UUID.randomUUID();
        BulkImportItem item = newItem();

        BulkImportItem linked = item.withPhoto(firstPhoto, NOW);
        BulkImportItem relinked = linked.withPhoto(secondPhoto, NOW);
        BulkImportItem accepted = relinked.accepted(garment, NOW.plusSeconds(1));

        assertThat(List.of(item, linked, relinked, accepted)).extracting(BulkImportItem::revision)
                .containsExactly(1L, 2L, 3L, 4L);
        assertThat(relinked.status()).isEqualTo(BulkImportItemStatus.REVIEWABLE);
        assertThat(relinked.photoUploadId()).contains(secondPhoto);
        assertThat(accepted.status()).isEqualTo(BulkImportItemStatus.ACCEPTED);
        assertThat(accepted.photoUploadId()).as("the stage now belongs to the garment").isEmpty();
        assertThat(accepted.garmentId()).contains(garment);
        assertThat(accepted.isFinished()).isTrue();
    }

    @Test
    void onlyAReviewableItemCanBeAcceptedAndAFinishedItemNeverChanges() {
        BulkImportItem awaiting = newItem();
        BulkImportItem canceled = awaiting.withPhoto(UUID.randomUUID(), NOW).canceled(NOW);
        BulkImportItem accepted = awaiting.withPhoto(UUID.randomUUID(), NOW).accepted(UUID.randomUUID(), NOW);

        assertThatThrownBy(() -> awaiting.accepted(UUID.randomUUID(), NOW)).isInstanceOf(IllegalStateException.class);
        assertThat(canceled.status()).isEqualTo(BulkImportItemStatus.CANCELED);
        assertThat(canceled.photoUploadId()).isEmpty();
        assertThatThrownBy(() -> canceled.withPhoto(UUID.randomUUID(), NOW)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> canceled.canceled(NOW)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> accepted.canceled(NOW)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> accepted.withPhoto(UUID.randomUUID(), NOW)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theItemInvariantsMatchTheDatabaseChecks() {
        UUID batch = UUID.randomUUID();

        assertThatThrownBy(() -> new BulkImportItem(UUID.randomUUID(), batch, 1, UUID.randomUUID(),
                BulkImportItemStatus.REVIEWABLE, Optional.empty(), Optional.empty(), 2, NOW))
                .as("reviewable without photo").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BulkImportItem(UUID.randomUUID(), batch, 1, UUID.randomUUID(),
                BulkImportItemStatus.ACCEPTED, Optional.empty(), Optional.empty(), 3, NOW))
                .as("accepted without garment").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BulkImportItem(UUID.randomUUID(), batch, 1, UUID.randomUUID(),
                BulkImportItemStatus.AWAITING_PHOTO, Optional.of(UUID.randomUUID()), Optional.empty(), 1, NOW))
                .as("awaiting with photo").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BulkImportItem(UUID.randomUUID(), batch, 0, UUID.randomUUID(),
                BulkImportItemStatus.AWAITING_PHOTO, Optional.empty(), Optional.empty(), 1, NOW))
                .as("positions start at 1").isInstanceOf(IllegalArgumentException.class);
    }

    private static BulkImportBatch newBatch() {
        return BulkImportBatch.open(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 3, NOW, LIFETIME);
    }

    private static BulkImportItem newItem() {
        return BulkImportItem.awaitingPhoto(UUID.randomUUID(), UUID.randomUUID(), 1, UUID.randomUUID(), NOW);
    }
}
