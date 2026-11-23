package com.capsuleai.wardrobe.application;

/** The item has no linked photo yet, so there is nothing to confirm. Nothing changed. */
public final class BulkImportItemNotReadyException extends RuntimeException {

    public BulkImportItemNotReadyException() {
        super("The import item has no linked photo yet");
    }
}
