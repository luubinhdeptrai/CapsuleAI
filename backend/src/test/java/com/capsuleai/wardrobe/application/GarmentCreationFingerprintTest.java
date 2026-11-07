package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The idempotency fingerprint compares the accepted Style set, not the order it was selected in (ADA-009). */
class GarmentCreationFingerprintTest {

    @Test
    void theOrderAndRepetitionOfSelectedStylesDoNotChangeTheFingerprint() {
        assertThat(GarmentCreationFingerprint.of(withStyles(List.of(GarmentStyle.STREET, GarmentStyle.CASUAL))))
                .isEqualTo(GarmentCreationFingerprint.of(withStyles(
                        List.of(GarmentStyle.CASUAL, GarmentStyle.STREET, GarmentStyle.CASUAL))));
    }

    @Test
    void aDifferentStyleSetIsADifferentGarment() {
        String casual = GarmentCreationFingerprint.of(withStyles(List.of(GarmentStyle.CASUAL)));

        assertThat(GarmentCreationFingerprint.of(withStyles(List.of()))).isNotEqualTo(casual);
        assertThat(GarmentCreationFingerprint.of(withStyles(List.of(GarmentStyle.CASUAL, GarmentStyle.STREET))))
                .isNotEqualTo(casual);
        assertThat(casual).matches("[0-9a-f]{64}");
    }

    private static GarmentProfile withStyles(List<GarmentStyle> styles) {
        return new GarmentProfile(GarmentCategory.BAGS, Optional.empty(), GarmentColor.BLACK, Set.of(),
                Optional.empty(), Set.copyOf(styles), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
