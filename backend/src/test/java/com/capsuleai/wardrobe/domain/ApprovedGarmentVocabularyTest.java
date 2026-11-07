package com.capsuleai.wardrobe.domain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares every Wardrobe vocabulary with the Product Owner-approved
 * garment-property authority itself, so a typo, a missing value or an invented
 * value fails the build (BRULE-GAR-004/005, SRS §4.10).
 */
class ApprovedGarmentVocabularyTest {

    private static final Path AUTHORITY = Path.of("..", "Initial files", "Image to Properties Extraction.md");

    private static List<String> authorityLines;

    @BeforeAll
    static void readAuthority() throws IOException {
        assertThat(AUTHORITY).as("garment-property authority location").isRegularFile();
        authorityLines = Files.readAllLines(AUTHORITY, StandardCharsets.UTF_8);
    }

    @Test
    void theEightCategoriesMatchTheApprovedIdentifiersAndLabels() {
        Map<String, String> approved = categoryRows().stream()
                .collect(Collectors.toMap(row -> unquote(row.get(1)), row -> row.get(0)));

        assertThat(GarmentCategory.values()).hasSize(8);
        for (GarmentCategory category : GarmentCategory.values()) {
            assertThat(approved).containsEntry(category.name(), category.label());
        }
    }

    @ParameterizedTest
    @EnumSource(GarmentCategory.class)
    void eachCategoryHasExactlyItsApprovedSubtypesInOrder(GarmentCategory category) {
        List<String> approved = categoryRows().stream()
                .filter(row -> unquote(row.get(1)).equals(category.name()))
                .map(row -> values(row.get(2)))
                .findFirst()
                .orElseThrow();

        assertThat(Arrays.stream(GarmentSubtype.values())
                .filter(subtype -> subtype.category() == category)
                .map(GarmentSubtype::label))
                .containsExactlyElementsOf(approved);
    }

    @Test
    void thereAre84SubtypesAndTheTenDressIdentifiersAreTheApprovedOnes() {
        assertThat(GarmentSubtype.values()).hasSize(84);
        List<String> approvedDressIdentifiers = authorityLines.stream()
                .filter(line -> line.matches("\\| [A-Za-z -]+ \\| `[A-Z_]+` \\|"))
                .map(line -> unquote(cells(line).get(1)))
                .toList();

        assertThat(approvedDressIdentifiers).hasSize(10);
        assertThat(Arrays.stream(GarmentSubtype.values())
                .filter(subtype -> subtype.category() == GarmentCategory.DRESSES_JUMPSUITS)
                .map(Enum::name))
                .containsExactlyElementsOf(approvedDressIdentifiers);
    }

    @Test
    void globalVocabulariesMatchTheApprovedListsExactly() {
        assertVocabulary(GarmentColor.values(), globalRow("Color"), 34);
        assertVocabulary(GarmentMaterial.values(), globalRow("Material"), 28);
        assertVocabulary(GarmentPattern.values(), globalRow("Pattern"), 24);
        assertVocabulary(GarmentStyle.values(), globalRow("Style"), 17);
    }

    @Test
    void categorySpecificVocabulariesMatchTheApprovedListsExactly() {
        assertVocabulary(GarmentFit.values(), specificRow("Fit", "Tops"), 4);
        assertVocabulary(GarmentNeckline.values(), specificRow("Neckline", "Tops"), 23);
        assertVocabulary(GarmentSleeveLength.values(), specificRow("Sleeve Length", "Tops"), 5);
        assertVocabulary(GarmentSilhouette.values(), specificRow("Silhouette", "Pants"), 7);
        assertThat(lengthLabels(GarmentCategory.TOPS)).containsExactlyElementsOf(specificRow("Length", "Tops"));
        assertThat(lengthLabels(GarmentCategory.PANTS)).containsExactlyElementsOf(specificRow("Length", "Pants"));
        assertThat(GarmentLength.values()).hasSize(6);
    }

    @Test
    void etcIsNotAStyleAndNoUnknownFallbackWasInvented() {
        assertThat(Arrays.stream(GarmentStyle.values()).map(GarmentStyle::label)).doesNotContain("Etc");
        Stream.of(GarmentSubtype.values(), GarmentColor.values(), GarmentPattern.values(), GarmentStyle.values())
                .flatMap(Arrays::stream)
                .map(Enum::name)
                .forEach(code -> assertThat(code).isNotIn("UNKNOWN", "OTHER_SUBTYPE", "NONE"));
    }

    @Test
    void categorySpecificPropertiesApplyOnlyToTheirApprovedCategories() {
        for (GarmentCategory category : GarmentCategory.values()) {
            assertThat(category.accepts(CategorySpecificProperty.FIT)).isEqualTo(category == GarmentCategory.TOPS);
            assertThat(category.accepts(CategorySpecificProperty.NECKLINE)).isEqualTo(category == GarmentCategory.TOPS);
            assertThat(category.accepts(CategorySpecificProperty.SLEEVE_LENGTH))
                    .isEqualTo(category == GarmentCategory.TOPS);
            assertThat(category.accepts(CategorySpecificProperty.LENGTH))
                    .isEqualTo(category == GarmentCategory.TOPS || category == GarmentCategory.PANTS);
            assertThat(category.accepts(CategorySpecificProperty.SILHOUETTE))
                    .isEqualTo(category == GarmentCategory.PANTS);
        }
    }

    private static <E extends ControlledValue> void assertVocabulary(E[] values, List<String> approved, int count) {
        assertThat(Arrays.stream(values).map(ControlledValue::label)).containsExactlyElementsOf(approved);
        assertThat(values).hasSize(count);
    }

    private static List<String> lengthLabels(GarmentCategory category) {
        return Arrays.stream(GarmentLength.values())
                .filter(length -> length.category() == category)
                .map(GarmentLength::label)
                .toList();
    }

    /** Rows of the Section 3.1 table: display name, `IDENTIFIER`, comma-separated subtypes. */
    private static List<List<String>> categoryRows() {
        return authorityLines.stream()
                .filter(line -> line.matches("\\| [A-Za-z/]+ \\| `[A-Z_]+` \\| .+ \\|"))
                .map(ApprovedGarmentVocabularyTest::cells)
                .toList();
    }

    private static List<String> globalRow(String property) {
        return rowValues(cells -> cells.size() == 2 && cells.get(0).equals(property), 1);
    }

    private static List<String> specificRow(String property, String category) {
        return rowValues(cells -> cells.size() == 3 && cells.get(0).equals(property) && cells.get(1).equals(category), 2);
    }

    private static List<String> rowValues(Predicate<List<String>> rowMatches, int valueColumn) {
        return authorityLines.stream()
                .filter(line -> line.startsWith("| "))
                .map(ApprovedGarmentVocabularyTest::cells)
                .filter(rowMatches)
                .map(cells -> values(cells.get(valueColumn)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Row not found in the authority"));
    }

    private static List<String> cells(String tableLine) {
        String inner = tableLine.substring(1, tableLine.length() - 1);
        return Arrays.stream(inner.split("\\|")).map(String::strip).toList();
    }

    private static List<String> values(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(",")).map(String::strip).toList();
    }

    private static String unquote(String identifier) {
        return identifier.replace("`", "");
    }
}
