package com.capsuleai.wardrobe.application.ports;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * One claimed removal-cleanup obligation (US-GAR-005, OPEN-S4-04): which garment, when it was removed,
 * its fixed purge deadline, the photo object still to delete (if any) and which steps are already done.
 */
public record GarmentRemovalCleanupTarget(
        UUID garmentId,
        Instant removedAt,
        Instant purgeDeadline,
        Optional<String> photoObjectKey,
        boolean dataMinimized,
        boolean objectDeleted) {

    public GarmentRemovalCleanupTarget {
        Objects.requireNonNull(garmentId, "garmentId");
        Objects.requireNonNull(removedAt, "removedAt");
        Objects.requireNonNull(purgeDeadline, "purgeDeadline");
        Objects.requireNonNull(photoObjectKey, "photoObjectKey must be Optional.empty() without a photo");
    }

    /** True while the photo object still has to be deleted. */
    public boolean needsObjectDeletion() {
        return photoObjectKey.isPresent() && !objectDeleted;
    }
}