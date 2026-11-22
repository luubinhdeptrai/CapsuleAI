package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * US-WAR-003 — the owner's validated search conditions (S4-WAR-01/02/03; OPEN-S4-11/12/13/14).
 *
 * <p>{@code text} is one literal phrase, already stripped of outer whitespace and NFC-normalized; empty
 * means no text condition. {@code categories} and {@code colors} are exact approved codes; an empty set
 * means no restriction. Equivalent conditions (reordered codes, different letter case in the text) have
 * the same {@link #fingerprint()}, which binds a cursor to its conditions.
 */
public record WardrobeSearchCriteria(Optional<String> text, Set<GarmentCategory> categories,
                                     Set<GarmentColor> colors) {

    /** OPEN-S4-12: the longest accepted search text, in Unicode code points. */
    public static final int MAXIMUM_TEXT_CODE_POINTS = 100;

    /**
     * The matching policy the fingerprint is computed for. {@code m1} was the original delegated default
     * (English labels and codes). {@code m2} adds the Vietnamese search aliases (OPEN-S4-11, refined
     * 2026-11-17). Because the same conditions can match different garments under another policy, a cursor
     * issued under any other tag no longer matches the fingerprint and is refused with CONDITIONS_CHANGED.
     * Every later change of the matching rules or of the alias table gets a new tag.
     */
    static final String MATCHING_POLICY = "m2";

    /** Why a normalized search text is not acceptable. */
    public enum TextProblem {
        TOO_LONG,
        CONTROL_CHARACTER
    }

    public WardrobeSearchCriteria {
        Objects.requireNonNull(text, "text must be Optional.empty() without a text condition");
        Objects.requireNonNull(categories, "categories must be empty without a category condition");
        Objects.requireNonNull(colors, "colors must be empty without a color condition");
        text.ifPresent(value -> {
            if (!normalizeText(value).equals(Optional.of(value)) || problemOf(value).isPresent()) {
                throw new IllegalArgumentException("The search text must be normalized and acceptable");
            }
        });
        categories = Collections.unmodifiableSet(categories.isEmpty()
                ? EnumSet.noneOf(GarmentCategory.class) : EnumSet.copyOf(categories));
        colors = Collections.unmodifiableSet(colors.isEmpty()
                ? EnumSet.noneOf(GarmentColor.class) : EnumSet.copyOf(colors));
    }

    /** No condition at all: the plain current-wardrobe overview. */
    public static WardrobeSearchCriteria unrestricted() {
        return new WardrobeSearchCriteria(Optional.empty(), Set.of(), Set.of());
    }

    /**
     * The NFC-normalized text without outer spaces, or empty when it is missing or blank. Every Unicode space
     * counts, including no-break spaces, which {@link String#strip()} would keep.
     */
    public static Optional<String> normalizeText(String rawText) {
        if (rawText == null) {
            return Optional.empty();
        }
        String normalized = withoutOuterSpaces(Normalizer.normalize(rawText, Normalizer.Form.NFC));
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    /** Why the normalized text is not acceptable, if it is not (OPEN-S4-12). */
    public static Optional<TextProblem> problemOf(String normalizedText) {
        if (normalizedText.codePoints().anyMatch(Character::isISOControl)) {
            return Optional.of(TextProblem.CONTROL_CHARACTER);
        }
        if (normalizedText.codePointCount(0, normalizedText.length()) > MAXIMUM_TEXT_CODE_POINTS) {
            return Optional.of(TextProblem.TOO_LONG);
        }
        return Optional.empty();
    }

    public boolean isUnrestricted() {
        return text.isEmpty() && categories.isEmpty() && colors.isEmpty();
    }

    /**
     * The first 16 hexadecimal characters of SHA-256 over the canonical conditions
     * {@code m2|q=<lower-cased text>|c=<sorted category codes>|k=<sorted color codes>}.
     */
    public String fingerprint() {
        String canonical = MATCHING_POLICY + "|q=" + text.map(value -> value.toLowerCase(Locale.ROOT)).orElse("")
                + "|c=" + sortedCodes(categories) + "|k=" + sortedCodes(colors);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", unavailable);
        }
    }

    private static String sortedCodes(Set<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
    }

    private static String withoutOuterSpaces(String text) {
        int start = 0;
        int end = text.length();
        while (start < end && isSpace(text.codePointAt(start))) {
            start += Character.charCount(text.codePointAt(start));
        }
        while (end > start && isSpace(text.codePointBefore(end))) {
            end -= Character.charCount(text.codePointBefore(end));
        }
        return text.substring(start, end);
    }

    private static boolean isSpace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }
}