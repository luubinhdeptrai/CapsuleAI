package com.capsuleai.wardrobe.application;

/** The owner already has the maximum number of active batches (OPEN-S4-25). Nothing was started. */
public final class BulkImportLimitReachedException extends RuntimeException {

    public BulkImportLimitReachedException() {
        super("The maximum number of active import batches is reached");
    }
}
