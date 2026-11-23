package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.BulkImportRepository;
import com.capsuleai.wardrobe.domain.BulkImportBatch;
import com.capsuleai.wardrobe.domain.BulkImportBatchStatus;
import com.capsuleai.wardrobe.domain.BulkImportItem;
import com.capsuleai.wardrobe.domain.BulkImportItemStatus;
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

/**
 * PostgreSQL persistence of import batches and items (US-BULK-001, V15). Every statement names its tables with
 * the wardrobe schema (AR-01). Lock statements read a single table, so a lock never re-checks a joined row
 * against an old snapshot; overviews are fresh reads joined with the staged photo and the garment.
 */
public final class JdbcBulkImportRepository implements BulkImportRepository {

    /** Per-owner transaction lock: concurrent starts of one owner are admitted one after the other. */
    static final String LOCK_ADMISSION = """
            SELECT 1 FROM (SELECT pg_advisory_xact_lock(hashtextextended(:admissionName, 0))) AS admission
            """;

    static final String SELECT_BATCH = """
            SELECT id, owner_account_id, creation_key, item_count, status, created_at, expires_at, stopped_at
            FROM wardrobe.wardrobe_bulk_import_batch
            """;

    static final String SELECT_BATCH_BY_CREATION_KEY = SELECT_BATCH + """
            WHERE owner_account_id = :ownerAccountId AND creation_key = :creationKey
            """;

    static final String SELECT_OWNED_BATCH = SELECT_BATCH + """
            WHERE owner_account_id = :ownerAccountId AND id = :batchId
            """;

    /** Share mode: item commands of one batch run in parallel, and a stop waits for them. */
    static final String LOCK_BATCH_FOR_ITEM_WORK = SELECT_OWNED_BATCH + """
            FOR SHARE
            """;

    /** Exclusive: item commands wait until the stop commits. */
    static final String LOCK_BATCH_FOR_STOP = SELECT_OWNED_BATCH + """
            FOR UPDATE
            """;

    static final String SELECT_ACTIVE_BATCHES = SELECT_BATCH + """
            WHERE owner_account_id = :ownerAccountId AND status = 'OPEN' AND expires_at > :now
            ORDER BY created_at DESC, id DESC
            """;

    static final String COUNT_ACTIVE = """
            SELECT count(*) FROM wardrobe.wardrobe_bulk_import_batch
            WHERE owner_account_id = :ownerAccountId AND status = 'OPEN' AND expires_at > :now
            """;

    static final String INSERT_BATCH = """
            INSERT INTO wardrobe.wardrobe_bulk_import_batch (
                id, owner_account_id, creation_key, item_count, status, created_at, expires_at, stopped_at)
            VALUES (:id, :ownerAccountId, :creationKey, :itemCount, :status, :createdAt, :expiresAt, NULL)
            """;

    static final String INSERT_ITEM = """
            INSERT INTO wardrobe.wardrobe_bulk_import_item (
                id, batch_id, queue_position, acceptance_key, status, photo_upload_id, garment_id, revision, updated_at)
            VALUES (:id, :batchId, :queuePosition, :acceptanceKey, :status, NULL, NULL, :revision, :updatedAt)
            """;

    static final String SELECT_ITEM = """
            SELECT id, batch_id, queue_position, acceptance_key, status, photo_upload_id, garment_id, revision,
                   updated_at
            FROM wardrobe.wardrobe_bulk_import_item
            """;

    static final String LOCK_ITEM = SELECT_ITEM + """
            WHERE batch_id = :batchId AND id = :itemId
            FOR UPDATE
            """;

    /** Unfinished items in queue order, so concurrent stops always lock them in the same order. */
    static final String LOCK_UNFINISHED_ITEMS = SELECT_ITEM + """
            WHERE batch_id = :batchId AND status IN ('AWAITING_PHOTO', 'REVIEWABLE')
            ORDER BY queue_position
            FOR UPDATE
            """;

    static final String PHOTO_LINKED_TO_ANOTHER_ITEM = """
            SELECT EXISTS (
                SELECT 1 FROM wardrobe.wardrobe_bulk_import_item
                WHERE photo_upload_id = :photoUploadId AND id <> :itemId)
            """;

    static final String UPDATE_ITEM = """
            UPDATE wardrobe.wardrobe_bulk_import_item
            SET status = :status, photo_upload_id = :photoUploadId, garment_id = :garmentId, revision = :revision,
                updated_at = :updatedAt
            WHERE id = :itemId AND revision = :expectedRevision
            """;

    static final String MARK_STOPPED = """
            UPDATE wardrobe.wardrobe_bulk_import_batch
            SET status = 'STOPPED', stopped_at = :stoppedAt
            WHERE id = :batchId AND status = 'OPEN'
            """;

    /** Each item with its photo usability and, once accepted, whether its garment is still current. */
    static final String SELECT_ITEM_OVERVIEWS = """
            SELECT item.id, item.batch_id, item.queue_position, item.acceptance_key, item.status,
                   item.photo_upload_id, item.garment_id, item.revision, item.updated_at,
                   (photo.status = 'READY' AND photo.expires_at > :now) AS photo_ready,
                   CASE WHEN item.garment_id IS NULL THEN NULL ELSE garment.removed_at IS NULL END AS garment_current
            FROM wardrobe.wardrobe_bulk_import_item item
            LEFT JOIN wardrobe.wardrobe_photo_upload photo
                   ON photo.id = item.photo_upload_id AND photo.owner_account_id = :ownerAccountId
            LEFT JOIN wardrobe.wardrobe_garment garment ON garment.id = item.garment_id
            WHERE item.batch_id = :batchId
            ORDER BY item.queue_position
            """;

    private static final String ADMISSION_NAMESPACE = "capsuleai-wardrobe-bulk-import-admission:";

    private final JdbcClient jdbcClient;

    public JdbcBulkImportRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void lockAdmission(UUID ownerAccountId) {
        jdbcClient.sql(LOCK_ADMISSION)
                .param("admissionName", ADMISSION_NAMESPACE + ownerAccountId)
                .query(Integer.class)
                .single();
    }

    @Override
    public Optional<BulkImportBatch> findByCreationKey(UUID ownerAccountId, UUID creationKey) {
        return jdbcClient.sql(SELECT_BATCH_BY_CREATION_KEY)
                .param("ownerAccountId", ownerAccountId)
                .param("creationKey", creationKey)
                .query((row, rowNumber) -> mapBatch(row))
                .optional();
    }

    @Override
    public int countActive(UUID ownerAccountId, Instant now) {
        return jdbcClient.sql(COUNT_ACTIVE)
                .param("ownerAccountId", ownerAccountId)
                .param("now", toDatabase(now))
                .query(Integer.class)
                .single();
    }

    @Override
    public void add(BulkImportBatch batch, List<BulkImportItem> items) {
        jdbcClient.sql(INSERT_BATCH)
                .param("id", batch.id())
                .param("ownerAccountId", batch.ownerAccountId())
                .param("creationKey", batch.creationKey())
                .param("itemCount", (short) batch.itemCount())
                .param("status", batch.status().name())
                .param("createdAt", toDatabase(batch.createdAt()))
                .param("expiresAt", toDatabase(batch.expiresAt()))
                .update();
        for (BulkImportItem item : items) {
            jdbcClient.sql(INSERT_ITEM)
                    .param("id", item.id())
                    .param("batchId", item.batchId())
                    .param("queuePosition", (short) item.queuePosition())
                    .param("acceptanceKey", item.acceptanceKey())
                    .param("status", item.status().name())
                    .param("revision", item.revision())
                    .param("updatedAt", toDatabase(item.updatedAt()))
                    .update();
        }
    }

    @Override
    public Optional<BulkImportBatch> lockForItemWork(UUID ownerAccountId, UUID batchId) {
        return ownedBatch(LOCK_BATCH_FOR_ITEM_WORK, ownerAccountId, batchId);
    }

    @Override
    public Optional<BulkImportBatch> lockForStop(UUID ownerAccountId, UUID batchId) {
        return ownedBatch(LOCK_BATCH_FOR_STOP, ownerAccountId, batchId);
    }

    @Override
    public Optional<BulkImportItem> lockItem(UUID batchId, UUID itemId) {
        return jdbcClient.sql(LOCK_ITEM)
                .param("batchId", batchId)
                .param("itemId", itemId)
                .query((row, rowNumber) -> mapItem(row))
                .optional();
    }

    @Override
    public List<BulkImportItem> lockUnfinishedItems(UUID batchId) {
        return jdbcClient.sql(LOCK_UNFINISHED_ITEMS)
                .param("batchId", batchId)
                .query((row, rowNumber) -> mapItem(row))
                .list();
    }

    @Override
    public boolean isPhotoLinkedToAnotherItem(UUID photoUploadId, UUID itemId) {
        return jdbcClient.sql(PHOTO_LINKED_TO_ANOTHER_ITEM)
                .param("photoUploadId", photoUploadId)
                .param("itemId", itemId)
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean update(BulkImportItem item, long expectedRevision) {
        return jdbcClient.sql(UPDATE_ITEM)
                .param("status", item.status().name())
                .param("photoUploadId", item.photoUploadId().orElse(null), Types.OTHER)
                .param("garmentId", item.garmentId().orElse(null), Types.OTHER)
                .param("revision", item.revision())
                .param("updatedAt", toDatabase(item.updatedAt()))
                .param("itemId", item.id())
                .param("expectedRevision", expectedRevision)
                .update() == 1;
    }

    @Override
    public boolean markStopped(UUID batchId, Instant stoppedAt) {
        return jdbcClient.sql(MARK_STOPPED)
                .param("stoppedAt", toDatabase(stoppedAt))
                .param("batchId", batchId)
                .update() == 1;
    }

    @Override
    public Optional<BatchOverview> findOverview(UUID ownerAccountId, UUID batchId, Instant now) {
        return ownedBatch(SELECT_OWNED_BATCH, ownerAccountId, batchId).map(batch -> overviewOf(batch, now));
    }

    @Override
    public List<BatchOverview> findActiveOverviews(UUID ownerAccountId, Instant now) {
        return jdbcClient.sql(SELECT_ACTIVE_BATCHES)
                .param("ownerAccountId", ownerAccountId)
                .param("now", toDatabase(now))
                .query((row, rowNumber) -> mapBatch(row))
                .list()
                .stream()
                .map(batch -> overviewOf(batch, now))
                .toList();
    }

    /** One of the owner-scoped batch statements: a plain read, or a share or exclusive lock. */
    private Optional<BulkImportBatch> ownedBatch(String statement, UUID ownerAccountId, UUID batchId) {
        return jdbcClient.sql(statement)
                .param("ownerAccountId", ownerAccountId)
                .param("batchId", batchId)
                .query((row, rowNumber) -> mapBatch(row))
                .optional();
    }

    private BatchOverview overviewOf(BulkImportBatch batch, Instant now) {
        List<ItemOverview> items = jdbcClient.sql(SELECT_ITEM_OVERVIEWS)
                .param("now", toDatabase(now))
                .param("ownerAccountId", batch.ownerAccountId())
                .param("batchId", batch.id())
                .query((row, rowNumber) -> mapItemOverview(row))
                .list();
        return new BatchOverview(batch, batch.statusAt(now), items);
    }

    private static ItemOverview mapItemOverview(ResultSet row) throws SQLException {
        BulkImportItem item = mapItem(row);
        LinkedPhotoState photoState;
        if (item.photoUploadId().isEmpty()) {
            photoState = LinkedPhotoState.NONE;
        } else if (Boolean.TRUE.equals(row.getObject("photo_ready", Boolean.class))) {
            photoState = LinkedPhotoState.READY;
        } else {
            photoState = LinkedPhotoState.EXPIRED;
        }
        return new ItemOverview(item, photoState, Optional.ofNullable(row.getObject("garment_current", Boolean.class)));
    }

    private static BulkImportBatch mapBatch(ResultSet row) throws SQLException {
        OffsetDateTime stoppedAt = row.getObject("stopped_at", OffsetDateTime.class);
        return new BulkImportBatch(
                row.getObject("id", UUID.class),
                row.getObject("owner_account_id", UUID.class),
                row.getObject("creation_key", UUID.class),
                row.getInt("item_count"),
                BulkImportBatchStatus.valueOf(row.getString("status")),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("expires_at", OffsetDateTime.class).toInstant(),
                Optional.ofNullable(stoppedAt).map(OffsetDateTime::toInstant));
    }

    private static BulkImportItem mapItem(ResultSet row) throws SQLException {
        return new BulkImportItem(
                row.getObject("id", UUID.class),
                row.getObject("batch_id", UUID.class),
                row.getInt("queue_position"),
                row.getObject("acceptance_key", UUID.class),
                BulkImportItemStatus.valueOf(row.getString("status")),
                Optional.ofNullable(row.getObject("photo_upload_id", UUID.class)),
                Optional.ofNullable(row.getObject("garment_id", UUID.class)),
                row.getLong("revision"),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
