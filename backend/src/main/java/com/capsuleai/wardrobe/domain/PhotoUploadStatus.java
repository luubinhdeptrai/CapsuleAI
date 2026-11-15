package com.capsuleai.wardrobe.domain;

/**
 * The lifecycle of a staged garment photo (US-GAR-003, S3-IMG-04/08). The constant names are
 * stored and published, so rename one only together with a migration and a contract change.
 */
public enum PhotoUploadStatus {

    /** Intent recorded; the normalized photo is being stored. Not reviewable yet. */
    RECEIVING,

    /** Stored and reviewable; can be confirmed into a new garment until it expires. */
    READY,

    /** Belongs to exactly one confirmed garment; never expires and is never cleaned up. */
    ATTACHED,

    /** Cancelled, expired or failed; unusable, and its object is deleted by cleanup. */
    DISCARDED
}