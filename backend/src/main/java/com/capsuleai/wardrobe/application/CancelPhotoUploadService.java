package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-003 — cancels the owner's unconfirmed staged photo (AC-03/AC-06, FR-AI-004/009). Cancelled
 * work creates no garment and becomes unusable at once; its object is deleted by cleanup. Cancelling
 * twice succeeds; a photo that already belongs to a garment cannot be cancelled (S3-IMG-07).
 */
public final class CancelPhotoUploadService {

    private final PhotoUploadRepository uploads;
    private final WardrobeTransactions transactions;
    private final Clock clock;
    private final PhotoUploadPolicy policy;

    public CancelPhotoUploadService(PhotoUploadRepository uploads, WardrobeTransactions transactions, Clock clock,
                                    PhotoUploadPolicy policy) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * @throws PhotoUploadNotFoundException  for an unknown, malformed or another owner's id
     * @throws PhotoUploadNotUsableException if the photo already belongs to a garment
     */
    public void cancel(UUID ownerAccountId, String photoUploadId) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        UUID id = ViewPhotoUploadService.photoUploadId(photoUploadId);
        PhotoUpload upload = uploads.findOwned(ownerAccountId, id).orElseThrow(PhotoUploadNotFoundException::new);
        switch (upload.status()) {
            case ATTACHED -> throw new PhotoUploadNotUsableException();
            case DISCARDED -> {
                // Already cancelled or expired: cancelling again changes nothing.
            }
            case RECEIVING, READY -> {
                boolean cancelled = transactions.inTransaction(() -> uploads.cancel(ownerAccountId, id,
                        clock.instant().truncatedTo(ChronoUnit.MICROS), policy.inFlightFence()));
                if (!cancelled && uploads.findOwned(ownerAccountId, id).map(PhotoUpload::status)
                        .filter(PhotoUploadStatus.ATTACHED::equals).isPresent()) {
                    throw new PhotoUploadNotUsableException();
                }
            }
        }
    }
}