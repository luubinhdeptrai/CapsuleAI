package com.capsuleai.wardrobe.domain;

/**
 * US-BULK-001 (S4-BULK-02, OPEN-S4-26) — the persisted state of one import item. "Failed" and "uncertain" are
 * never stored: a failure is the answer to one request (nothing changed), and uncertainty is what a client
 * knows after a lost response, resolved by reading the item again.
 */
public enum BulkImportItemStatus {

    /** Created with the batch; no staged photo is linked yet. */
    AWAITING_PHOTO,

    /** A staged photo is linked and the item can be reviewed and confirmed. Nothing is owned yet. */
    REVIEWABLE,

    /** Explicitly confirmed: exactly one garment exists for this item. Final. */
    ACCEPTED,

    /** Cancelled by the user or by stopping the batch; no garment was created. Final. */
    CANCELED;

    public boolean isFinal() {
        return this == ACCEPTED || this == CANCELED;
    }
}
