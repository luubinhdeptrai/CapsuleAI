package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-005 (S4-GAR-01, OPEN-S4-01/06): the confirmed, guarded and monotonic removal rules. */
class RemoveGarmentServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-11-01T08:00:00Z");
    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");
    private static final GarmentRemovalPolicy POLICY =
            new GarmentRemovalPolicy(Duration.ofDays(30), Duration.ofDays(21), 50, Duration.ofMinutes(15));

    private InMemoryGarmentRepository garmentRepository;
    private InMemoryGarmentRemovalRepository garmentRemovalRepository;
    private ImmediateWardrobeTransactions wardrobeTransactions;
    private RemoveGarmentService removeGarmentService;

    @BeforeEach
    void setUp() {
        garmentRepository = new InMemoryGarmentRepository();
        garmentRemovalRepository = new InMemoryGarmentRemovalRepository(garmentRepository);
        wardrobeTransactions = new ImmediateWardrobeTransactions();
        removeGarmentService = serviceAt(NOW);
    }

    @Test
    void aConfirmedRemovalOfTheReviewedRevisionRemovesItAndRecordsTheCleanupDeadline() {
        UUID garmentId = addCurrentGarment();

        GarmentRemoval removal = removeGarmentService.remove(command(garmentId.toString(), true, "1"));

        assertThat(removal.newlyRemoved()).isTrue();
        assertThat(removal.removedGarment().removedAt()).isEqualTo(NOW);
        assertThat(removal.removedGarment().category()).isEqualTo(GarmentCategory.TOPS);
        assertThat(garmentRepository.findOwned(OWNER, garmentId).orElseThrow().isCurrent()).isFalse();
        InMemoryGarmentRemovalRepository.Obligation obligation =
                garmentRemovalRepository.obligation(garmentId).orElseThrow();
        assertThat(obligation.removedAt).isEqualTo(NOW);
        assertThat(obligation.purgeDeadline).isEqualTo(NOW.plus(Duration.ofDays(30)));
        assertThat(obligation.attachedPhoto).isEmpty();
        assertThat(wardrobeTransactions.count()).isOne();
    }

    @Test
    void theAttachedPhotoIsRecordedForPhysicalDeletion() {
        UUID garmentId = addCurrentGarment();
        PhotoCleanupTarget photo = new PhotoCleanupTarget(UUID.randomUUID(), "photos/" + UUID.randomUUID() + ".jpg");
        garmentRemovalRepository.attachPhoto(garmentId, photo);

        removeGarmentService.remove(command(garmentId.toString(), true, "1"));

        assertThat(garmentRemovalRepository.obligation(garmentId).orElseThrow().attachedPhoto).contains(photo);
    }

    @Test
    void repeatingARemovalReportsAlreadyRemovedWithTheOriginalTimeAndWritesNothing() {
        UUID garmentId = addCurrentGarment();
        removeGarmentService.remove(command(garmentId.toString(), true, "1"));

        GarmentRemoval repeated = serviceAt(NOW.plus(Duration.ofMinutes(5)))
                .remove(command(garmentId.toString(), true, "1"));

        assertThat(repeated.newlyRemoved()).isFalse();
        assertThat(repeated.removedGarment().removedAt()).isEqualTo(NOW);
        assertThat(garmentRemovalRepository.obligationCount()).isOne();
        assertThat(wardrobeTransactions.count()).as("the repeat opens no transaction").isOne();
    }

    @Test
    void aStaleRevisionIsAConflictAndRemovesNothing() {
        UUID garmentId = addCurrentGarment();

        assertThatThrownBy(() -> removeGarmentService.remove(command(garmentId.toString(), true, "2")))
                .isInstanceOf(GarmentRevisionConflictException.class);

        assertThat(garmentRepository.findOwned(OWNER, garmentId).orElseThrow().isCurrent()).isTrue();
        assertThat(garmentRemovalRepository.obligationCount()).isZero();
    }

    @Test
    void anotherOwnersGarmentAndAnUnknownGarmentAreNotFound() {
        UUID garmentId = addCurrentGarment();

        assertThatThrownBy(() -> removeGarmentService.remove(
                new RemoveGarmentCommand(UUID.randomUUID(), garmentId.toString(), true, "1")))
                .isInstanceOf(GarmentNotFoundException.class);
        assertThatThrownBy(() -> removeGarmentService.remove(command(UUID.randomUUID().toString(), true, "1")))
                .isInstanceOf(GarmentNotFoundException.class);
        assertThat(garmentRepository.findOwned(OWNER, garmentId).orElseThrow().isCurrent()).isTrue();
    }

    @Test
    void anUnconfirmedOrRevisionlessRemovalIsRejectedBeforeAnyRead() {
        String unknownGarmentId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> removeGarmentService.remove(command(unknownGarmentId, null, null)))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("confirmed", GarmentValueProblem.REQUIRED),
                                new GarmentFieldViolation("expectedRevision", GarmentValueProblem.REQUIRED)));
        assertThatThrownBy(() -> removeGarmentService.remove(command(unknownGarmentId, false, "abc")))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("confirmed", GarmentValueProblem.NOT_CONFIRMED),
                                new GarmentFieldViolation("expectedRevision", GarmentValueProblem.INVALID_FORMAT)));
    }

    @Test
    void aMalformedGarmentIdIsNotFound() {
        assertThatThrownBy(() -> removeGarmentService.remove(command("not-a-garment-id", true, "1")))
                .isInstanceOf(GarmentNotFoundException.class);
    }

    @Test
    void aRemovalRefusedBecauseOfACompetingRemovalReportsThatEarlierRemoval() {
        UUID garmentId = addCurrentGarment();
        Instant competingRemovalTime = NOW.minusSeconds(1);
        garmentRemovalRepository.removeConcurrentlyBeforeNextAttempt(competingRemovalTime);

        GarmentRemoval removal = removeGarmentService.remove(command(garmentId.toString(), true, "1"));

        assertThat(removal.newlyRemoved()).isFalse();
        assertThat(removal.removedGarment().removedAt()).isEqualTo(competingRemovalTime);
        assertThat(garmentRemovalRepository.obligationCount())
                .as("the competing removal records its own obligation; this call records none")
                .isZero();
    }

    private RemoveGarmentService serviceAt(Instant instant) {
        return new RemoveGarmentService(garmentRepository, garmentRemovalRepository, wardrobeTransactions,
                Clock.fixed(instant, ZoneOffset.UTC), POLICY);
    }

    private UUID addCurrentGarment() {
        Garment garment = Garment.confirmManualEntry(UUID.randomUUID(), OWNER,
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.NAVY), CREATED_AT);
        garmentRepository.add(garment);
        return garment.id();
    }

    private static RemoveGarmentCommand command(String garmentId, Boolean confirmed, String expectedRevision) {
        return new RemoveGarmentCommand(OWNER, garmentId, confirmed, expectedRevision);
    }
}
