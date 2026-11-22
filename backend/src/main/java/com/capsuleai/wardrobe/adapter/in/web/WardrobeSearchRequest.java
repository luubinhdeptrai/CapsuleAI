package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.BrowseWardrobeService;
import com.capsuleai.wardrobe.application.WardrobeSearchCriteria;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * US-WAR-003 — the overview's query parameters: {@code q}, repeated {@code category} and {@code color},
 * {@code limit} and {@code cursor} (OPEN-S4-12/13/14). Every problem is reported at once and nothing is
 * guessed. A cursor issued for other conditions is CONDITIONS_CHANGED: the client starts again from the
 * first page of the new conditions.
 */
record WardrobeSearchRequest(WardrobeSearchCriteria criteria, int pageSize, Optional<WardrobePosition> after) {

    static final String QUERY = "q";
    static final String CATEGORY = "category";
    static final String COLOR = "color";

    /** Far above the 8 categories and 34 colors; more raw values are rejected unread. */
    static final int MAXIMUM_VALUES_PER_GROUP = 50;

    private static final Pattern INTEGER = Pattern.compile("-?[0-9]{1,9}");

    static WardrobeSearchRequest parse(String query, List<String> categories, List<String> colors, String limit,
                                       String cursor) {
        List<InvalidWardrobePageException.FieldError> errors = new ArrayList<>();
        Optional<String> text = WardrobeSearchCriteria.normalizeText(query);
        if (text.isPresent()) {
            WardrobeSearchCriteria.problemOf(text.get()).ifPresent(problem -> errors.add(
                    new InvalidWardrobePageException.FieldError(QUERY,
                            problem == WardrobeSearchCriteria.TextProblem.TOO_LONG ? "OUT_OF_RANGE" : "INVALID_FORMAT")));
        }
        Set<GarmentCategory> categorySet = codes(GarmentCategory.class, CATEGORY, categories, errors);
        Set<GarmentColor> colorSet = codes(GarmentColor.class, COLOR, colors, errors);
        boolean conditionsValid = errors.isEmpty();
        int pageSize = pageSize(limit, errors);

        // The cursor's fingerprint depends only on the conditions, so it is checked whenever they are valid,
        // also when the limit is not (every problem is reported at once).
        WardrobeSearchCriteria criteria = null;
        Optional<WardrobePosition> after = Optional.empty();
        if (conditionsValid) {
            criteria = new WardrobeSearchCriteria(text, categorySet, colorSet);
            if (cursor != null) {
                Optional<String> fingerprint = criteria.isUnrestricted()
                        ? Optional.empty() : Optional.of(criteria.fingerprint());
                WardrobeCursor.Reading reading = WardrobeCursor.read(cursor, fingerprint);
                if (reading instanceof WardrobeCursor.Reading.At at) {
                    after = Optional.of(at.position());
                } else {
                    errors.add(new InvalidWardrobePageException.FieldError(WardrobePageRequest.CURSOR,
                            reading instanceof WardrobeCursor.Reading.ConditionsChanged
                                    ? "CONDITIONS_CHANGED" : "INVALID_FORMAT"));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidWardrobePageException(errors);
        }
        return new WardrobeSearchRequest(criteria, pageSize, after);
    }

    private static <E extends Enum<E> & ControlledValue> Set<E> codes(Class<E> vocabulary, String field,
                                                                      List<String> submitted,
                                                                      List<InvalidWardrobePageException.FieldError> errors) {
        Set<E> values = EnumSet.noneOf(vocabulary);
        if (submitted == null || submitted.isEmpty()) {
            return values;
        }
        if (submitted.size() > MAXIMUM_VALUES_PER_GROUP) {
            errors.add(new InvalidWardrobePageException.FieldError(field, "OUT_OF_RANGE"));
            return values;
        }
        boolean unsupported = false;
        for (String code : submitted) {
            Optional<E> value = ControlledValue.fromCode(vocabulary, code);
            if (value.isPresent()) {
                values.add(value.get());
            } else {
                unsupported = true;
            }
        }
        if (unsupported) {
            errors.add(new InvalidWardrobePageException.FieldError(field, "UNSUPPORTED_VALUE"));
        }
        return values;
    }

    /** The same rules as the overview's limit (OPEN-S2-10). */
    private static int pageSize(String limit, List<InvalidWardrobePageException.FieldError> errors) {
        if (limit == null) {
            return BrowseWardrobeService.DEFAULT_PAGE_SIZE;
        }
        if (!INTEGER.matcher(limit).matches()) {
            errors.add(new InvalidWardrobePageException.FieldError(WardrobePageRequest.LIMIT, "INVALID_FORMAT"));
            return BrowseWardrobeService.DEFAULT_PAGE_SIZE;
        }
        int pageSize = Integer.parseInt(limit);
        if (pageSize < 1 || pageSize > BrowseWardrobeService.MAXIMUM_PAGE_SIZE) {
            errors.add(new InvalidWardrobePageException.FieldError(WardrobePageRequest.LIMIT, "OUT_OF_RANGE"));
        }
        return pageSize;
    }
}