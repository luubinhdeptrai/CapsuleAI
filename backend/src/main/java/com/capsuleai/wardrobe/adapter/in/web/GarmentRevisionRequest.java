package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.FieldChange;
import com.capsuleai.wardrobe.application.GarmentChanges;
import com.capsuleai.wardrobe.application.GarmentChanges.PriceInput;
import com.capsuleai.wardrobe.application.ReviseGarmentCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of PATCH /api/v1/wardrobe/garments/{garmentId} (US-GAR-002): the explicit
 * confirmation, the revision the editor read and any of the 13 properties to change.
 *
 * <p>Every property is a raw {@link JsonNode} so that an omitted property (Jackson leaves the
 * component null) can be told apart from an explicit JSON null (a NullNode): omitted keeps the
 * accepted value, null clears it (S3-GAR-01, OPEN-S3-05). There is no owner and no media field.
 */
public record GarmentRevisionRequest(
        Boolean confirmed,
        String expectedRevision,
        JsonNode category,
        JsonNode subtype,
        JsonNode color,
        JsonNode materials,
        JsonNode pattern,
        JsonNode styles,
        JsonNode fit,
        JsonNode neckline,
        JsonNode sleeveLength,
        JsonNode length,
        JsonNode silhouette,
        JsonNode brand,
        JsonNode price) {

    ReviseGarmentCommand toCommand(UUID ownerAccountId, String garmentId) {
        GarmentChanges changes = new GarmentChanges(
                text(category), text(subtype), text(color), codes(materials), text(pattern), codes(styles),
                text(fit), text(neckline), text(sleeveLength), text(length), text(silhouette), text(brand),
                price(price));
        return new ReviseGarmentCommand(ownerAccountId, garmentId, confirmed, expectedRevision, changes);
    }

    /** A code or Brand text: omitted keeps, null clears, a string replaces, anything else is malformed. */
    static FieldChange<String> text(JsonNode node) {
        if (node == null) {
            return FieldChange.keep();
        }
        if (node.isNull()) {
            return FieldChange.clear();
        }
        if (node.isString()) {
            return FieldChange.replace(node.stringValue());
        }
        return FieldChange.malformed();
    }

    /**
     * Materials or Styles: omitted keeps, null clears, an array replaces the whole set. A
     * non-string element becomes null, which validation reports at its position (for example
     * {@code materials[1]: UNSUPPORTED_VALUE}); a non-array is malformed.
     */
    static FieldChange<List<String>> codes(JsonNode node) {
        if (node == null) {
            return FieldChange.keep();
        }
        if (node.isNull()) {
            return FieldChange.clear();
        }
        if (!node.isArray()) {
            return FieldChange.malformed();
        }
        List<String> codes = new ArrayList<>();
        for (JsonNode element : node) {
            codes.add(element.isString() ? element.stringValue() : null);
        }
        return FieldChange.replace(Collections.unmodifiableList(codes));
    }

    /** Price is one value: omitted keeps, null clears, an object of string-or-null parts replaces both. */
    static FieldChange<PriceInput> price(JsonNode node) {
        if (node == null) {
            return FieldChange.keep();
        }
        if (node.isNull()) {
            return FieldChange.clear();
        }
        if (!node.isObject()) {
            return FieldChange.malformed();
        }
        JsonNode amount = node.get("amount");
        JsonNode currency = node.get("currency");
        if (!isStringOrAbsent(amount) || !isStringOrAbsent(currency)) {
            return FieldChange.malformed();
        }
        return FieldChange.replace(new PriceInput(stringOrNull(amount), stringOrNull(currency)));
    }

    private static boolean isStringOrAbsent(JsonNode node) {
        return node == null || node.isNull() || node.isString();
    }

    private static String stringOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.stringValue();
    }
}