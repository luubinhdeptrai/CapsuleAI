package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentFit;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-WAR-003 (OPEN-S4-11 as refined on 2026-11-17): the Vietnamese alias table is complete, well formed and
 * identical to its canonical requirements document, so neither can change without the other.
 */
class VietnameseDescriptorAliasesTest {

    private static final Path CANONICAL_TABLE =
            Path.of("..", "docs", "03-requirements", "vocabularies", "vietnamese-descriptor-search-aliases.md");

    private static final Pattern VOCABULARY_HEADING =
            Pattern.compile("^## \\d+\\. (Category|Subtype|Color|Material|Pattern|Style)$");

    /** The six searchable vocabularies, in the order of the canonical document. */
    private static final Map<String, ControlledValue[]> SEARCHABLE_VOCABULARIES = searchableVocabularies();

    /** One documented row: the code, the approved English label and the aliases, search label first. */
    private record DocumentedValue(String code, String englishLabel, List<String> aliases) {
    }

    @Test
    void everyValueOfTheSixSearchableVocabulariesHasAVietnameseSearchLabel() {
        int values = 0;
        for (ControlledValue[] vocabulary : SEARCHABLE_VOCABULARIES.values()) {
            for (ControlledValue value : vocabulary) {
                assertThat(VietnameseDescriptorAliases.aliasesOf(value)).as(value.name()).isNotEmpty();
                values++;
            }
        }
        assertThat(values).as("8 + 84 + 34 + 28 + 24 + 17 approved values").isEqualTo(195);
    }

    @Test
    void everyAliasIsDistinctLowerCaseNfcTextWithoutOuterSpaces() {
        for (ControlledValue[] vocabulary : SEARCHABLE_VOCABULARIES.values()) {
            for (ControlledValue value : vocabulary) {
                List<String> aliases = VietnameseDescriptorAliases.aliasesOf(value);
                assertThat(new HashSet<>(aliases)).as("%s has no repeated alias", value.name()).hasSameSizeAs(aliases);
                for (String alias : aliases) {
                    assertThat(alias).as(value.name()).isNotBlank().isEqualTo(alias.strip())
                            .isEqualTo(alias.toLowerCase(Locale.ROOT))
                            .isEqualTo(Normalizer.normalize(alias, Normalizer.Form.NFC));
                }
            }
        }
    }

    @Test
    void theTableIsIdenticalToItsCanonicalRequirementsDocument() throws IOException {
        Map<String, List<DocumentedValue>> documented = documentedTable();

        assertThat(documented.keySet()).containsExactlyElementsOf(SEARCHABLE_VOCABULARIES.keySet());
        SEARCHABLE_VOCABULARIES.forEach((vocabulary, values) -> {
            List<DocumentedValue> implemented = Arrays.stream(values)
                    .map(value -> new DocumentedValue(value.name(), value.label(),
                            VietnameseDescriptorAliases.aliasesOf(value)))
                    .toList();
            assertThat(documented.get(vocabulary)).as(vocabulary).containsExactlyElementsOf(implemented);
        });
    }

    @Test
    void eachAliasIsAlsoSearchableInItsOtherStandardTonePlacement() {
        assertThat(VietnameseDescriptorAliases.withOtherTonePlacement("họa tiết hoa")).isEqualTo("hoạ tiết hoa");
        assertThat(VietnameseDescriptorAliases.withOtherTonePlacement("khoá kéo")).isEqualTo("khóa kéo");
        assertThat(VietnameseDescriptorAliases.withOtherTonePlacement("thủy")).isEqualTo("thuỷ");
        assertThat(VietnameseDescriptorAliases.withOtherTonePlacement("quý")).as("qu is a consonant").isEqualTo("quý");
        assertThat(VietnameseDescriptorAliases.withOtherTonePlacement("áo khoác")).as("closed syllable unchanged")
                .isEqualTo("áo khoác");

        assertThat(VietnameseDescriptorAliases.searchSpellingsOf(GarmentPattern.FLORAL))
                .contains("họa tiết hoa", "hoạ tiết hoa", "hoa");
        assertThat(VietnameseDescriptorAliases.searchSpellingsOf(GarmentColor.WHITE))
                .as("an alias without such a syllable has one spelling").containsExactly("trắng", "màu trắng");
    }

    @Test
    void aVocabularyThatIsNotSearchedHasNoAliases() {
        assertThatThrownBy(() -> VietnameseDescriptorAliases.aliasesOf(GarmentFit.SLIM))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VietnameseDescriptorAliases.searchSpellingsOf(GarmentFit.SLIM))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Map<String, ControlledValue[]> searchableVocabularies() {
        Map<String, ControlledValue[]> vocabularies = new LinkedHashMap<>();
        vocabularies.put("Category", GarmentCategory.values());
        vocabularies.put("Subtype", GarmentSubtype.values());
        vocabularies.put("Color", GarmentColor.values());
        vocabularies.put("Material", GarmentMaterial.values());
        vocabularies.put("Pattern", GarmentPattern.values());
        vocabularies.put("Style", GarmentStyle.values());
        return vocabularies;
    }

    /**
     * The rows of the canonical document: inside each "## n. Vocabulary" section, every table row whose first
     * cell is a code in backticks. Further aliases are separated by "; ", and "—" means none.
     */
    private static Map<String, List<DocumentedValue>> documentedTable() throws IOException {
        assertThat(CANONICAL_TABLE).as("canonical Vietnamese alias table").isRegularFile();
        Map<String, List<DocumentedValue>> table = new LinkedHashMap<>();
        String vocabulary = null;
        for (String line : Files.readAllLines(CANONICAL_TABLE, StandardCharsets.UTF_8)) {
            if (line.startsWith("## ")) {
                Matcher heading = VOCABULARY_HEADING.matcher(line);
                vocabulary = heading.matches() ? heading.group(1) : null;
            } else if (vocabulary != null && line.startsWith("| `")) {
                List<String> cells = Arrays.stream(line.substring(1, line.length() - 1).split("\\|"))
                        .map(String::strip)
                        .toList();
                List<String> aliases = new ArrayList<>();
                aliases.add(cells.get(2));
                if (!cells.get(3).equals("—")) {
                    aliases.addAll(Arrays.asList(cells.get(3).split("; ")));
                }
                table.computeIfAbsent(vocabulary, ignored -> new ArrayList<>())
                        .add(new DocumentedValue(cells.get(0).replace("`", ""), cells.get(1), List.copyOf(aliases)));
            }
        }
        return table;
    }
}
