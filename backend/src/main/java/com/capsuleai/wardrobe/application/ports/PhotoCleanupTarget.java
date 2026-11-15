package com.capsuleai.wardrobe.application.ports;

import java.util.Objects;
import java.util.UUID;

/** A discarded staged photo whose stored object is due for deletion, claimed by one cleanup run. */
public record PhotoCleanupTarget(UUID photoUploadId, String objectKey) {

    public PhotoCleanupTarget {
        Objects.requireNonNull(photoUploadId, "photoUploadId");
        Objects.requireNonNull(objectKey, "objectKey");
    }
}