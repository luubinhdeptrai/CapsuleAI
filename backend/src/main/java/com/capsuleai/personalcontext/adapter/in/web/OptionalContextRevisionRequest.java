package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.OptionalContextChanges;
import com.capsuleai.personalcontext.application.ReviseOptionalContextCommand;
import com.capsuleai.personalcontext.application.ValueChange;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of PATCH /api/v1/personal-context/optional-context (US-PROF-002). Each component is a raw
 * {@link JsonNode}, so omitted (Jackson leaves it null), JSON null (a NullNode) and a value can be told apart
 * (S4-PROF-03). There is no owner field.
 */
public record OptionalContextRevisionRequest(JsonNode expectedRevision, JsonNode genderContext, JsonNode bodyShape) {

    ReviseOptionalContextCommand toCommand(UUID ownerAccountId) {
        return new ReviseOptionalContextCommand(ownerAccountId,
                PreferencesRevisionRequest.expectedRevision(expectedRevision),
                new OptionalContextChanges(code(genderContext), code(bodyShape)));
    }

    /** Omitted keeps; null clears; a string replaces; anything else is malformed. */
    static ValueChange<String> code(JsonNode node) {
        if (node == null) {
            return ValueChange.keep();
        }
        if (node.isNull()) {
            return ValueChange.clear();
        }
        return node.isString() ? ValueChange.replace(node.stringValue()) : ValueChange.malformed();
    }
}