package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-PROF-001 (S3-PROF-02): canonical vocabularies, the need mapping, priorities and absent versus zero. */
class DeclaredPreferencesTest {

    @Test
    void theProfileStylesAreExactlyTheSevenSrsCodes() {
        assertThat(Arrays.stream(ProfileStyle.values()).map(Enum::name)).containsExactly(
                "MINIMAL", "CASUAL", "SMART_CASUAL", "CLASSIC", "STREETWEAR", "SPORTY", "FORMAL");
        assertThat(ProfileStyle.fromCode("STREET")).as("a garment Style code").isEmpty();
        assertThat(ProfileStyle.fromCode("MINIMALIST")).as("a garment Style code").isEmpty();
        assertThat(ProfileStyle.fromCode("minimal")).as("no case folding").isEmpty();
    }

    @Test
    void everyNeedMapsToItsSrsOccasionCodes() {
        assertThat(CommonNeed.EVERYDAY_CASUAL.relevantOccasionCodes()).containsExactly("EVERYDAY", "DATE", "SOCIAL_EVENT");
        assertThat(CommonNeed.WORK_PROFESSIONAL.relevantOccasionCodes()).containsExactly("WORK");
        assertThat(CommonNeed.SCHOOL_UNIVERSITY.relevantOccasionCodes()).containsExactly("SCHOOL_UNIVERSITY");
        assertThat(CommonNeed.FORMAL_SPECIAL_EVENT.relevantOccasionCodes()).containsExactly("FORMAL_EVENT");
        assertThat(CommonNeed.TRAVEL.relevantOccasionCodes()).containsExactly("TRAVEL");
        assertThat(CommonNeed.SPORT_ACTIVE.relevantOccasionCodes()).containsExactly("SPORT_ACTIVITY");
        assertThat(CommonNeed.values()).hasSize(6);
    }

    @Test
    void prioritiesAreNotRelevantZeroToHighThreeAndNothingElse() {
        assertThat(NeedPriority.fromWeight(0)).contains(NeedPriority.NOT_RELEVANT);
        assertThat(NeedPriority.fromWeight(1)).contains(NeedPriority.LOW);
        assertThat(NeedPriority.fromWeight(2)).contains(NeedPriority.MEDIUM);
        assertThat(NeedPriority.fromWeight(3)).contains(NeedPriority.HIGH);
        assertThat(NeedPriority.fromWeight(-1)).isEmpty();
        assertThat(NeedPriority.fromWeight(4)).isEmpty();
    }

    @Test
    void anUnsetNeedIsDistinctFromANotRelevantOne() {
        DeclaredPreferences preferences = new DeclaredPreferences(Set.of(),
                Map.of(CommonNeed.TRAVEL, NeedPriority.NOT_RELEVANT));

        assertThat(preferences.priorityOf(CommonNeed.TRAVEL)).contains(NeedPriority.NOT_RELEVANT);
        assertThat(preferences.priorityOf(CommonNeed.WORK_PROFESSIONAL)).isEmpty();
        assertThat(preferences.declaresNothing()).isFalse();
        assertThat(DeclaredPreferences.none().declaresNothing()).isTrue();
    }

    @Test
    void preferencesAreImmutableCopiesInCatalogueOrder() {
        EnumSet<ProfileStyle> styles = EnumSet.of(ProfileStyle.FORMAL, ProfileStyle.MINIMAL);
        EnumMap<CommonNeed, NeedPriority> needs = new EnumMap<>(Map.of(CommonNeed.WORK_PROFESSIONAL, NeedPriority.HIGH));
        DeclaredPreferences preferences = new DeclaredPreferences(styles, needs);

        styles.add(ProfileStyle.CASUAL);
        needs.put(CommonNeed.TRAVEL, NeedPriority.LOW);

        assertThat(List.copyOf(preferences.styles())).containsExactly(ProfileStyle.MINIMAL, ProfileStyle.FORMAL);
        assertThat(preferences.commonNeeds()).containsOnlyKeys(CommonNeed.WORK_PROFESSIONAL);
    }
}
