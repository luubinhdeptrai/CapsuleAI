package com.capsuleai.wardrobe.domain;

/**
 * Why a submitted photo was not accepted (FR-AI-012, ERR-AI-001). Each maps to the published
 * problem code {@code PHOTO_<NAME>}; the user can replace the photo or continue manually.
 */
public enum PhotoRejectionReason {

    /** The bytes are not a JPEG, PNG or HEIC/HEIF image, whatever the file name or declared type says. */
    UNSUPPORTED_FORMAT,

    /** More than the approved maximum size: 15,000,000 bytes (OPEN-S3-09). */
    TOO_LARGE,

    /** The shortest side of the original image is below 512 pixels. */
    TOO_SMALL,

    /** A supported format that could not be decoded completely and safely within the processing limits. */
    UNREADABLE,

    /**
     * A HEIF file with several top-level images whose primary image is not selected: the approved
     * temporary fail-closed fallback of OPEN-S3-11, used while primary-image selection is disabled or
     * when the primary image cannot be identified reliably. Never a silent choice of another image.
     */
    MULTIPLE_IMAGES
}