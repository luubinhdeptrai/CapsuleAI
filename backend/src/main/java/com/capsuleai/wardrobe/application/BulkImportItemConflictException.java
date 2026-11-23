package com.capsuleai.wardrobe.application;

/** The item changed after the user read it (expectedRevision is stale). Nothing changed. */
public final class BulkImportItemConflictException extends RuntimeException {

    public BulkImportItemConflictException() {
        super("The import item changed after it was read");
    }
}
