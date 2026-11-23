package com.capsuleai.wardrobe.domain;

/** US-BULK-001 (S4-BULK-02/06/07, OPEN-S4-26) — whether a batch still takes item work. */
public enum BulkImportBatchStatus {

    /** Takes item work until it expires. */
    OPEN,

    /** Stopped by the user: every item not accepted was cancelled; accepted items stay accepted. */
    STOPPED,

    /** Derived and never stored: the lifetime of an open batch has ended, so it takes no new item work. */
    EXPIRED
}
