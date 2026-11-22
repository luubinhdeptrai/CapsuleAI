package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentPhotoAttachmentRepository;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentPhoto;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory fake of {@link GarmentPhotoAttachmentRepository}. Like the SQL LEFT JOIN, a locked garment carries
 * the photo the photo repository has ATTACHED to it.
 */
final class InMemoryGarmentPhotoAttachmentRepository implements GarmentPhotoAttachmentRepository {

    private final Map<UUID, Garment> garments = new LinkedHashMap<>();
    private final InMemoryPhotoUploadRepository photoUploadRepository;

    InMemoryGarmentPhotoAttachmentRepository(InMemoryPhotoUploadRepository photoUploadRepository) {
        this.photoUploadRepository = photoUploadRepository;
    }

    @Override
    public synchronized Optional<Garment> lockOwnedGarment(UUID ownerAccountId, UUID garmentId) {
        Garment stored = garments.get(garmentId);
        if (stored == null || !stored.isOwnedBy(ownerAccountId)) {
            return Optional.empty();
        }
        Optional<GarmentPhoto> attachedPhoto = photoUploadRepository.findAttachedTo(ownerAccountId, garmentId)
                .map(PhotoUpload::asGarmentPhoto);
        return Optional.of(new Garment(stored.id(), stored.ownerAccountId(), stored.profile(), stored.provenance(),
                stored.createdAt(), stored.removedAt(), stored.revision(), attachedPhoto));
    }

    @Override
    public synchronized boolean advanceRevisionIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision) {
        Garment stored = garments.get(garmentId);
        if (stored == null || !stored.isOwnedBy(ownerAccountId) || !stored.isCurrent()
                || stored.revision() != expectedRevision) {
            return false;
        }
        garments.put(garmentId, new Garment(stored.id(), stored.ownerAccountId(), stored.profile(),
                stored.provenance(), stored.createdAt(), stored.removedAt(), expectedRevision + 1));
        return true;
    }

    synchronized void add(Garment garment) {
        garments.put(garment.id(), garment);
    }

    synchronized long revisionOf(UUID garmentId) {
        return garments.get(garmentId).revision();
    }
}
