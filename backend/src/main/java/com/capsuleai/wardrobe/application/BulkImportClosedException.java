package com.capsuleai.wardrobe.application;

/**
 * The batch takes no more item work (stopped or expired), or the item's outcome is final (accepted, or
 * cancelled). Nothing changed; reading the batch shows the current outcomes (S4-BULK-07, OPEN-S4-28).
 */
public final class BulkImportClosedException extends RuntimeException {

    public BulkImportClosedException() {
        super("The import batch or item no longer accepts this change");
    }
}
