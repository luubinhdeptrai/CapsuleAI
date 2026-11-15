package com.capsuleai.personalcontext.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * What the user has declared (DATA-AUTH-003, S3-PROF-02): a set of profile styles and a priority for
 * each declared common need. A need that is not in {@code commonNeeds} is unset (absent context); a need
 * mapped to {@link NeedPriority#NOT_RELEVANT} is explicitly not relevant. Nothing is inferred or defaulted.
 */
public record DeclaredPreferences(Set<ProfileStyle> styles, Map<CommonNeed, NeedPriority> commonNeeds) {

    public DeclaredPreferences {
        Objects.requireNonNull(styles, "styles must be empty when none are declared");
        Objects.requireNonNull(commonNeeds, "commonNeeds must be empty when none are declared");
        styles = Collections.unmodifiableSet(copyOf(styles));
        EnumMap<CommonNeed, NeedPriority> needs = new EnumMap<>(CommonNeed.class);
        commonNeeds.forEach((need, priority) -> needs.put(Objects.requireNonNull(need, "need"),
                Objects.requireNonNull(priority, "a declared need has a priority; an unset need is absent")));
        commonNeeds = Collections.unmodifiableMap(needs);
    }

    /** Nothing declared. */
    public static DeclaredPreferences none() {
        return new DeclaredPreferences(Set.of(), Map.of());
    }

    public boolean declaresNothing() {
        return styles.isEmpty() && commonNeeds.isEmpty();
    }

    /** The declared priority of a need, or empty if it is unset. */
    public Optional<NeedPriority> priorityOf(CommonNeed need) {
        return Optional.ofNullable(commonNeeds.get(need));
    }

    private static Set<ProfileStyle> copyOf(Collection<ProfileStyle> styles) {
        EnumSet<ProfileStyle> copy = EnumSet.noneOf(ProfileStyle.class);
        copy.addAll(styles);
        return copy;
    }
}