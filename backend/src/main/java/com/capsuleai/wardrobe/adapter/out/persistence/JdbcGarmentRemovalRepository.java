package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.GarmentRemovalCleanupTarget;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link GarmentRemovalRepository} (US-GAR-005, V12). */
public final class JdbcGarmentRemovalRepository implements GarmentRemovalRepository {

    /**
     * Removes the garment only while it is the owner's, current and at the expected revision. Under READ
     * COMMITTED a concurrent change of the same row makes this statement wait and then re-evaluate the
     * predicate against the committed row, so of two competing changes at most one applies (S4-GAR-01).
     */
    static final String REMOVE_IF_CURRENT_AT = """
            UPDATE wardrobe.wardrobe_garment
            SET removed_at = :removedAt, revision = revision + 1
            WHERE id = :garmentId AND owner_account_id = :ownerAccountId
              AND removed_at IS NULL AND revision = :expectedRevision
            """;

    static final String FIND_ATTACHED_PHOTO = """
            SELECT id, object_key FROM wardrobe.wardrobe_photo_upload
            WHERE garment_id = :garmentId AND status = 'ATTACHED'
            """;

    /** next_attempt_at = removed_at: due at once. ON CONFLICT keeps the first deadline forever. */
    static final String RECORD_CLEANUP = """
            INSERT INTO wardrobe.wardrobe_garment_removal_cleanup (
                garment_id, owner_account_id, removed_at, purge_deadline, photo_upload_id, photo_object_key,
                next_attempt_at)
            VALUES (:garmentId, :ownerAccountId, :removedAt, :purgeDeadline, :photoUploadId, :photoObjectKey,
                    :removedAt)
            ON CONFLICT (garment_id) DO NOTHING
            """;

    static final String CLAIM_DUE_CLEANUP = """
            UPDATE wardrobe.wardrobe_garment_removal_cleanup
            SET next_attempt_at = :leaseUntil, attempts = attempts + 1
            WHERE garment_id IN (
                SELECT c.garment_id FROM wardrobe.wardrobe_garment_removal_cleanup c
                WHERE c.completed_at IS NULL AND c.next_attempt_at <= :now
                ORDER BY c.next_attempt_at
                LIMIT :limit
                FOR UPDATE SKIP LOCKED)
            RETURNING garment_id, removed_at, purge_deadline, photo_object_key,
                      data_minimized_at IS NOT NULL AS data_minimized,
                      object_deleted_at IS NOT NULL AS object_deleted
            """;

    /** Garment first (S4-INT-01): purge every column the minimized shape requires to be NULL. */
    static final String MINIMIZE_GARMENT = """
            UPDATE wardrobe.wardrobe_garment
            SET subtype = NULL, pattern = NULL, fit = NULL, neckline = NULL, sleeve_length = NULL,
                length = NULL, silhouette = NULL, brand = NULL, price_amount = NULL, price_currency = NULL,
                minimized_at = GREATEST(:minimizedAt, removed_at)
            WHERE id = :garmentId AND removed_at IS NOT NULL AND minimized_at IS NULL
            """;

    static final String DELETE_MATERIALS = """
            DELETE FROM wardrobe.wardrobe_garment_material WHERE garment_id = :garmentId
            """;

    static final String DELETE_STYLES = """
            DELETE FROM wardrobe.wardrobe_garment_style WHERE garment_id = :garmentId
            """;

    /** Then the photo row (its key was copied into the obligation at removal time). */
    static final String DELETE_ATTACHED_PHOTO = """
            DELETE FROM wardrobe.wardrobe_photo_upload WHERE garment_id = :garmentId AND status = 'ATTACHED'
            """;

    /** Then the cleanup work. */
    static final String MARK_DATA_MINIMIZED = """
            UPDATE wardrobe.wardrobe_garment_removal_cleanup
            SET data_minimized_at = :minimizedAt
            WHERE garment_id = :garmentId AND data_minimized_at IS NULL
            """;

    static final String MARK_OBJECT_DELETED = """
            UPDATE wardrobe.wardrobe_garment_removal_cleanup
            SET object_deleted_at = :deletedAt
            WHERE garment_id = :garmentId AND photo_object_key IS NOT NULL AND object_deleted_at IS NULL
            """;

    static final String COMPLETE_IF_FINISHED = """
            UPDATE wardrobe.wardrobe_garment_removal_cleanup
            SET completed_at = :completedAt
            WHERE garment_id = :garmentId AND completed_at IS NULL AND data_minimized_at IS NOT NULL
              AND (photo_object_key IS NULL OR object_deleted_at IS NOT NULL)
            """;

    static final String COUNT_OVERDUE = """
            SELECT count(*) FROM wardrobe.wardrobe_garment_removal_cleanup
            WHERE completed_at IS NULL AND removed_at <= :removedBefore
            """;

    private final JdbcClient jdbcClient;

    public JdbcGarmentRemovalRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public boolean removeIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision, Instant removedAt) {
        return jdbcClient.sql(REMOVE_IF_CURRENT_AT)
                .param("garmentId", garmentId)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .param("removedAt", toDatabase(removedAt))
                .update() == 1;
    }

    @Override
    public Optional<PhotoCleanupTarget> findAttachedPhoto(UUID garmentId) {
        return jdbcClient.sql(FIND_ATTACHED_PHOTO)
                .param("garmentId", garmentId)
                .query((row, rowNumber) ->
                        new PhotoCleanupTarget(row.getObject("id", UUID.class), row.getString("object_key")))
                .optional();
    }

    @Override
    public void recordCleanup(UUID garmentId, UUID ownerAccountId, Instant removedAt, Instant purgeDeadline,
                              Optional<PhotoCleanupTarget> attachedPhoto) {
        jdbcClient.sql(RECORD_CLEANUP)
                .param("garmentId", garmentId)
                .param("ownerAccountId", ownerAccountId)
                .param("removedAt", toDatabase(removedAt))
                .param("purgeDeadline", toDatabase(purgeDeadline))
                .param("photoUploadId", attachedPhoto.map(PhotoCleanupTarget::photoUploadId).orElse(null), Types.OTHER)
                .param("photoObjectKey", attachedPhoto.map(PhotoCleanupTarget::objectKey).orElse(null), Types.VARCHAR)
                .update();
    }

    @Override
    public List<GarmentRemovalCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
        return jdbcClient.sql(CLAIM_DUE_CLEANUP)
                .param("now", toDatabase(now))
                .param("leaseUntil", toDatabase(leaseUntil))
                .param("limit", limit)
                .query((row, rowNumber) -> mapTarget(row))
                .list();
    }

    @Override
    public boolean minimizeRemovedGarment(UUID garmentId, Instant minimizedAt) {
        jdbcClient.sql(MINIMIZE_GARMENT)
                .param("garmentId", garmentId)
                .param("minimizedAt", toDatabase(minimizedAt))
                .update();
        jdbcClient.sql(DELETE_MATERIALS).param("garmentId", garmentId).update();
        jdbcClient.sql(DELETE_STYLES).param("garmentId", garmentId).update();
        jdbcClient.sql(DELETE_ATTACHED_PHOTO).param("garmentId", garmentId).update();
        return jdbcClient.sql(MARK_DATA_MINIMIZED)
                .param("garmentId", garmentId)
                .param("minimizedAt", toDatabase(minimizedAt))
                .update() == 1;
    }

    @Override
    public boolean markObjectDeleted(UUID garmentId, Instant deletedAt) {
        return jdbcClient.sql(MARK_OBJECT_DELETED)
                .param("garmentId", garmentId)
                .param("deletedAt", toDatabase(deletedAt))
                .update() == 1;
    }

    @Override
    public boolean completeIfFinished(UUID garmentId, Instant completedAt) {
        return jdbcClient.sql(COMPLETE_IF_FINISHED)
                .param("garmentId", garmentId)
                .param("completedAt", toDatabase(completedAt))
                .update() == 1;
    }

    @Override
    public int countOverdue(Instant removedBefore) {
        return jdbcClient.sql(COUNT_OVERDUE)
                .param("removedBefore", toDatabase(removedBefore))
                .query(Integer.class)
                .single();
    }

    private static GarmentRemovalCleanupTarget mapTarget(ResultSet row) throws SQLException {
        return new GarmentRemovalCleanupTarget(
                row.getObject("garment_id", UUID.class),
                row.getObject("removed_at", OffsetDateTime.class).toInstant(),
                row.getObject("purge_deadline", OffsetDateTime.class).toInstant(),
                Optional.ofNullable(row.getString("photo_object_key")),
                row.getBoolean("data_minimized"),
                row.getBoolean("object_deleted"));
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}