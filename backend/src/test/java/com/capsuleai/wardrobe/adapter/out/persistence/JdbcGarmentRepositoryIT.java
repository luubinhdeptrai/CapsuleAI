package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentFit;
import com.capsuleai.wardrobe.domain.GarmentLength;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentNeckline;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentSilhouette;
import com.capsuleai.wardrobe.domain.GarmentSleeveLength;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import com.capsuleai.wardrobe.domain.PurchasePrice;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every approved vocabulary value survives a real PostgreSQL round trip exactly,
 * which also proves the V8 CHECK lists and the Java vocabularies agree.
 */
class JdbcGarmentRepositoryIT extends WardrobeIntegrationTestSupport {

    private static final Instant CREATED = Instant.parse("2026-01-05T08:00:00.123456Z");

    @Autowired
    private GarmentRepository garments;

    @Autowired
    private WardrobeTransactions transactions;

    private final UUID owner = UUID.randomUUID();

    @Test
    void everyCategoryWithItsMinimumRoundTrips() {
        for (GarmentCategory category : GarmentCategory.values()) {
            assertRoundTrip(GarmentProfile.minimum(category, GarmentColor.BLACK));
        }
    }

    @Test
    void everyApprovedSubtypeRoundTrips() {
        for (GarmentSubtype subtype : GarmentSubtype.values()) {
            assertRoundTrip(profile(subtype.category(), Optional.of(subtype), GarmentColor.WHITE, Set.of(),
                    Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()));
        }
    }

    @Test
    void everyColorPatternAndStyleRoundTrips() {
        for (GarmentColor color : GarmentColor.values()) {
            assertRoundTrip(GarmentProfile.minimum(GarmentCategory.BAGS, color));
        }
        for (GarmentPattern pattern : GarmentPattern.values()) {
            assertRoundTrip(profile(GarmentCategory.HEADWEAR, Optional.empty(), GarmentColor.RED, Set.of(),
                    Optional.of(pattern), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()));
        }
        for (GarmentStyle style : GarmentStyle.values()) {
            assertRoundTrip(profile(GarmentCategory.OUTERWEAR, Optional.empty(), GarmentColor.RED, Set.of(),
                    Optional.empty(), Set.of(style), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()));
        }
    }

    @Test
    void allTwentyEightMaterialsOnOneGarmentRoundTripInCatalogueOrder() {
        Garment stored = assertRoundTrip(profile(GarmentCategory.TOPS, Optional.empty(), GarmentColor.NAVY,
                EnumSet.allOf(GarmentMaterial.class), Optional.empty(), Set.of(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        assertThat(stored.profile().materials()).containsExactly(GarmentMaterial.values());
    }

    @Test
    void allSeventeenStylesOnOneGarmentRoundTripInCatalogueOrder() {
        Garment stored = assertRoundTrip(profile(GarmentCategory.DRESSES_JUMPSUITS, Optional.empty(),
                GarmentColor.BLACK, Set.of(GarmentMaterial.SILK), Optional.empty(), EnumSet.allOf(GarmentStyle.class),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        assertThat(stored.profile().styles()).containsExactly(GarmentStyle.values());
        assertThat(stored.profile().materials()).containsExactly(GarmentMaterial.SILK);
    }

    @Test
    void everyTopsDetailRoundTrips() {
        for (GarmentFit fit : GarmentFit.values()) {
            assertRoundTrip(tops(Optional.of(fit), Optional.empty(), Optional.empty(), Optional.empty()));
        }
        for (GarmentNeckline neckline : GarmentNeckline.values()) {
            assertRoundTrip(tops(Optional.empty(), Optional.of(neckline), Optional.empty(), Optional.empty()));
        }
        for (GarmentSleeveLength sleeveLength : GarmentSleeveLength.values()) {
            assertRoundTrip(tops(Optional.empty(), Optional.empty(), Optional.of(sleeveLength), Optional.empty()));
        }
    }

    @Test
    void everyLengthAndSilhouetteRoundTripsUnderItsCategory() {
        for (GarmentLength length : GarmentLength.values()) {
            assertRoundTrip(profile(length.category(), Optional.empty(), GarmentColor.BLUE, Set.of(),
                    Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(length), Optional.empty()));
        }
        for (GarmentSilhouette silhouette : GarmentSilhouette.values()) {
            assertRoundTrip(profile(GarmentCategory.PANTS, Optional.empty(), GarmentColor.BLUE, Set.of(),
                    Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(silhouette)));
        }
    }

    @Test
    void brandAndPriceAreKeptExactlyIncludingTrailingZerosAndDiacritics() {
        GarmentProfile withBrandAndPrice = new GarmentProfile(GarmentCategory.SHOES, Optional.empty(),
                GarmentColor.WHITE, Set.of(), Optional.empty(), Set.of(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Brand.optional(" Giày Việt "),
                Optional.of(PurchasePrice.of("12.50", "USD")));

        Garment stored = assertRoundTrip(withBrandAndPrice);

        assertThat(stored.profile().brand()).map(Brand::value).contains(" Giày Việt ");
        assertThat(stored.profile().price()).map(PurchasePrice::amountText).contains("12.50");
    }

    @Test
    void aZeroPriceIsStoredAsAKnownZeroNotAsAMissingPrice() {
        GarmentProfile gift = new GarmentProfile(GarmentCategory.HEADWEAR, Optional.empty(), GarmentColor.BEIGE,
                Set.of(), Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(PurchasePrice.of("0", "VND")));

        Garment stored = assertRoundTrip(gift);

        assertThat(stored.profile().price()).map(PurchasePrice::amountText).contains("0");
    }

    private Garment assertRoundTrip(GarmentProfile profile) {
        Garment garment = Garment.confirmManualEntry(UUID.randomUUID(), owner, profile,
                CREATED.truncatedTo(ChronoUnit.MICROS));
        UUID creationKey = UUID.randomUUID();
        boolean stored = transactions.inTransaction(
                () -> garments.addIfCreationKeyUnused(garment, creationKey, "a".repeat(64)));

        assertThat(stored).isTrue();
        Garment reread = garments.findCreation(owner, creationKey).orElseThrow().garment();
        assertThat(reread).isEqualTo(garment);
        return reread;
    }

    private static GarmentProfile tops(Optional<GarmentFit> fit, Optional<GarmentNeckline> neckline,
                                       Optional<GarmentSleeveLength> sleeveLength, Optional<GarmentLength> length) {
        return new GarmentProfile(GarmentCategory.TOPS, Optional.empty(), GarmentColor.NAVY, Set.of(),
                Optional.empty(), Set.of(), fit, neckline, sleeveLength, length, Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    private static GarmentProfile profile(GarmentCategory category, Optional<GarmentSubtype> subtype,
                                          GarmentColor color, Set<GarmentMaterial> materials,
                                          Optional<GarmentPattern> pattern, Set<GarmentStyle> styles,
                                          Optional<GarmentFit> fit, Optional<GarmentNeckline> neckline,
                                          Optional<GarmentSleeveLength> sleeveLength, Optional<GarmentLength> length,
                                          Optional<GarmentSilhouette> silhouette) {
        return new GarmentProfile(category, subtype, color, materials, pattern, styles, fit, neckline, sleeveLength,
                length, silhouette, Optional.empty(), Optional.empty());
    }
}
