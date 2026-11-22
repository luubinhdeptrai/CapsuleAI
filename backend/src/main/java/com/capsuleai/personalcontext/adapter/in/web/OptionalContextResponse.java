package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import java.util.Optional;

/**
 * The account's optional context (US-PROF-002). Every field is always present: {@code revision} and
 * {@code updatedAt} are null while nothing has ever been declared; a value is null when not provided or cleared.
 */
public record OptionalContextResponse(String revision, String genderContext, String bodyShape, String updatedAt) {

    static OptionalContextResponse from(Optional<OptionalContextProfile> profile) {
        return new OptionalContextResponse(
                profile.map(record -> Long.toString(record.revision())).orElse(null),
                profile.flatMap(record -> record.values().genderContext()).map(Enum::name).orElse(null),
                profile.flatMap(record -> record.values().bodyShape()).map(Enum::name).orElse(null),
                profile.map(record -> record.updatedAt().toString()).orElse(null));
    }
}