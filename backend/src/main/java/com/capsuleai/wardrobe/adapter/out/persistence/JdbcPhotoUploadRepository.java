package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link PhotoUploadRepository} (US-GAR-003, V10). */
public final class JdbcPhotoUploadRepository implements PhotoUploadRepository {

    static final String SELECT_UPLOAD = """
            SELECT id, owner_account_id, upload_key, content_sha256, object_key, status, content_type,
                   width, height, created_at, expires_at, garment_id
            FROM wardrobe.wardrobe_photo_upload
            """;

    /** Earliest moment an object of an upload discarded now may be deleted: after the in-flight fence. */
    private static final String FENCED_CLEANUP = "GREATEST(:now, created_at + :fenceSeconds * INTERVAL '1 second')";

    private final JdbcClient jdbc;

    public JdbcPhotoUploadRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addIfUploadKeyUnused(PhotoUpload upload) {
        return jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_photo_upload (
                            id, owner_account_id, upload_key, content_sha256, object_key, status, created_at, expires_at)
                        VALUES (:id, :ownerAccountId, :uploadKey, :contentSha256, :objectKey, 'RECEIVING',
                                :createdAt, :expiresAt)
                        ON CONFLICT (owner_account_id, upload_key) DO NOTHING
                        """)
                .param("id", upload.id())
                .param("ownerAccountId", upload.ownerAccountId())
                .param("uploadKey", upload.uploadKey())
                .param("contentSha256", upload.contentSha256())
                .param("objectKey", upload.objectKey())
                .param("createdAt", toDatabase(upload.createdAt()))
                .param("expiresAt", toDatabase(upload.expiresAt()))
                .update() == 1;
    }

    @Override
    public Optional<PhotoUpload> findByUploadKey(UUID ownerAccountId, UUID uploadKey) {
        return jdbc.sql(SELECT_UPLOAD + "WHERE owner_account_id = :ownerAccountId AND upload_key = :uploadKey")
                .param("ownerAccountId", ownerAccountId)
                .param("uploadKey", uploadKey)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public Optional<PhotoUpload> findOwned(UUID ownerAccountId, UUID photoUploadId) {
        return jdbc.sql(SELECT_UPLOAD + "WHERE owner_account_id = :ownerAccountId AND id = :id")
                .param("ownerAccountId", ownerAccountId)
                .param("id", photoUploadId)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public Optional<PhotoUpload> lockOwned(UUID ownerAccountId, UUID photoUploadId) {
        return jdbc.sql(SELECT_UPLOAD + "WHERE owner_account_id = :ownerAccountId AND id = :id FOR UPDATE")
                .param("ownerAccountId", ownerAccountId)
                .param("id", photoUploadId)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public Optional<PhotoUpload> findAttachedTo(UUID ownerAccountId, UUID garmentId) {
        return jdbc.sql(SELECT_UPLOAD
                        + "WHERE owner_account_id = :ownerAccountId AND garment_id = :garmentId AND status = 'ATTACHED'")
                .param("ownerAccountId", ownerAccountId)
                .param("garmentId", garmentId)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public boolean markReady(UUID photoUploadId, String contentType, int width, int height, Instant readyAt) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'READY', content_type = :contentType, width = :width, height = :height,
                            ready_at = :readyAt
                        WHERE id = :id AND status = 'RECEIVING'
                        """)
                .param("id", photoUploadId)
                .param("contentType", contentType)
                .param("width", width)
                .param("height", height)
                .param("readyAt", toDatabase(readyAt))
                .update() == 1;
    }

    @Override
    public boolean discardReceiving(UUID photoUploadId, Instant now, Duration inFlightFence) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'DISCARDED', discarded_at = :now, next_cleanup_at = %s
                        WHERE id = :id AND status = 'RECEIVING'
                        """.formatted(FENCED_CLEANUP))
                .param("id", photoUploadId)
                .param("now", toDatabase(now))
                .param("fenceSeconds", inFlightFence.toSeconds())
                .update() == 1;
    }

    @Override
    public boolean cancel(UUID ownerAccountId, UUID photoUploadId, Instant now, Duration inFlightFence) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'DISCARDED', discarded_at = :now,
                            next_cleanup_at = CASE WHEN status = 'RECEIVING' THEN %s ELSE :now END
                        WHERE id = :id AND owner_account_id = :ownerAccountId AND status IN ('RECEIVING', 'READY')
                        """.formatted(FENCED_CLEANUP))
                .param("id", photoUploadId)
                .param("ownerAccountId", ownerAccountId)
                .param("now", toDatabase(now))
                .param("fenceSeconds", inFlightFence.toSeconds())
                .update() == 1;
    }

    @Override
    public void requeueCleanup(UUID photoUploadId, Instant now) {
        jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET object_deleted_at = NULL, next_cleanup_at = :now
                        WHERE id = :id AND status = 'DISCARDED'
                        """)
                .param("id", photoUploadId)
                .param("now", toDatabase(now))
                .update();
    }

    @Override
    public boolean attach(UUID photoUploadId, UUID garmentId, Instant attachedAt) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'ATTACHED', garment_id = :garmentId, attached_at = :attachedAt
                        WHERE id = :id AND status = 'READY'
                        """)
                .param("id", photoUploadId)
                .param("garmentId", garmentId)
                .param("attachedAt", toDatabase(attachedAt))
                .update() == 1;
    }

    @Override
    public int expireDue(Instant now, Duration inFlightFence, int limit) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'DISCARDED', discarded_at = :now, next_cleanup_at = %s
                        WHERE id IN (
                            SELECT p.id FROM wardrobe.wardrobe_photo_upload p
                            WHERE p.status IN ('RECEIVING', 'READY') AND p.expires_at <= :now
                            ORDER BY p.expires_at
                            LIMIT :limit
                            FOR UPDATE SKIP LOCKED)
                          AND status IN ('RECEIVING', 'READY')
                        """.formatted(FENCED_CLEANUP))
                .param("now", toDatabase(now))
                .param("fenceSeconds", inFlightFence.toSeconds())
                .param("limit", limit)
                .update();
    }

    @Override
    public List<PhotoCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET next_cleanup_at = :leaseUntil, cleanup_attempts = cleanup_attempts + 1
                        WHERE id IN (
                            SELECT p.id FROM wardrobe.wardrobe_photo_upload p
                            WHERE p.status = 'DISCARDED' AND p.object_deleted_at IS NULL AND p.next_cleanup_at <= :now
                            ORDER BY p.next_cleanup_at
                            LIMIT :limit
                            FOR UPDATE SKIP LOCKED)
                        RETURNING id, object_key
                        """)
                .param("now", toDatabase(now))
                .param("leaseUntil", toDatabase(leaseUntil))
                .param("limit", limit)
                .query((row, rowNumber) ->
                        new PhotoCleanupTarget(row.getObject("id", UUID.class), row.getString("object_key")))
                .list();
    }

    @Override
    public boolean markObjectDeleted(UUID photoUploadId, Instant deletedAt) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET object_deleted_at = :deletedAt
                        WHERE id = :id AND status = 'DISCARDED' AND object_deleted_at IS NULL
                        """)
                .param("id", photoUploadId)
                .param("deletedAt", toDatabase(deletedAt))
                .update() == 1;
    }

    static PhotoUpload mapUpload(ResultSet row) throws SQLException {
        return new PhotoUpload(
                row.getObject("id", UUID.class),
                row.getObject("owner_account_id", UUID.class),
                row.getObject("upload_key", UUID.class),
                row.getString("content_sha256"),
                row.getString("object_key"),
                PhotoUploadStatus.valueOf(row.getString("status")),
                row.getString("content_type"),
                row.getObject("width", Integer.class),
                row.getObject("height", Integer.class),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("expires_at", OffsetDateTime.class).toInstant(),
                row.getObject("garment_id", UUID.class));
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}