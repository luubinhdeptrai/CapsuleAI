package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.CategorySpecificProperty;
import com.capsuleai.wardrobe.domain.ControlledValue;
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
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.InvalidGarmentValueException;
import com.capsuleai.wardrobe.domain.PurchasePrice;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Turns submitted garment values into an accepted profile, or reports every problem at
 * once (FR-GAR-002/003/006–010/012/013, BRULE-GAR-004–006/010).
 *
 * <p>Nothing is silently replaced, discarded or invented: an unsupported value, a Subtype
 * or Length from another category's list, or a category-specific property supplied for a
 * category it does not apply to is reported for correction or clearing. Blank optional
 * values mean "not supplied".
 *
 * <p>Creation (US-GAR-001) validates the logical-action key, the confirmation and the
 * profile; revision (US-GAR-002) validates the profile that results from applying an edit
 * to the accepted one, with exactly the same rules (S3-GAR-03, S3-GAR-05).
 */
final class GarmentEntryValidator {

    static final String IDEMPOTENCY_KEY_FIELD = "Idempotency-Key";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final List<GarmentFieldViolation> violations = new ArrayList<>();

    private GarmentEntryValidator() {
    }

    /** A validated logical-action key and the accepted profile. */
    record ValidatedGarmentEntry(UUID creationKey, GarmentProfile profile) {
    }

    /** @throws GarmentValidationException listing every problem; nothing was stored */
    static ValidatedGarmentEntry validate(AddGarmentCommand command) {
        return new GarmentEntryValidator().validateEntry(command);
    }

    /**
     * Validates complete garment values as one accepted profile (US-GAR-002: the profile that
     * results from an edit). Field names in the reported problems are the request field names,
     * so a previously accepted value that no longer applies is reported against its own field.
     *
     * @throws GarmentValidationException listing every problem
     */
    static GarmentProfile validateProfile(GarmentValues values) {
        GarmentEntryValidator validator = new GarmentEntryValidator();
        Optional<GarmentProfile> profile = validator.profile(values);
        validator.rejectIfAnyProblem();
        return profile.orElseThrow();
    }

    private ValidatedGarmentEntry validateEntry(AddGarmentCommand command) {
        UUID creationKey = creationKey(command.idempotencyKey());
        requireConfirmation(command.confirmed());
        Optional<GarmentProfile> profile = profile(command.values());
        rejectIfAnyProblem();
        return new ValidatedGarmentEntry(creationKey, profile.orElseThrow());
    }

    /** The accepted profile, or empty when any property was rejected (the problems are recorded). */
    private Optional<GarmentProfile> profile(GarmentValues values) {
        int problemsBefore = violations.size();
        Optional<GarmentCategory> category = required(GarmentCategory.class, "category", values.category());
        Optional<GarmentColor> color = required(GarmentColor.class, "color", values.color());
        Optional<GarmentSubtype> subtype = subtype(values.subtype(), category);
        Set<GarmentMaterial> materials = distinct(GarmentMaterial.class, "materials", values.materials());
        Optional<GarmentPattern> pattern = optional(GarmentPattern.class, "pattern", values.pattern());
        Set<GarmentStyle> styles = distinct(GarmentStyle.class, "styles", values.styles());
        Optional<GarmentFit> fit = categorySpecific(
                GarmentFit.class, "fit", values.fit(), CategorySpecificProperty.FIT, category);
        Optional<GarmentNeckline> neckline = categorySpecific(
                GarmentNeckline.class, "neckline", values.neckline(), CategorySpecificProperty.NECKLINE, category);
        Optional<GarmentSleeveLength> sleeveLength = categorySpecific(GarmentSleeveLength.class, "sleeveLength",
                values.sleeveLength(), CategorySpecificProperty.SLEEVE_LENGTH, category);
        Optional<GarmentLength> length = length(values.length(), category);
        Optional<GarmentSilhouette> silhouette = categorySpecific(GarmentSilhouette.class, "silhouette",
                values.silhouette(), CategorySpecificProperty.SILHOUETTE, category);
        Optional<Brand> brand = brand(values.brand());
        Optional<PurchasePrice> price = price(values.priceAmount(), values.priceCurrency());

        if (violations.size() != problemsBefore) {
            return Optional.empty();
        }
        return Optional.of(new GarmentProfile(category.orElseThrow(), subtype, color.orElseThrow(), materials,
                pattern, styles, fit, neckline, sleeveLength, length, silhouette, brand, price));
    }

    private void rejectIfAnyProblem() {
        if (!violations.isEmpty()) {
            throw new GarmentValidationException(violations);
        }
    }

    private UUID creationKey(String idempotencyKey) {
        if (isBlank(idempotencyKey)) {
            reject(IDEMPOTENCY_KEY_FIELD, GarmentValueProblem.REQUIRED);
            return null;
        }
        if (!CANONICAL_UUID.matcher(idempotencyKey).matches()) {
            reject(IDEMPOTENCY_KEY_FIELD, GarmentValueProblem.INVALID_FORMAT);
            return null;
        }
        return UUID.fromString(idempotencyKey);
    }

    /** AC-05: a draft is never authoritative; only explicit confirmation creates a garment. */
    private void requireConfirmation(Boolean confirmed) {
        if (confirmed == null) {
            reject("confirmed", GarmentValueProblem.REQUIRED);
        } else if (!confirmed) {
            reject("confirmed", GarmentValueProblem.NOT_CONFIRMED);
        }
    }

    private <E extends Enum<E> & ControlledValue> Optional<E> required(Class<E> vocabulary, String field, String code) {
        if (isBlank(code)) {
            reject(field, GarmentValueProblem.REQUIRED);
            return Optional.empty();
        }
        return optional(vocabulary, field, code);
    }

    private <E extends Enum<E> & ControlledValue> Optional<E> optional(Class<E> vocabulary, String field, String code) {
        if (isBlank(code)) {
            return Optional.empty();
        }
        Optional<E> value = ControlledValue.fromCode(vocabulary, code);
        if (value.isEmpty()) {
            reject(field, GarmentValueProblem.UNSUPPORTED_VALUE);
        }
        return value;
    }

    private Optional<GarmentSubtype> subtype(String code, Optional<GarmentCategory> category) {
        Optional<GarmentSubtype> subtype = optional(GarmentSubtype.class, "subtype", code);
        if (subtype.isPresent() && category.isPresent() && subtype.get().category() != category.get()) {
            reject("subtype", GarmentValueProblem.NOT_IN_CATEGORY);
            return Optional.empty();
        }
        return subtype;
    }

    /**
     * Distinct approved values of a multi-valued property, Material (FR-GAR-010) or Style
     * (FR-GAR-009): repeated selections collapse into one, and each unsupported entry is
     * identified by its position, for example {@code styles[1]}.
     */
    private <E extends Enum<E> & ControlledValue> Set<E> distinct(
            Class<E> vocabulary, String field, List<String> codes) {
        Set<E> values = EnumSet.noneOf(vocabulary);
        if (codes == null) {
            return values;
        }
        for (int index = 0; index < codes.size(); index++) {
            Optional<E> value = ControlledValue.fromCode(vocabulary, codes.get(index));
            if (value.isPresent()) {
                values.add(value.get());
            } else {
                reject(field + "[" + index + "]", GarmentValueProblem.UNSUPPORTED_VALUE);
            }
        }
        return values;
    }

    private <E extends Enum<E> & ControlledValue> Optional<E> categorySpecific(Class<E> vocabulary, String field,
            String code, CategorySpecificProperty property, Optional<GarmentCategory> category) {
        if (isBlank(code)) {
            return Optional.empty();
        }
        if (category.isPresent() && !category.get().accepts(property)) {
            reject(field, GarmentValueProblem.NOT_APPLICABLE);
            return Optional.empty();
        }
        return optional(vocabulary, field, code);
    }

    private Optional<GarmentLength> length(String code, Optional<GarmentCategory> category) {
        Optional<GarmentLength> length = categorySpecific(
                GarmentLength.class, "length", code, CategorySpecificProperty.LENGTH, category);
        if (length.isPresent() && category.isPresent() && length.get().category() != category.get()) {
            reject("length", GarmentValueProblem.NOT_IN_CATEGORY);
            return Optional.empty();
        }
        return length;
    }

    private Optional<Brand> brand(String rawBrand) {
        try {
            return Brand.optional(rawBrand);
        } catch (InvalidGarmentValueException rejected) {
            reject("brand", rejected.problem());
            return Optional.empty();
        }
    }

    /** Amount and currency together, or neither (BRULE-GAR-010); a missing Price is unknown, not zero. */
    private Optional<PurchasePrice> price(String amount, String currency) {
        boolean amountSupplied = !isBlank(amount);
        boolean currencySupplied = !isBlank(currency);
        if (!amountSupplied && !currencySupplied) {
            return Optional.empty();
        }
        if (amountSupplied != currencySupplied) {
            reject("price", GarmentValueProblem.INCOMPLETE_PRICE);
            return Optional.empty();
        }
        try {
            return Optional.of(PurchasePrice.of(amount, currency));
        } catch (InvalidGarmentValueException rejected) {
            String field = rejected.problem() == GarmentValueProblem.UNSUPPORTED_CURRENCY
                    ? "price.currency"
                    : "price.amount";
            reject(field, rejected.problem());
            return Optional.empty();
        }
    }

    private void reject(String field, GarmentValueProblem problem) {
        violations.add(new GarmentFieldViolation(field, problem));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}