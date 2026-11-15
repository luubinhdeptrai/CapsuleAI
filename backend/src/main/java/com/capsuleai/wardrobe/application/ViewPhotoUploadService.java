package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-GAR-003 — the owner's staged photo: its status (outcome review after an interrupted upload or
 * confirmation) and, while it is READY, its bytes for review (AC-03, S3-IMG-05). Read-only.
 */
public final class ViewPhotoUploadService {

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;
    private final Clock clock;

    public ViewPhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage, Clock clock) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** @throws PhotoUploadNotFoundException for an unknown, malformed or another owner's id */
    public PhotoUpload view(UUID ownerAccountId, String photoUploadId) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        return uploads.findOwned(ownerAccountId, photoUploadId(photoUploadId))
                .orElseThrow(PhotoUploadNotFoundException::new);
    }

    /**
     * The bytes of a READY, unexpired staged photo; the caller closes the result.
     *
     * @throws PhotoUploadNotUsableException if it is not reviewable (receiving, attached, cancelled, expired)
     */
    public StoredPhoto openPending(UUID ownerAccountId, String photoUploadId) {
        PhotoUpload upload = view(ownerAccountId, photoUploadId);
        if (!upload.isUsableAt(clock.instant())) {
            throw new PhotoUploadNotUsableException();
        }
        return storage.open(upload.objectKey()).orElseThrow(PhotoUploadNotUsableException::new);
    }

    /** A canonical UUID, or "not found" for anything else, so a malformed id reveals nothing. */
    static UUID photoUploadId(String photoUploadId) {
        if (photoUploadId == null || !CANONICAL_UUID.matcher(photoUploadId).matches()) {
            throw new PhotoUploadNotFoundException();
        }
        return UUID.fromString(photoUploadId);
    }
}