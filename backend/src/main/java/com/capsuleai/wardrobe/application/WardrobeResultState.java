package com.capsuleai.wardrobe.application;

/**
 * US-WAR-003 AC-04 (S4-WAR-03, OPEN-S4-14): what one page means. A failure or denied access is never a
 * result state: it is an error response.
 */
public enum WardrobeResultState {

    /** The page lists at least one garment. */
    HAS_RESULTS,

    /** First page with conditions: nothing matches, but the owner has current garments (change or reset). */
    NO_MATCH,

    /** First page: the owner has no current garment at all (Add Garment). */
    EMPTY_WARDROBE,

    /** A later page is empty because matching garments left meanwhile: the end of this traversal. */
    END_OF_RESULTS
}