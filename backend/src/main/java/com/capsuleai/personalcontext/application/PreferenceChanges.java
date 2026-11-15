package com.capsuleai.personalcontext.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The requested change (S3-PROF-03): the style selection as a whole, and each mentioned need on its own
 * (keys are submitted need codes, validated by the service). {@code commonNeedsMalformed} is true when
 * {@code commonNeeds} was present but not an object, including null: needs are cleared one at a time,
 * never all at once.
 */
public record PreferenceChanges(
        ValueChange<List<String>> styles,
        Map<String, ValueChange<Integer>> commonNeeds,
        boolean commonNeedsMalformed) {

    public PreferenceChanges {
        Objects.requireNonNull(styles, "styles");
        Objects.requireNonNull(commonNeeds, "commonNeeds");
        commonNeeds = Collections.unmodifiableMap(new LinkedHashMap<>(commonNeeds));
    }

    public static PreferenceChanges none() {
        return new PreferenceChanges(ValueChange.keep(), Map.of(), false);
    }
}