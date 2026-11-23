package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ReviseLocationCommand;
import com.capsuleai.personalcontext.application.SubmittedLocation;
import com.capsuleai.personalcontext.application.ValueChange;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of PATCH /api/v1/personal-context/location (US-CTX-001). {@code selection} omitted keeps the
 * selection, null clears it, an object replaces it. Raw {@link JsonNode} components keep omitted, null and
 * values apart; members of the wrong JSON kind are reported, never coerced. There is no owner field.
 */
public record LocationRevisionRequest(JsonNode expectedRevision, JsonNode selection) {

    ReviseLocationCommand toCommand(UUID ownerAccountId) {
        return new ReviseLocationCommand(ownerAccountId, PreferencesRevisionRequest.expectedRevision(expectedRevision),
                selection(selection));
    }

    static ValueChange<SubmittedLocation> selection(JsonNode node) {
        if (node == null) {
            return ValueChange.keep();
        }
        if (node.isNull()) {
            return ValueChange.clear();
        }
        if (!node.isObject()) {
            return ValueChange.malformed();
        }
        List<String> wrongTypeFields = new ArrayList<>();
        BigDecimal latitude = number(node.get("latitude"), SubmittedLocation.LATITUDE_FIELD, wrongTypeFields);
        BigDecimal longitude = number(node.get("longitude"), SubmittedLocation.LONGITUDE_FIELD, wrongTypeFields);
        String label = text(node.get("label"), SubmittedLocation.LABEL_FIELD, wrongTypeFields);
        String source = text(node.get("source"), SubmittedLocation.SOURCE_FIELD, wrongTypeFields);
        return ValueChange.replace(new SubmittedLocation(latitude, longitude, label, source, wrongTypeFields));
    }

    private static BigDecimal number(JsonNode value, String field, List<String> wrongTypeFields) {
        if (value == null || value.isNull()) {
            return null;
        }
        // A JSON number too large for a double (for example 1e400) is read as infinity and has no decimal value:
        // it is reported like a value of the wrong kind (UNSUPPORTED_VALUE), never left to fail as a server error.
        if (!value.isNumber() || (value.isFloatingPointNumber() && !Double.isFinite(value.doubleValue()))) {
            wrongTypeFields.add(field);
            return null;
        }
        return value.decimalValue();
    }

    private static String text(JsonNode value, String field, List<String> wrongTypeFields) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isString()) {
            wrongTypeFields.add(field);
            return null;
        }
        return value.stringValue();
    }
}
