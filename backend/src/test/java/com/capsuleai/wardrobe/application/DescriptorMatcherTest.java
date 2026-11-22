package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.WardrobeSearchQuery.TextMatch;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-WAR-003 (S4-WAR-01; OPEN-S4-11 as refined on 2026-11-17): substring matching of the controlled
 * vocabularies through their English labels, their codes and their Vietnamese search aliases.
 */
class DescriptorMatcherTest {

    @Test
    void anApprovedLabelOrCodeContainingTheTextMatchesCaseInsensitively() {
        assertThat(DescriptorMatcher.match("SHIRT").subtypes())
                .contains(GarmentSubtype.T_SHIRTS, GarmentSubtype.LONG_SLEEVE_T_SHIRTS, GarmentSubtype.SHIRTS);
        assertThat(DescriptorMatcher.match("light-gray").colors()).as("label Light-Gray").contains(GarmentColor.LIGHT_GRAY);
        assertThat(DescriptorMatcher.match("light_gray").colors()).as("code LIGHT_GRAY").contains(GarmentColor.LIGHT_GRAY);
        assertThat(DescriptorMatcher.match("NaVy").colors()).containsExactly(GarmentColor.NAVY);
    }

    @Test
    void materialsAndStylesMatchThroughTheirOwnVocabularies() {
        TextMatch cotton = DescriptorMatcher.match("cotton");
        TextMatch street = DescriptorMatcher.match("street");

        assertThat(cotton.materials()).containsExactly(GarmentMaterial.COTTON);
        assertThat(cotton.categories()).isEmpty();
        assertThat(street.styles()).contains(GarmentStyle.STREET);
    }

    @Test
    void aTextMatchingNoControlledValueStillCarriesTheLiteralBrandTextWithItsAccents() {
        TextMatch brandOnly = DescriptorMatcher.match("Đầm Xinh");

        assertThat(brandOnly.categories()).isEmpty();
        assertThat(brandOnly.subtypes()).isEmpty();
        assertThat(brandOnly.colors()).isEmpty();
        assertThat(brandOnly.materials()).isEmpty();
        assertThat(brandOnly.patterns()).isEmpty();
        assertThat(brandOnly.styles()).isEmpty();
        assertThat(brandOnly.brandText()).isEqualTo("Đầm Xinh");
    }

    @Test
    void filtersPassThroughAndNoTextMeansNoTextCondition() {
        WardrobeSearchQuery query = DescriptorMatcher.queryFor(new WardrobeSearchCriteria(Optional.empty(),
                Set.of(GarmentCategory.TOPS), Set.of(GarmentColor.NAVY, GarmentColor.BLACK)));

        assertThat(query.textMatch()).isEmpty();
        assertThat(query.categories()).containsExactly(GarmentCategory.TOPS);
        assertThat(query.colors()).containsExactlyInAnyOrder(GarmentColor.NAVY, GarmentColor.BLACK);
    }

    @Test
    void vietnameseSearchLabelsAndAliasesMatchTheirCodes() {
        assertThat(DescriptorMatcher.match("áo thun").subtypes()).as("search label")
                .contains(GarmentSubtype.T_SHIRTS, GarmentSubtype.LONG_SLEEVE_T_SHIRTS);
        assertThat(DescriptorMatcher.match("áo phông").subtypes()).as("alias").contains(GarmentSubtype.T_SHIRTS);
        assertThat(DescriptorMatcher.match("trắng").colors()).contains(GarmentColor.WHITE);
        assertThat(DescriptorMatcher.match("đen").colors()).containsExactly(GarmentColor.BLACK);
        assertThat(DescriptorMatcher.match("chân váy").categories()).containsExactly(GarmentCategory.SKIRTS);
        assertThat(DescriptorMatcher.match("chân váy").subtypes()).containsExactlyInAnyOrder(
                GarmentSubtype.MINI_SKIRTS, GarmentSubtype.MIDI_SKIRTS, GarmentSubtype.MAXI_SKIRTS);
        assertThat(DescriptorMatcher.match("vải lanh").materials()).containsExactly(GarmentMaterial.LINEN);
        assertThat(DescriptorMatcher.match("chấm bi").patterns()).containsExactly(GarmentPattern.DOTTED);
        assertThat(DescriptorMatcher.match("tối giản").styles()).containsExactly(GarmentStyle.MINIMALIST);
    }

    @Test
    void vietnameseMatchingIgnoresLetterCaseButNotAccents() {
        assertThat(DescriptorMatcher.match("ÁO THUN").subtypes()).contains(GarmentSubtype.T_SHIRTS);
        assertThat(DescriptorMatcher.match("Trắng").colors()).contains(GarmentColor.WHITE);
        assertThat(DescriptorMatcher.match("ĐEN").colors()).containsExactly(GarmentColor.BLACK);

        TextMatch unaccented = DescriptorMatcher.match("ao thun");
        assertThat(unaccented.categories()).isEmpty();
        assertThat(unaccented.subtypes()).isEmpty();
        assertThat(unaccented.colors()).isEmpty();
        assertThat(unaccented.materials()).isEmpty();
        assertThat(unaccented.patterns()).isEmpty();
        assertThat(unaccented.styles()).isEmpty();
        assertThat(DescriptorMatcher.match("trang").colors()).as("trắng without accents").isEmpty();
    }

    @Test
    void anAmbiguousWordMatchesEveryValueItNamesAndBothTonePlacementsMatch() {
        assertThat(DescriptorMatcher.match("váy").categories())
                .containsExactlyInAnyOrder(GarmentCategory.DRESSES_JUMPSUITS, GarmentCategory.SKIRTS);
        assertThat(DescriptorMatcher.match("xanh").colors()).contains(GarmentColor.GREEN, GarmentColor.BLUE,
                GarmentColor.NAVY, GarmentColor.SKY_BLUE, GarmentColor.TEAL);
        assertThat(DescriptorMatcher.match("tím").colors())
                .containsExactlyInAnyOrder(GarmentColor.PURPLE, GarmentColor.LAVENDER, GarmentColor.NAVY);

        assertThat(DescriptorMatcher.match("họa tiết hoa").patterns()).containsExactly(GarmentPattern.FLORAL);
        assertThat(DescriptorMatcher.match("hoạ tiết hoa").patterns()).as("new-style tone placement")
                .containsExactly(GarmentPattern.FLORAL);
        assertThat(DescriptorMatcher.match("khoá kéo").subtypes()).containsExactly(GarmentSubtype.ZIP_UP_HOODIES);
    }
}
