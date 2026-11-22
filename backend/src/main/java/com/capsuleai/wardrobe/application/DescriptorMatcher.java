package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.WardrobeSearchQuery.TextMatch;
import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * US-WAR-003 (S4-WAR-01; OPEN-S4-11 as refined on 2026-11-17): translates validated conditions into the query
 * the repository runs. A controlled value matches the text when its approved English label, its code or one
 * of its Vietnamese search spellings ({@link VietnameseDescriptorAliases}) contains the text. The comparison
 * is case-insensitive ({@link Locale#ROOT}, then NFC) and accent-sensitive. A text that names several values
 * matches all of them (OR). Matching happens here, against the vocabulary authority and the canonical alias
 * table, so it never depends on the database locale. Brand is matched literally by the repository.
 */
public final class DescriptorMatcher {

    private DescriptorMatcher() {
    }

    public static WardrobeSearchQuery queryFor(WardrobeSearchCriteria criteria) {
        Optional<TextMatch> textMatch = criteria.text().map(DescriptorMatcher::match);
        return new WardrobeSearchQuery(textMatch, criteria.categories(), criteria.colors());
    }

    static TextMatch match(String text) {
        String needle = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFC);
        return new TextMatch(
                matching(GarmentCategory.class, needle),
                matching(GarmentSubtype.class, needle),
                matching(GarmentColor.class, needle),
                matching(GarmentMaterial.class, needle),
                matching(GarmentPattern.class, needle),
                matching(GarmentStyle.class, needle),
                text);
    }

    private static <E extends Enum<E> & ControlledValue> Set<E> matching(Class<E> vocabulary, String needle) {
        Set<E> matches = EnumSet.noneOf(vocabulary);
        for (E value : vocabulary.getEnumConstants()) {
            if (matches(value, needle)) {
                matches.add(value);
            }
        }
        return matches;
    }

    /** OR across the approved English label, the code and every Vietnamese search spelling of the value. */
    private static boolean matches(ControlledValue value, String needle) {
        return value.label().toLowerCase(Locale.ROOT).contains(needle)
                || value.name().toLowerCase(Locale.ROOT).contains(needle)
                || VietnameseDescriptorAliases.searchSpellingsOf(value).stream()
                        .anyMatch(spelling -> spelling.contains(needle));
    }
}