package com.capsuleai.personalcontext.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Published language (S3-PROF-05, ADA-018): an account's currently declared context as codes. Only
 * declared needs are present; an unset need is absent and a Not Relevant need has priority 0.
 * {@code revision} lets a future consumer detect that the basis of an earlier assessment changed
 * (FR-ANL-014); it does not by itself refresh anything.
 */
public record DeclaredPersonalContext(String revision, Set<String> styleCodes, Map<String, Integer> needPriorities) {

    public DeclaredPersonalContext {
        Objects.requireNonNull(revision, "revision");
        styleCodes = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(styleCodes, "styleCodes")));
        needPriorities = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(needPriorities, "needPriorities")));
    }
}