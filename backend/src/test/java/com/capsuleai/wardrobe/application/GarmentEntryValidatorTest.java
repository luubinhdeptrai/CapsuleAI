package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentLength;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarmentEntryValidatorTest {

    private static final String KEY = "6f1c2a8e-9b3d-4e5f-a1b2-c3d4e5f60718";

    @ParameterizedTest
    @EnumSource(GarmentCategory.class)
    void theConfirmedMinimumIsAcceptedForEveryCategory(GarmentCategory category) {
        ValidatedGarmentEntry entry = GarmentEntryValidator.validate(entry(category.name(), "BLACK", c -> c));

        assertThat(entry.creationKey()).isEqualTo(UUID.fromString(KEY));
        assertThat(entry.profile()).isEqualTo(GarmentProfile.minimum(category, GarmentColor.BLACK));
    }

    @ParameterizedTest
    @EnumSource(GarmentSubtype.class)
    void everyApprovedSubtypeIsAcceptedUnderItsOwnCategory(GarmentSubtype subtype) {
        GarmentProfile profile = GarmentEntryValidator.validate(entry(subtype.category().name(), "WHITE",
                c -> with(c, "subtype", subtype.name()))).profile();

        assertThat(profile.subtype()).contains(subtype);
    }

    @Test
    void aSubtypeFromAnotherCategoryIsIdentifiedNotReplaced() {
        assertViolations(entry("PANTS", "BLUE", c -> with(c, "subtype", "T_SHIRTS")),
                new GarmentFieldViolation("subtype", GarmentValueProblem.NOT_IN_CATEGORY));
    }

    @Test
    void missingCategoryColorAndConfirmationAreAllReported() {
        AddGarmentCommand incomplete = new AddGarmentCommand(UUID.randomUUID(), KEY, null, null, null, " ",
                null, null, null, null, null, null, null, null, null, null, null);

        assertViolations(incomplete,
                new GarmentFieldViolation("confirmed", GarmentValueProblem.REQUIRED),
                new GarmentFieldViolation("category", GarmentValueProblem.REQUIRED),
                new GarmentFieldViolation("color", GarmentValueProblem.REQUIRED));
    }

    @Test
    void anUnconfirmedEntryIsNotSaveable() {
        AddGarmentCommand draft = new AddGarmentCommand(UUID.randomUUID(), KEY, false, "TOPS", null, "RED",
                null, null, null, null, null, null, null, null, null, null, null);

        assertViolations(draft, new GarmentFieldViolation("confirmed", GarmentValueProblem.NOT_CONFIRMED));
    }

    @Test
    void unsupportedCodesAreRejectedIncludingEtcLowerCaseAndUnknown() {
        assertViolations(entry("TOPS", "red", c -> withStyles(with(c, "pattern", "UNKNOWN"), List.of("ETC"))),
                new GarmentFieldViolation("color", GarmentValueProblem.UNSUPPORTED_VALUE),
                new GarmentFieldViolation("pattern", GarmentValueProblem.UNSUPPORTED_VALUE),
                new GarmentFieldViolation("styles[0]", GarmentValueProblem.UNSUPPORTED_VALUE));
        assertViolations(entry("SOCKS", "RED", c -> c),
                new GarmentFieldViolation("category", GarmentValueProblem.UNSUPPORTED_VALUE));
    }

    @Test
    void colorfulIsTheOneListedValueForMulticoloredGarments() {
        assertThat(GarmentEntryValidator.validate(entry("BAGS", "COLORFUL", c -> c)).profile().color())
                .isEqualTo(GarmentColor.COLORFUL);
    }

    @Test
    void repeatedMaterialsAddNoSecondSemanticMaterial() {
        GarmentProfile profile = GarmentEntryValidator.validate(entry("TOPS", "WHITE",
                c -> withMaterials(c, List.of("LINEN", "COTTON", "LINEN")))).profile();

        assertThat(profile.materials()).containsExactly(GarmentMaterial.COTTON, GarmentMaterial.LINEN);
    }

    @Test
    void eachUnsupportedMaterialIsIdentifiedByItsPosition() {
        assertViolations(entry("TOPS", "WHITE", c -> withMaterials(c, Arrays.asList("COTTON", "PLASTIC", null))),
                new GarmentFieldViolation("materials[1]", GarmentValueProblem.UNSUPPORTED_VALUE),
                new GarmentFieldViolation("materials[2]", GarmentValueProblem.UNSUPPORTED_VALUE));
    }

    @Test
    void zeroOneOrSeveralStylesAreAcceptedAndRepeatedStylesCollapse() {
        assertThat(GarmentEntryValidator.validate(entry("SHOES", "WHITE", c -> c)).profile().styles()).isEmpty();
        assertThat(GarmentEntryValidator.validate(entry("SHOES", "WHITE", c -> withStyles(c, List.of())))
                .profile().styles()).isEmpty();
        assertThat(GarmentEntryValidator.validate(entry("SHOES", "WHITE", c -> withStyles(c, List.of("SPORTY"))))
                .profile().styles()).containsExactly(GarmentStyle.SPORTY);

        GarmentProfile profile = GarmentEntryValidator.validate(entry("DRESSES_JUMPSUITS", "RED",
                c -> withStyles(c, List.of("FEMININE", "CASUAL", "FEMININE")))).profile();

        assertThat(profile.styles()).containsExactly(GarmentStyle.CASUAL, GarmentStyle.FEMININE);
    }

    @ParameterizedTest
    @EnumSource(GarmentCategory.class)
    void severalStylesApplyToEveryCategory(GarmentCategory category) {
        GarmentProfile profile = GarmentEntryValidator.validate(entry(category.name(), "BLACK",
                c -> withStyles(c, List.of("STREET", "MINIMALIST")))).profile();

        assertThat(profile.styles()).containsExactly(GarmentStyle.MINIMALIST, GarmentStyle.STREET);
    }

    @Test
    void eachUnsupportedStyleIsIdentifiedByItsPosition() {
        assertViolations(entry("TOPS", "WHITE", c -> withStyles(c, Arrays.asList("CASUAL", "Etc", "casual", null))),
                new GarmentFieldViolation("styles[1]", GarmentValueProblem.UNSUPPORTED_VALUE),
                new GarmentFieldViolation("styles[2]", GarmentValueProblem.UNSUPPORTED_VALUE),
                new GarmentFieldViolation("styles[3]", GarmentValueProblem.UNSUPPORTED_VALUE));
    }

    @Test
    void topsOnlyAndPantsOnlyDetailsAreRejectedOutsideTheirCategory() {
        AddGarmentCommand skirt = new AddGarmentCommand(UUID.randomUUID(), KEY, true, "SKIRTS", null, "BLACK",
                null, null, null, "SLIM", "ROUND", "LONG_SLEEVE", "KNEE", "FLARE", null, null, null);

        assertViolations(skirt,
                new GarmentFieldViolation("fit", GarmentValueProblem.NOT_APPLICABLE),
                new GarmentFieldViolation("neckline", GarmentValueProblem.NOT_APPLICABLE),
                new GarmentFieldViolation("sleeveLength", GarmentValueProblem.NOT_APPLICABLE),
                new GarmentFieldViolation("length", GarmentValueProblem.NOT_APPLICABLE),
                new GarmentFieldViolation("silhouette", GarmentValueProblem.NOT_APPLICABLE));
    }

    @Test
    void lengthUsesTheSeparateTopsAndPantsLists() {
        assertViolations(entry("PANTS", "BLACK", c -> with(c, "length", "CROPPED")),
                new GarmentFieldViolation("length", GarmentValueProblem.NOT_IN_CATEGORY));
        assertViolations(entry("TOPS", "BLACK", c -> with(c, "length", "LONG")),
                new GarmentFieldViolation("length", GarmentValueProblem.NOT_IN_CATEGORY));
        assertThat(GarmentEntryValidator.validate(entry("PANTS", "BLACK", c -> with(c, "length", "SHORT")))
                .profile().length()).contains(GarmentLength.SHORT);
    }

    @Test
    void halfAPriceIsIncompleteAndBothHalvesTogetherAreAccepted() {
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, "1250000", null)),
                new GarmentFieldViolation("price", GarmentValueProblem.INCOMPLETE_PRICE));
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, null, "VND")),
                new GarmentFieldViolation("price", GarmentValueProblem.INCOMPLETE_PRICE));
        assertThat(GarmentEntryValidator.validate(entry("SHOES", "WHITE", c -> withPrice(c, "1250000", "VND")))
                .profile().price()).hasValueSatisfying(price -> assertThat(price.amountText()).isEqualTo("1250000"));
    }

    @Test
    void priceProblemsNameTheAmountOrTheCurrency() {
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, "-5", "USD")),
                new GarmentFieldViolation("price.amount", GarmentValueProblem.INVALID_AMOUNT));
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, "5.5", "VND")),
                new GarmentFieldViolation("price.amount", GarmentValueProblem.TOO_MANY_DECIMALS));
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, "5", "DONG")),
                new GarmentFieldViolation("price.currency", GarmentValueProblem.UNSUPPORTED_CURRENCY));
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, "5", "usd")),
                new GarmentFieldViolation("price.currency", GarmentValueProblem.UNSUPPORTED_CURRENCY));
        assertViolations(entry("SHOES", "WHITE", c -> withPrice(c, "12.505", "USD")),
                new GarmentFieldViolation("price.amount", GarmentValueProblem.TOO_MANY_DECIMALS));
    }

    @Test
    void aZeroPriceIsAKnownPriceWhileAMissingPriceStaysUnknown() {
        assertThat(GarmentEntryValidator.validate(entry("SHOES", "WHITE", c -> withPrice(c, "0", "USD")))
                .profile().price()).hasValueSatisfying(price -> assertThat(price.amountText()).isEqualTo("0"));
        assertThat(GarmentEntryValidator.validate(entry("SHOES", "WHITE", c -> c)).profile().price()).isEmpty();
    }

    @Test
    void theIdempotencyKeyMustBeACanonicalUuid() {
        assertViolations(withKey(null), new GarmentFieldViolation("Idempotency-Key", GarmentValueProblem.REQUIRED));
        assertViolations(withKey("1-1-1-1-1"),
                new GarmentFieldViolation("Idempotency-Key", GarmentValueProblem.INVALID_FORMAT));
    }

    @Test
    void aTooLongBrandIsIdentified() {
        assertViolations(entry("BAGS", "BLACK", c -> with(c, "brand", "b".repeat(101))),
                new GarmentFieldViolation("brand", GarmentValueProblem.TOO_LONG));
    }

    @Test
    void aSuppliedBrandIsKeptExactlyAndABlankBrandMeansNoBrand() {
        assertThat(GarmentEntryValidator.validate(entry("BAGS", "BLACK", c -> with(c, "brand", "  Maison  Ébène ")))
                .profile().brand()).map(Brand::value).contains("  Maison  Ébène ");
        assertThat(GarmentEntryValidator.validate(entry("BAGS", "BLACK", c -> with(c, "brand", "\t  ")))
                .profile().brand()).isEmpty();
        assertThat(GarmentEntryValidator.validate(entry("BAGS", "BLACK", c -> with(c, "brand", "b".repeat(100))))
                .profile().brand()).isPresent();
    }

    private static void assertViolations(AddGarmentCommand command, GarmentFieldViolation... expected) {
        assertThatThrownBy(() -> GarmentEntryValidator.validate(command))
                .isInstanceOfSatisfying(GarmentValidationException.class,
                        rejected -> assertThat(rejected.violations()).containsExactly(expected));
    }

    private static AddGarmentCommand entry(String category, String color, UnaryOperator<AddGarmentCommand> change) {
        return change.apply(new AddGarmentCommand(UUID.randomUUID(), KEY, true, category, null, color,
                null, null, null, null, null, null, null, null, null, null, null));
    }

    private static AddGarmentCommand withKey(String idempotencyKey) {
        return new AddGarmentCommand(UUID.randomUUID(), idempotencyKey, true, "TOPS", null, "RED",
                null, null, null, null, null, null, null, null, null, null, null);
    }

    private static AddGarmentCommand with(AddGarmentCommand c, String field, String value) {
        return new AddGarmentCommand(c.ownerAccountId(), c.idempotencyKey(), c.confirmed(), c.category(),
                field.equals("subtype") ? value : c.subtype(), c.color(), c.materials(),
                field.equals("pattern") ? value : c.pattern(),
                c.styles(),
                c.fit(), c.neckline(), c.sleeveLength(),
                field.equals("length") ? value : c.length(),
                c.silhouette(),
                field.equals("brand") ? value : c.brand(),
                c.priceAmount(), c.priceCurrency());
    }

    private static AddGarmentCommand withMaterials(AddGarmentCommand c, List<String> materials) {
        return new AddGarmentCommand(c.ownerAccountId(), c.idempotencyKey(), c.confirmed(), c.category(),
                c.subtype(), c.color(), materials, c.pattern(), c.styles(), c.fit(), c.neckline(), c.sleeveLength(),
                c.length(), c.silhouette(), c.brand(), c.priceAmount(), c.priceCurrency());
    }

    private static AddGarmentCommand withStyles(AddGarmentCommand c, List<String> styles) {
        return new AddGarmentCommand(c.ownerAccountId(), c.idempotencyKey(), c.confirmed(), c.category(),
                c.subtype(), c.color(), c.materials(), c.pattern(), styles, c.fit(), c.neckline(), c.sleeveLength(),
                c.length(), c.silhouette(), c.brand(), c.priceAmount(), c.priceCurrency());
    }

    private static AddGarmentCommand withPrice(AddGarmentCommand c, String amount, String currency) {
        return new AddGarmentCommand(c.ownerAccountId(), c.idempotencyKey(), c.confirmed(), c.category(),
                c.subtype(), c.color(), c.materials(), c.pattern(), c.styles(), c.fit(), c.neckline(),
                c.sleeveLength(), c.length(), c.silhouette(), c.brand(), amount, currency);
    }
}
