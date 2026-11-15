package com.capsuleai.wardrobe.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * One owner's staged garment photo (US-GAR-003, S3-IMG-03/04/08): private unconfirmed work that
 * never counts as owned clothing. It becomes part of a garment only through a separately
 * confirmed creation, which moves it from READY to ATTACHED.
 *
 * <p>{@code uploadKey} names the client's logical staging action, {@code contentSha256} the bytes
 * received for it, so a retry can be told apart from a different photo under the same key.
 */
public record PhotoUpload(
        UUID id,
        UUID ownerAccountId,
        UUID uploadKey,
        String contentSha256,
        String objectKey,
        PhotoUploadStatus status,
        String contentType,
        Integer width,
        Integer height,
        Instant createdAt,
        Instant expiresAt,
        UUID garmentId) {

    /** Every stored photo is a normalized, metadata-free JPEG (OPEN-S3-10). */
    public static final String STORED_CONTENT_TYPE = "image/jpeg";

    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    public PhotoUpload {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(contentSha256, "contentSha256");
        Objects.requireNonNull(objectKey, "objectKey");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!SHA256_HEX.matcher(contentSha256).matches()) {
            throw new IllegalArgumentException("contentSha256 must be lower-case hexadecimal SHA-256");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("A staged photo expires after it was staged");
        }
        boolean stored = status == PhotoUploadStatus.READY || status == PhotoUploadStatus.ATTACHED;
        if (stored && (contentType == null || width == null || height == null || width < 1 || height < 1)) {
            throw new IllegalArgumentException("A stored photo has a content type and positive dimensions");
        }
        if ((status == PhotoUploadStatus.ATTACHED) != (garmentId != null)) {
            throw new IllegalArgumentException("Exactly an attached photo names its garment");
        }
    }

    /** A new intent, recorded before anything is stored. */
    public static PhotoUpload receiving(UUID id, UUID ownerAccountId, UUID uploadKey, String contentSha256,
                                        Instant createdAt, Duration pendingTtl) {
        return new PhotoUpload(id, ownerAccountId, uploadKey, contentSha256, objectKeyFor(id),
                PhotoUploadStatus.RECEIVING, null, null, null, createdAt, createdAt.plus(pendingTtl), null);
    }

    /** The opaque storage key: server-generated, unique, free of any user value. */
    public static String objectKeyFor(UUID photoUploadId) {
        return "photos/" + photoUploadId + ".jpg";
    }

    public boolean isOwnedBy(UUID accountId) {
        return ownerAccountId.equals(accountId);
    }

    /** Whether this staged photo can still be reviewed or confirmed at {@code now}. */
    public boolean isUsableAt(Instant now) {
        return status == PhotoUploadStatus.READY && now.isBefore(expiresAt);
    }

    /** The garment's view of this photo; only for a stored photo. */
    public GarmentPhoto asGarmentPhoto() {
        if (status != PhotoUploadStatus.READY && status != PhotoUploadStatus.ATTACHED) {
            throw new IllegalStateException("Only a stored photo can be attached");
        }
        return new GarmentPhoto(id, contentType, width, height);
    }
}