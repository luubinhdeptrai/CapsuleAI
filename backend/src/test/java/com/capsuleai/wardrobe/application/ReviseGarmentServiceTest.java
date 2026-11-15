package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentChanges.PriceInput;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentFit;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentSilhouette;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PurchasePrice;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-002 — presence-aware, validated, revision-protected garment edits (S3-GAR-01–05). */
class ReviseGarmentServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant CREATED = Instant.parse("2026-11-01T08:00:00Z");

    private final InMemoryGarmentRepository garments = new InMemoryGarmentRepository();
    private final ImmediateWardrobeTransactions transactions = new ImmediateWardrobeTransactions();
    private final ReviseGarmentService service = new ReviseGarmentService(garments, transactions);

    @Test
    void aConfirmedEditReplacesOnlyTheSubmittedPropertiesAndAdvancesTheRevision() {
        Garment tee = stored(whiteCottonTee());

        GarmentRevision revision = service.revise(command(tee, "1", new Edit()
                .set("color", FieldChange.replace("NAVY"))
                .set("pattern", FieldChange.replace("STRIPED"))));

        GarmentProfile profile = revision.garment().profile();
        assertThat(revision.changed()).isTrue();
        assertThat(revision.garment().revision()).isEqualTo(2L);
        assertThat(profile.color()).isEqualTo(GarmentColor.NAVY);
        assertThat(profile.pattern()).contains(GarmentPattern.STRIPED);
        assertThat(profile.subtype()).contains(GarmentSubtype.T_SHIRTS);
        assertThat(profile.materials()).containsExactly(GarmentMaterial.COTTON);
        assertThat(profile.styles()).containsExactly(GarmentStyle.CASUAL);
        assertThat(profile.fit()).contains(GarmentFit.REGULAR);
        assertThat(profile.brand()).contains(new Brand("Uniqlo"));
        assertThat(profile.price()).isEqualTo(whiteCottonTee().price());
        assertThat(garments.findOwned(OWNER, tee.id())).contains(revision.garment());
        assertThat(transactions.count()).isOne();
    }

    @Test
    void nullClearsOptionalPropertiesAndAnEmptyOrNullSetClearsMaterialsAndStyles() {
        Garment tee = stored(whiteCottonTee());

        GarmentProfile profile = service.revise(command(tee, "1", new Edit()
                .set("subtype", FieldChange.clear())
                .set("fit", FieldChange.clear())
                .set("brand", FieldChange.clear())
                .set("materials", FieldChange.replace(List.of()))
                .set("styles", FieldChange.clear()))).garment().profile();

        assertThat(profile.category()).isEqualTo(GarmentCategory.TOPS);
        assertThat(profile.color()).isEqualTo(GarmentColor.WHITE);
        assertThat(profile.subtype()).isEmpty();
        assertThat(profile.fit()).isEmpty();
        assertThat(profile.brand()).isEmpty();
        assertThat(profile.materials()).isEmpty();
        assertThat(profile.styles()).isEmpty();
        assertThat(profile.price()).as("untouched").isPresent();
    }

    @Test
    void priceIsReplacedOrClearedAsAWholeAndZeroIsAKnownAmount() {
        Garment tee = stored(whiteCottonTee());

        GarmentProfile zero = service.revise(command(tee, "1", new Edit()
                .set("price", FieldChange.replace(new PriceInput("0", "USD"))))).garment().profile();
        assertThat(zero.price()).hasValueSatisfying(price -> {
            assertThat(price.amount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(price.currencyCode()).isEqualTo("USD");
        });

        GarmentProfile cleared = service.revise(command(tee, "2", new Edit()
                .set("price", FieldChange.clear()))).garment().profile();
        assertThat(cleared.price()).isEmpty();
    }

    @Test
    void anIncompletePriceIsRejectedAndTheAcceptedProfileIsKept() {
        Garment tee = stored(whiteCottonTee());

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("price", FieldChange.replace(new PriceInput("10", null))))))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("price", GarmentValueProblem.INCOMPLETE_PRICE)));
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
    }

    @Test
    void categoryAndColorCanBeReplacedButNotCleared() {
        Garment tee = stored(whiteCottonTee());

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("category", FieldChange.clear())
                .set("color", FieldChange.clear()))))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("category", GarmentValueProblem.REQUIRED),
                                new GarmentFieldViolation("color", GarmentValueProblem.REQUIRED)));
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
    }

    @Test
    void anUnconfirmedEditChangesNothing() {
        Garment tee = stored(whiteCottonTee());
        Edit navy = new Edit().set("color", FieldChange.replace("NAVY"));

        assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(OWNER, tee.id().toString(), false, "1",
                navy.build())))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("confirmed", GarmentValueProblem.NOT_CONFIRMED)));
        assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(OWNER, tee.id().toString(), null, "1",
                navy.build())))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("confirmed", GarmentValueProblem.REQUIRED)));
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
        assertThat(transactions.count()).isZero();
    }

    @Test
    void aMissingOrMalformedExpectedRevisionIsRejectedBeforeAnyLookup() {
        String unknownGarment = UUID.randomUUID().toString();

        assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(OWNER, unknownGarment, true, null,
                GarmentChanges.none())))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("expectedRevision", GarmentValueProblem.REQUIRED)));
        for (String malformed : List.of("abc", "0", "01", "-1", "1.0", "1234567890123456789")) {
            assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(OWNER, unknownGarment, true, malformed,
                    GarmentChanges.none())))
                    .as(malformed)
                    .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                            assertThat(rejected.violations()).containsExactly(
                                    new GarmentFieldViolation("expectedRevision", GarmentValueProblem.INVALID_FORMAT)));
        }
    }

    @Test
    void aCategoryChangeThatLeavesInapplicableAcceptedValuesIsRejectedNotCleared() {
        Garment tee = stored(whiteCottonTee());

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("category", FieldChange.replace("PANTS")))))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("subtype", GarmentValueProblem.NOT_IN_CATEGORY),
                                new GarmentFieldViolation("fit", GarmentValueProblem.NOT_APPLICABLE)));
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
    }

    @Test
    void inapplicableValuesCorrectedOrClearedInTheSameEditAreAccepted() {
        Garment tee = stored(whiteCottonTee());

        GarmentProfile pants = service.revise(command(tee, "1", new Edit()
                .set("category", FieldChange.replace("PANTS"))
                .set("subtype", FieldChange.replace("JEANS"))
                .set("fit", FieldChange.clear())
                .set("silhouette", FieldChange.replace("WIDE_LEG")))).garment().profile();

        assertThat(pants.category()).isEqualTo(GarmentCategory.PANTS);
        assertThat(pants.subtype()).contains(GarmentSubtype.JEANS);
        assertThat(pants.fit()).isEmpty();
        assertThat(pants.silhouette()).contains(GarmentSilhouette.WIDE_LEG);
        assertThat(pants.length()).as("no length is derived from the subtype").isEmpty();
        assertThat(pants.materials()).containsExactly(GarmentMaterial.COTTON);
    }

    @Test
    void unsupportedValuesAndEtcAsAStyleAreRejectedWithoutChanges() {
        Garment tee = stored(whiteCottonTee());

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("color", FieldChange.replace("SKY"))
                .set("styles", FieldChange.replace(List.of("CASUAL", "ETC"))))))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("color", GarmentValueProblem.UNSUPPORTED_VALUE),
                                new GarmentFieldViolation("styles[1]", GarmentValueProblem.UNSUPPORTED_VALUE)));
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
    }

    @Test
    void anEditThatChangesNothingKeepsTheRevisionAndWritesNothing() {
        Garment tee = stored(whiteCottonTee());

        GarmentRevision empty = service.revise(command(tee, "1", new Edit()));
        GarmentRevision sameValues = service.revise(command(tee, "1", new Edit()
                .set("color", FieldChange.replace("WHITE"))
                .set("materials", FieldChange.replace(List.of("COTTON", "COTTON")))));

        assertThat(empty.changed()).isFalse();
        assertThat(sameValues.changed()).isFalse();
        assertThat(sameValues.garment()).isEqualTo(tee);
        assertThat(sameValues.garment().revision()).isEqualTo(1L);
        assertThat(transactions.count()).as("no write was attempted").isZero();
    }

    @Test
    void aStaleRevisionIsAConflictAndChangesNothing() {
        Garment tee = stored(whiteCottonTee());
        service.revise(command(tee, "1", new Edit().set("color", FieldChange.replace("RED"))));

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("color", FieldChange.replace("BLUE")))))
                .isInstanceOf(GarmentRevisionConflictException.class);
        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("color", FieldChange.replace("RED")))))
                .as("OPEN-S3-06: a stale precondition is a conflict even when nothing would change")
                .isInstanceOf(GarmentRevisionConflictException.class);

        Garment current = garments.findOwned(OWNER, tee.id()).orElseThrow();
        assertThat(current.profile().color()).isEqualTo(GarmentColor.RED);
        assertThat(current.revision()).isEqualTo(2L);
    }

    @Test
    void anotherOwnersUnknownOrMalformedGarmentIsNotFound() {
        Garment tee = stored(whiteCottonTee());
        GarmentChanges navy = new Edit().set("color", FieldChange.replace("NAVY")).build();

        assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(UUID.randomUUID(), tee.id().toString(),
                true, "1", navy))).isInstanceOf(GarmentNotFoundException.class);
        assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(OWNER, UUID.randomUUID().toString(),
                true, "1", navy))).isInstanceOf(GarmentNotFoundException.class);
        assertThatThrownBy(() -> service.revise(new ReviseGarmentCommand(OWNER, "not-a-garment",
                true, "1", navy))).isInstanceOf(GarmentNotFoundException.class);
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
    }

    @Test
    void aGarmentThatLeftTheCurrentWardrobeIsNoLongerCurrent() {
        Garment tee = stored(whiteCottonTee());
        garments.remove(tee.id(), CREATED.plusSeconds(30));

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("color", FieldChange.replace("NAVY")))))
                .isInstanceOf(GarmentNoLongerCurrentException.class);
    }

    @Test
    void aChangeBetweenReadingAndWritingIsExplainedFromTheStateAtWriteTime() {
        Garment editedElsewhere = stored(whiteCottonTee());
        Garment removedElsewhere = stored(GarmentProfile.minimum(GarmentCategory.SHOES, GarmentColor.BLACK));
        GarmentProfile competingEdit = GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.GREEN);
        RacingRepository racing = new RacingRepository(garments);
        ReviseGarmentService racingService = new ReviseGarmentService(racing, transactions);

        racing.beforeNextRevision(() -> garments.reviseIfCurrentAt(OWNER, editedElsewhere.id(), 1, competingEdit));
        assertThatThrownBy(() -> racingService.revise(command(editedElsewhere, "1", new Edit()
                .set("color", FieldChange.replace("NAVY")))))
                .isInstanceOf(GarmentRevisionConflictException.class);
        assertThat(garments.findOwned(OWNER, editedElsewhere.id()).orElseThrow().profile()).isEqualTo(competingEdit);

        racing.beforeNextRevision(() -> garments.remove(removedElsewhere.id(), CREATED.plusSeconds(5)));
        assertThatThrownBy(() -> racingService.revise(command(removedElsewhere, "1", new Edit()
                .set("color", FieldChange.replace("WHITE")))))
                .isInstanceOf(GarmentNoLongerCurrentException.class);
    }

    @Test
    void brandIsKeptExactlyAndAnOverlongBrandIsRejected() {
        Garment tee = stored(whiteCottonTee());

        GarmentProfile exact = service.revise(command(tee, "1", new Edit()
                .set("brand", FieldChange.replace("  Maison  Ébène ")))).garment().profile();
        assertThat(exact.brand()).contains(new Brand("  Maison  Ébène "));

        assertThatThrownBy(() -> service.revise(command(tee, "2", new Edit()
                .set("brand", FieldChange.replace("x".repeat(Brand.MAX_LENGTH + 1))))))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("brand", GarmentValueProblem.TOO_LONG)));
    }

    @Test
    void valuesOfTheWrongKindAreReportedPerFieldBeforeAnyChange() {
        Garment tee = stored(whiteCottonTee());

        assertThatThrownBy(() -> service.revise(command(tee, "1", new Edit()
                .set("subtype", FieldChange.malformed())
                .set("materials", FieldChange.malformed())
                .set("price", FieldChange.malformed()))))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("subtype", GarmentValueProblem.UNSUPPORTED_VALUE),
                                new GarmentFieldViolation("materials", GarmentValueProblem.UNSUPPORTED_VALUE),
                                new GarmentFieldViolation("price", GarmentValueProblem.UNSUPPORTED_VALUE)));
        assertThat(garments.findOwned(OWNER, tee.id())).contains(tee);
    }

    private Garment stored(GarmentProfile profile) {
        Garment garment = Garment.confirmManualEntry(UUID.randomUUID(), OWNER, profile, CREATED);
        garments.add(garment);
        return garment;
    }

    private static ReviseGarmentCommand command(Garment garment, String expectedRevision, Edit edit) {
        return new ReviseGarmentCommand(OWNER, garment.id().toString(), true, expectedRevision, edit.build());
    }

    private static GarmentProfile whiteCottonTee() {
        return new GarmentProfile(GarmentCategory.TOPS, Optional.of(GarmentSubtype.T_SHIRTS), GarmentColor.WHITE,
                Set.of(GarmentMaterial.COTTON), Optional.empty(), Set.of(GarmentStyle.CASUAL),
                Optional.of(GarmentFit.REGULAR), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(new Brand("Uniqlo")), Optional.of(PurchasePrice.of("199000", "VND")));
    }

    /** Builds GarmentChanges in which every property is kept unless set. */
    private static final class Edit {

        private final Map<String, FieldChange<?>> changes = new HashMap<>();

        Edit set(String field, FieldChange<?> change) {
            changes.put(field, change);
            return this;
        }

        GarmentChanges build() {
            return new GarmentChanges(get("category"), get("subtype"), get("color"), get("materials"),
                    get("pattern"), get("styles"), get("fit"), get("neckline"), get("sleeveLength"), get("length"),
                    get("silhouette"), get("brand"), get("price"));
        }

        @SuppressWarnings("unchecked")
        private <T> FieldChange<T> get(String field) {
            return (FieldChange<T>) changes.getOrDefault(field, FieldChange.keep());
        }
    }

    /** Runs a competing change after the service has read the garment and just before it writes. */
    private static final class RacingRepository implements GarmentRepository {

        private final InMemoryGarmentRepository delegate;
        private Runnable competingChange = () -> { };

        RacingRepository(InMemoryGarmentRepository delegate) {
            this.delegate = delegate;
        }

        void beforeNextRevision(Runnable change) {
            competingChange = change;
        }

        @Override
        public boolean reviseIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision,
                                         GarmentProfile revisedProfile) {
            Runnable change = competingChange;
            competingChange = () -> { };
            change.run();
            return delegate.reviseIfCurrentAt(ownerAccountId, garmentId, expectedRevision, revisedProfile);
        }

        @Override
        public boolean addIfCreationKeyUnused(Garment garment, UUID creationKey, String creationFingerprint) {
            return delegate.addIfCreationKeyUnused(garment, creationKey, creationFingerprint);
        }

        @Override
        public Optional<RecordedGarmentCreation> findCreation(UUID ownerAccountId, UUID creationKey) {
            return delegate.findCreation(ownerAccountId, creationKey);
        }

        @Override
        public List<Garment> findCurrentByOwner(UUID ownerAccountId, Optional<WardrobePosition> after,
                                                int maximumCount) {
            return delegate.findCurrentByOwner(ownerAccountId, after, maximumCount);
        }

        @Override
        public Optional<Garment> findOwned(UUID ownerAccountId, UUID garmentId) {
            return delegate.findOwned(ownerAccountId, garmentId);
        }
    }
}
