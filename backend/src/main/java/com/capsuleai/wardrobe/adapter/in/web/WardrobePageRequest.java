package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.BrowseWardrobeService;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The optional {@code limit} and {@code cursor} query parameters of the overview
 * (OPEN-S2-10). A missing limit means {@link BrowseWardrobeService#DEFAULT_PAGE_SIZE};
 * a missing cursor means the first page. Every problem is reported at once and nothing
 * is guessed: an unusable cursor is never treated as "start again" or as an empty page.
 */
record WardrobePageRequest(int pageSize, Optional<WardrobePosition> after) {

    static final String LIMIT = "limit";
    static final String CURSOR = "cursor";

    private static final Pattern INTEGER = Pattern.compile("-?[0-9]{1,9}");

    static WardrobePageRequest parse(String limit, String cursor) {
        List<InvalidWardrobePageException.FieldError> errors = new ArrayList<>();
        int pageSize = BrowseWardrobeService.DEFAULT_PAGE_SIZE;
        if (limit != null) {
            if (!INTEGER.matcher(limit).matches()) {
                errors.add(new InvalidWardrobePageException.FieldError(LIMIT, "INVALID_FORMAT"));
            } else {
                pageSize = Integer.parseInt(limit);
                if (pageSize < 1 || pageSize > BrowseWardrobeService.MAXIMUM_PAGE_SIZE) {
                    errors.add(new InvalidWardrobePageException.FieldError(LIMIT, "OUT_OF_RANGE"));
                }
            }
        }
        Optional<WardrobePosition> after = Optional.empty();
        if (cursor != null) {
            after = WardrobeCursor.decode(cursor);
            if (after.isEmpty()) {
                errors.add(new InvalidWardrobePageException.FieldError(CURSOR, "INVALID_FORMAT"));
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidWardrobePageException(errors);
        }
        return new WardrobePageRequest(pageSize, after);
    }
}