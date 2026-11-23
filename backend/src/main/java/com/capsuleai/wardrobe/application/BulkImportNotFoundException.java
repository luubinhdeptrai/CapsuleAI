package com.capsuleai.wardrobe.application;

/** The owner has no such batch, or the batch has no such item. Another account's batch gives the same answer. */
public final class BulkImportNotFoundException extends RuntimeException {

    public BulkImportNotFoundException() {
        super("No such import batch or item for this account");
    }
}
