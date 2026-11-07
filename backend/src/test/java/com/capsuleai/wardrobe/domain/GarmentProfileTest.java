package com.capsuleai.wardrobe.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarmentProfileTest {

    @Test
    void theMinimumHasOnlyCategoryAndColorAndNothingInvented() {
        GarmentProfile minimum = GarmentProfile.minimum(GarmentCategory.SHOES, GarmentColor.BLACK);

        assertThat(minimum.category()).isEqualTo(GarmentCategory.SHOES);
        assertThat(minimum.color()).isEqualTo(GarmentColor.BLACK);
        assertThat(minimum.materials()).isEmpty();
        assertThat(minimum.styles()).isEmpty();
        assertThat(List.of(minimum.subtype(), minimum.pattern(), minimum.fit(), minimum.neckline(),
                minimum.sleeveLength(), minimum.length(), minimum.silhouette(), minimum.brand(), minimum.price()))
                .allSatisfy(absent -> assertThat(absent).isEmpty());
    }

    @Test
    void materialsAreDistinctInCatalogueOrderAndCannotBeChangedAfterwards() {
        GarmentProfile profile = profile(GarmentCategory.TOPS, Optional.empty(),
                List.of(GarmentMaterial.SPANDEX_ELASTANE, GarmentMaterial.COTTON), List.of());

        assertThat(profile.materials()).containsExactly(GarmentMaterial.COTTON, GarmentMaterial.SPANDEX_ELASTANE);
        assertThatThrownBy(() -> profile.materials().add(GarmentMaterial.WOOL))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void stylesAreDistinctInCatalogueOrderAndCannotBeChangedAfterwards() {
        GarmentProfile profile = profile(GarmentCategory.BAGS, Optional.empty(),
                List.of(), List.of(GarmentStyle.STREET, GarmentStyle.CASUAL));

        assertThat(profile.styles()).containsExactly(GarmentStyle.CASUAL, GarmentStyle.STREET);
        assertThatThrownBy(() -> profile.styles().add(GarmentStyle.FORMAL))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void aSubtypeFromAnotherCategoryIsRefused() {
        assertThatThrownBy(() -> profile(GarmentCategory.PANTS, Optional.of(GarmentSubtype.T_SHIRTS), List.of(),
                List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void topsOnlyPropertiesAreRefusedOutsideTops() {
        assertThatThrownBy(() -> new GarmentProfile(GarmentCategory.DRESSES_JUMPSUITS, Optional.empty(),
                GarmentColor.RED, Set.of(), Optional.empty(), Set.of(), Optional.of(GarmentFit.SLIM),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lengthMustComeFromTheCategoryList() {
        assertThatThrownBy(() -> new GarmentProfile(GarmentCategory.PANTS, Optional.empty(), GarmentColor.BLUE,
                Set.of(), Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(GarmentLength.CROPPED), Optional.empty(), Optional.empty(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GarmentProfile(GarmentCategory.SKIRTS, Optional.empty(), GarmentColor.BLUE,
                Set.of(), Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(GarmentLength.LONG), Optional.empty(), Optional.empty(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void silhouetteIsOnlyForPants() {
        assertThatThrownBy(() -> new GarmentProfile(GarmentCategory.SKIRTS, Optional.empty(), GarmentColor.BLUE,
                Set.of(), Optional.empty(), Set.of(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(GarmentSilhouette.FLARE), Optional.empty(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static GarmentProfile profile(GarmentCategory category, Optional<GarmentSubtype> subtype,
                                          List<GarmentMaterial> materials, List<GarmentStyle> styles) {
        return new GarmentProfile(category, subtype, GarmentColor.NAVY, Set.copyOf(materials), Optional.empty(),
                Set.copyOf(styles), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }
}
