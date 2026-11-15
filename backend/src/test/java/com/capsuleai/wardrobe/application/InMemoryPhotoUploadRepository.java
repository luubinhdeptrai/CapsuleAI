package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link PhotoUploadRepository} with the same conditional transitions as the SQL. */
final class InMemoryPhotoUploadRepository implements PhotoUploadRepository {

    private static final class Row {
        PhotoUpload upload;
        Instant nextCleanupAt;
        Instant objectDeletedAt;

        Row(PhotoUpload upload) {
            this.upload = upload;
        }
    }

    private final Map<UUID, Row> rows = new LinkedHashMap<>();

    @Override
    public synchronized boolean addIfUploadKeyUnused(PhotoUpload upload) {
        boolean used = rows.values().stream().anyMatch(row -> row.upload.isOwnedBy(upload.ownerAccountId())
                && row.upload.uploadKey().equals(upload.uploadKey()));
        if (used) {
            return false;
        }
        rows.put(upload.id(), new Row(upload));
        return true;
    }

    @Override
    public synchronized Optional<PhotoUpload> findByUploadKey(UUID ownerAccountId, UUID uploadKey) {
        return rows.values().stream().map(row -> row.upload)
                .filter(upload -> upload.isOwnedBy(ownerAccountId) && upload.uploadKey().equals(uploadKey))
                .findFirst();
    }

    @Override
    public synchronized Optional<PhotoUpload> findOwned(UUID ownerAccountId, UUID photoUploadId) {
        return Optional.ofNullable(rows.get(photoUploadId)).map(row -> row.upload)
                .filter(upload -> upload.isOwnedBy(ownerAccountId));
    }

    @Override
    public synchronized Optional<PhotoUpload> lockOwned(UUID ownerAccountId, UUID photoUploadId) {
        return findOwned(ownerAccountId, photoUploadId);
    }

    @Override
    public synchronized Optional<PhotoUpload> findAttachedTo(UUID ownerAccountId, UUID garmentId) {
        return rows.values().stream().map(row -> row.upload)
                .filter(upload -> upload.isOwnedBy(ownerAccountId) && garmentId.equals(upload.garmentId())
                        && upload.status() == PhotoUploadStatus.ATTACHED)
                .findFirst();
    }

    @Override
    public synchronized boolean markReady(UUID photoUploadId, String contentType, int width, int height,
                                          Instant readyAt) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.RECEIVING) {
            return false;
        }
        row.upload = with(row.upload, PhotoUploadStatus.READY, contentType, width, height, null);
        return true;
    }

    @Override
    public synchronized boolean discardReceiving(UUID photoUploadId, Instant now, Duration inFlightFence) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.RECEIVING) {
            return false;
        }
        discard(row, fenced(row, now, inFlightFence));
        return true;
    }

    @Override
    public synchronized boolean cancel(UUID ownerAccountId, UUID photoUploadId, Instant now, Duration inFlightFence) {
        Row row = rows.get(photoUploadId);
        if (row == null || !row.upload.isOwnedBy(ownerAccountId)) {
            return false;
        }
        PhotoUploadStatus status = row.upload.status();
        if (status != PhotoUploadStatus.RECEIVING && status != PhotoUploadStatus.READY) {
            return false;
        }
        discard(row, status == PhotoUploadStatus.RECEIVING ? fenced(row, now, inFlightFence) : now);
        return true;
    }

    @Override
    public synchronized void requeueCleanup(UUID photoUploadId, Instant now) {
        Row row = rows.get(photoUploadId);
        if (row != null && row.upload.status() == PhotoUploadStatus.DISCARDED) {
            row.objectDeletedAt = null;
            row.nextCleanupAt = now;
        }
    }

    @Override
    public synchronized boolean attach(UUID photoUploadId, UUID garmentId, Instant attachedAt) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.READY) {
            return false;
        }
        row.upload = with(row.upload, PhotoUploadStatus.ATTACHED, row.upload.contentType(), row.upload.width(),
                row.upload.height(), garmentId);
        return true;
    }

    @Override
    public synchronized int expireDue(Instant now, Duration inFlightFence, int limit) {
        List<Row> due = rows.values().stream()
                .filter(row -> (row.upload.status() == PhotoUploadStatus.RECEIVING
                        || row.upload.status() == PhotoUploadStatus.READY) && !row.upload.expiresAt().isAfter(now))
                .sorted(Comparator.comparing(row -> row.upload.expiresAt()))
                .limit(limit)
                .toList();
        due.forEach(row -> discard(row, fenced(row, now, inFlightFence)));
        return due.size();
    }

    @Override
    public synchronized List<PhotoCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
        List<PhotoCleanupTarget> claimed = new ArrayList<>();
        rows.values().stream()
                .filter(row -> row.upload.status() == PhotoUploadStatus.DISCARDED && row.objectDeletedAt == null
                        && !row.nextCleanupAt.isAfter(now))
                .sorted(Comparator.comparing(row -> row.nextCleanupAt))
                .limit(limit)
                .forEach(row -> {
                    row.nextCleanupAt = leaseUntil;
                    claimed.add(new PhotoCleanupTarget(row.upload.id(), row.upload.objectKey()));
                });
        return claimed;
    }

    @Override
    public synchronized boolean markObjectDeleted(UUID photoUploadId, Instant deletedAt) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.DISCARDED || row.objectDeletedAt != null) {
            return false;
        }
        row.objectDeletedAt = deletedAt;
        return true;
    }

    synchronized Optional<Instant> nextCleanupAt(UUID photoUploadId) {
        return Optional.ofNullable(rows.get(photoUploadId)).map(row -> row.nextCleanupAt);
    }

    synchronized boolean objectDeleted(UUID photoUploadId) {
        return rows.get(photoUploadId).objectDeletedAt != null;
    }

    synchronized int size() {
        return rows.size();
    }

    private static Instant fenced(Row row, Instant now, Duration inFlightFence) {
        Instant fenceEnd = row.upload.createdAt().plus(inFlightFence);
        return fenceEnd.isAfter(now) ? fenceEnd : now;
    }

    private static void discard(Row row, Instant nextCleanupAt) {
        row.upload = with(row.upload, PhotoUploadStatus.DISCARDED, row.upload.contentType(), row.upload.width(),
                row.upload.height(), null);
        row.nextCleanupAt = nextCleanupAt;
    }

    private static PhotoUpload with(PhotoUpload upload, PhotoUploadStatus status, String contentType, Integer width,
                                    Integer height, UUID garmentId) {
        return new PhotoUpload(upload.id(), upload.ownerAccountId(), upload.uploadKey(), upload.contentSha256(),
                upload.objectKey(), status, contentType, width, height, upload.createdAt(), upload.expiresAt(),
                garmentId);
    }
}
