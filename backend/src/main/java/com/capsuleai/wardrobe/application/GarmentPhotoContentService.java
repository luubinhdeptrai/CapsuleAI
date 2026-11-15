package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-003 — the photo of the owner's current garment, after the same owner and currency checks
 * as garment detail (S3-IMG-05, AC-07). A missing photo or object affects only this read; the
 * garment's information stays available (AC-06).
 */
public final class GarmentPhotoContentService {

    private final InspectGarmentService garments;
    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;

    public GarmentPhotoContentService(InspectGarmentService garments, PhotoUploadRepository uploads,
                                      PhotoStorage storage) {
        this.garments = Objects.requireNonNull(garments, "garments");
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    /**
     * @throws GarmentNotFoundException          for an unknown, malformed or another owner's garment
     * @throws GarmentNoLongerCurrentException   for the owner's removed garment
     * @throws GarmentPhotoUnavailableException  if no photo is attached or its object is missing
     */
    public StoredPhoto open(UUID ownerAccountId, String garmentId) {
        Garment garment = garments.inspect(ownerAccountId, garmentId);
        if (garment.photo().isEmpty()) {
            throw new GarmentPhotoUnavailableException();
        }
        PhotoUpload attached = uploads.findAttachedTo(ownerAccountId, garment.id())
                .orElseThrow(GarmentPhotoUnavailableException::new);
        return storage.open(attached.objectKey()).orElseThrow(GarmentPhotoUnavailableException::new);
    }
}