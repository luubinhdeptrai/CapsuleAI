package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ExpectedRevision;
import com.capsuleai.personalcontext.application.PreferenceChanges;
import com.capsuleai.personalcontext.application.RevisePreferencesCommand;
import com.capsuleai.personalcontext.application.ValueChange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of PATCH /api/v1/personal-context/preferences (US-PROF-001). Each component is a raw
 * {@link JsonNode} so that omitted (Jackson leaves it null), JSON null (a NullNode) and a value can be told
 * apart (S3-PROF-03, OPEN-S3-05). There is no owner field.
 */
public record PreferencesRevisionRequest(JsonNode expectedRevision, JsonNode styles, JsonNode commonNeeds) {

    RevisePreferencesCommand toCommand(UUID ownerAccountId) {
        return new RevisePreferencesCommand(ownerAccountId, expectedRevision(expectedRevision),
                new PreferenceChanges(styles(styles), needs(commonNeeds),
                        commonNeeds != null && !commonNeeds.isObject()));
    }

    /** Omitted: missing (an error). null: no declaration seen yet. A string: a revision token. */
    static ExpectedRevision expectedRevision(JsonNode node) {
        if (node == null) {
            return new ExpectedRevision.Missing();
        }
        if (node.isNull()) {
            return new ExpectedRevision.Unset();
        }
        return node.isString() ? ExpectedRevision.ofToken(node.stringValue()) : new ExpectedRevision.Malformed();
    }

    /** Omitted keeps; null clears; an array replaces (non-string entries become null and are reported). */
    static ValueChange<List<String>> styles(JsonNode node) {
        if (node == null) {
            return ValueChange.keep();
        }
        if (node.isNull()) {
            return ValueChange.clear();
        }
        if (!node.isArray()) {
            return ValueChange.malformed();
        }
        List<String> codes = new ArrayList<>();
        for (JsonNode element : node) {
            codes.add(element.isString() ? element.stringValue() : null);
        }
        return ValueChange.replace(Collections.unmodifiableList(codes));
    }

    /** Each mentioned need: null clears it, an integer sets it, anything else is malformed. */
    static Map<String, ValueChange<Integer>> needs(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        Map<String, ValueChange<Integer>> needs = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            JsonNode value = entry.getValue();
            if (value.isNull()) {
                needs.put(entry.getKey(), ValueChange.clear());
            } else if (value.isIntegralNumber() && value.canConvertToInt()) {
                needs.put(entry.getKey(), ValueChange.replace(value.intValue()));
            } else {
                needs.put(entry.getKey(), ValueChange.malformed());
            }
        }
        return needs;
    }
}