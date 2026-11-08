package com.capsuleai.wardrobe.adapter.in.web;

import java.util.List;

/** The overview's {@code limit} or {@code cursor} query parameter is unusable; nothing was read. */
final class InvalidWardrobePageException extends RuntimeException {

    /** One rejected query parameter and its stable field-error code. */
    record FieldError(String field, String code) {
    }

    private final List<FieldError> errors;

    InvalidWardrobePageException(List<FieldError> errors) {
        super("Wardrobe page request rejected with " + errors.size() + " parameter error(s)");
        this.errors = List.copyOf(errors);
    }

    List<FieldError> errors() {
        return errors;
    }
}