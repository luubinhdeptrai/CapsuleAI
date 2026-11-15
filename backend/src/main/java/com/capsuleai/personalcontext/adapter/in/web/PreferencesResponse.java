package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The account's declaration (OPEN-S3-15). Every field is always present. {@code revision} and
 * {@code updatedAt} are null while nothing has been declared. {@code commonNeeds} lists all six needs in
 * catalogue order: null means unset, 0 means Not Relevant, 1–3 Low/Medium/High. {@code styles} is in
 * catalogue order; [] means no declared style.
 */
public record PreferencesResponse(String revision, List<String> styles, Map<String, Integer> commonNeeds,
                                  String updatedAt) {

    static PreferencesResponse from(Optional<PreferenceProfile> profile) {
        Map<String, Integer> needs = new LinkedHashMap<>();
        for (CommonNeed need : CommonNeed.values()) {
            needs.put(need.name(), profile.flatMap(declared -> declared.preferences().priorityOf(need))
                    .map(NeedPriority::weight).orElse(null));
        }
        return new PreferencesResponse(
                profile.map(declared -> Long.toString(declared.revision())).orElse(null),
                profile.map(declared -> declared.preferences().styles().stream().map(Enum::name).toList())
                        .orElse(List.of()),
                Collections.unmodifiableMap(needs),
                profile.map(declared -> declared.updatedAt().toString()).orElse(null));
    }
}