package com.capsuleai.wardrobe.application;

import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.ValueProvenance;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddGarmentServiceTest {

    private final InMemoryGarmentRepository garments = new InMemoryGarmentRepository();
    private final ImmediateWardrobeTransactions transactions = new ImmediateWardrobeTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final AddGarmentService service = new AddGarmentService(garments, transactions, clock);

    private final UUID owner = UUID.randomUUID();

    @Test
    void aConfirmedEntryBecomesOneOwnedUserEnteredGarment() {
        GarmentCreation creation = service.add(command(owner, UUID.randomUUID().toString(), "SHOES", "BLACK"));

        Garment garment = creation.garment();
        assertThat(creation.newlyCreated()).isTrue();
        assertThat(garments.all()).containsExactly(garment);
        assertThat(garment.ownerAccountId()).isEqualTo(owner);
        assertThat(garment.profile().category()).isEqualTo(GarmentCategory.SHOES);
        assertThat(garment.profile().color()).isEqualTo(GarmentColor.BLACK);
        assertThat(garment.provenance()).isEqualTo(ValueProvenance.USER_ENTERED);
        assertThat(garment.createdAt()).isEqualTo(MutableClock.DEFAULT_START);
        assertThat(garment.isCurrent()).isTrue();
    }

    @Test
    void repeatingTheSameAdditionReturnsTheAcceptedGarmentWithoutASecondOne() {
        String key = UUID.randomUUID().toString();
        GarmentCreation first = service.add(command(owner, key, "SHOES", "BLACK"));

        GarmentCreation retry = service.add(command(owner, key, "SHOES", "BLACK"));

        assertThat(retry.newlyCreated()).isFalse();
        assertThat(retry.garment()).isEqualTo(first.garment());
        assertThat(garments.all()).hasSize(1);
    }

    @Test
    void twoSeparateAdditionsOfIdenticalGarmentsAreTwoGarments() {
        service.add(command(owner, UUID.randomUUID().toString(), "SHOES", "BLACK"));
        service.add(command(owner, UUID.randomUUID().toString(), "SHOES", "BLACK"));

        assertThat(garments.all()).hasSize(2);
    }

    @Test
    void reusingAKeyForDifferentValuesIsRejectedAndChangesNothing() {
        String key = UUID.randomUUID().toString();
        Garment accepted = service.add(command(owner, key, "SHOES", "BLACK")).garment();

        assertThatThrownBy(() -> service.add(command(owner, key, "SHOES", "WHITE")))
                .isInstanceOf(IdempotencyKeyReusedException.class);
        assertThat(garments.all()).containsExactly(accepted);
    }

    @Test
    void theSameKeyFromAnotherOwnerIsAnIndependentAddition() {
        String key = UUID.randomUUID().toString();
        service.add(command(owner, key, "SHOES", "BLACK"));

        GarmentCreation other = service.add(command(UUID.randomUUID(), key, "SHOES", "BLACK"));

        assertThat(other.newlyCreated()).isTrue();
        assertThat(garments.all()).hasSize(2);
    }

    @Test
    void anInvalidEntryIsRejectedBeforeAnyTransaction() {
        assertThatThrownBy(() -> service.add(command(owner, UUID.randomUUID().toString(), "TOPS", null)))
                .isInstanceOf(GarmentValidationException.class);

        assertThat(transactions.count()).isZero();
        assertThat(garments.all()).isEmpty();
    }

    @Test
    void aFailedCommitIsNotReportedAsACreatedGarment() {
        AddGarmentService failingCommit = new AddGarmentService(garments, new FailingCommit(), clock);

        assertThatThrownBy(() -> failingCommit.add(command(owner, UUID.randomUUID().toString(), "SHOES", "BLACK")))
                .isInstanceOf(IllegalStateException.class);
    }

    private static AddGarmentCommand command(UUID owner, String key, String category, String color) {
        return new AddGarmentCommand(owner, key, true, category, null, color, List.of(), null, null, null, null,
                null, null, null, null, null, null);
    }

    /** A transaction boundary whose commit fails, as when the database connection is lost. */
    private static final class FailingCommit implements WardrobeTransactions {

        @Override
        public <T> T inTransaction(Supplier<T> work) {
            work.get();
            throw new IllegalStateException("commit failed");
        }
    }
}
