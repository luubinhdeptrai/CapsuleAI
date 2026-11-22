package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-WAR-003 (OPEN-S4-11/12/14): text normalization and limits, and the condition fingerprint and its policy. */
class WardrobeSearchCriteriaTest {

    @Test
    void aMissingOrBlankTextMeansNoTextCondition() {
        assertThat(WardrobeSearchCriteria.normalizeText(null)).isEmpty();
        assertThat(WardrobeSearchCriteria.normalizeText("")).isEmpty();
        assertThat(WardrobeSearchCriteria.normalizeText("    ")).isEmpty();
    }

    @Test
    void textIsStrippedAndComposedToNfc() {
        String decomposed = "  Đầm  ";

        assertThat(WardrobeSearchCriteria.normalizeText(decomposed)).contains("Đầm");
    }

    @Test
    void textLongerThanOneHundredCodePointsIsTooLong() {
        assertThat(WardrobeSearchCriteria.problemOf("a".repeat(100))).isEmpty();
        assertThat(WardrobeSearchCriteria.problemOf("😀".repeat(100))).as("code points, not chars").isEmpty();
        assertThat(WardrobeSearchCriteria.problemOf("a".repeat(101)))
                .contains(WardrobeSearchCriteria.TextProblem.TOO_LONG);
    }

    @Test
    void controlCharactersAreRejected() {
        assertThat(WardrobeSearchCriteria.problemOf("a\u0000b"))
                .contains(WardrobeSearchCriteria.TextProblem.CONTROL_CHARACTER);
        assertThat(WardrobeSearchCriteria.problemOf("line\nbreak"))
                .contains(WardrobeSearchCriteria.TextProblem.CONTROL_CHARACTER);
    }

    @Test
    void equivalentConditionsShareOneFingerprintAndDifferentOnesDoNot() {
        WardrobeSearchCriteria original = new WardrobeSearchCriteria(Optional.of("Navy"),
                Set.of(GarmentCategory.TOPS, GarmentCategory.PANTS), Set.of(GarmentColor.NAVY));
        WardrobeSearchCriteria reordered = new WardrobeSearchCriteria(Optional.of("navy"),
                Set.of(GarmentCategory.PANTS, GarmentCategory.TOPS), Set.of(GarmentColor.NAVY));
        WardrobeSearchCriteria otherColor = new WardrobeSearchCriteria(Optional.of("navy"),
                Set.of(GarmentCategory.PANTS, GarmentCategory.TOPS), Set.of(GarmentColor.BLACK));

        assertThat(original.fingerprint()).matches("[0-9a-f]{16}").isEqualTo(reordered.fingerprint());
        assertThat(otherColor.fingerprint()).isNotEqualTo(original.fingerprint());
    }

    @Test
    void onlyTheAbsenceOfEveryConditionIsUnrestrictedAndTextMustBeNormalized() {
        assertThat(WardrobeSearchCriteria.unrestricted().isUnrestricted()).isTrue();
        assertThat(new WardrobeSearchCriteria(Optional.empty(), Set.of(GarmentCategory.SHOES), Set.of())
                .isUnrestricted()).isFalse();
        assertThatThrownBy(() -> new WardrobeSearchCriteria(Optional.of(" navy"), Set.of(), Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theFingerprintIsComputedUnderTheVietnameseAliasPolicyM2() throws Exception {
        WardrobeSearchCriteria criteria = new WardrobeSearchCriteria(Optional.of("Áo Thun"),
                Set.of(GarmentCategory.TOPS), Set.of(GarmentColor.WHITE));

        assertThat(WardrobeSearchCriteria.MATCHING_POLICY).isEqualTo("m2");
        assertThat(criteria.fingerprint()).isEqualTo(sha256Prefix("m2|q=áo thun|c=TOPS|k=WHITE"));
        assertThat(criteria.fingerprint()).as("a fingerprint of the English-only policy m1 never matches")
                .isNotEqualTo(sha256Prefix("m1|q=áo thun|c=TOPS|k=WHITE"));
    }

    static String sha256Prefix(String canonicalConditions) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(canonicalConditions.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest).substring(0, 16);
    }
}
