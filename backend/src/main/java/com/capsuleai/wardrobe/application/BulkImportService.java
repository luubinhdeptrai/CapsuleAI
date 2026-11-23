package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.BulkImportRepository;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository.BatchOverview;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.BulkImportBatch;
import com.capsuleai.wardrobe.domain.BulkImportBatchStatus;
import com.capsuleai.wardrobe.domain.BulkImportItem;
import com.capsuleai.wardrobe.domain.BulkImportItemStatus;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * US-BULK-001 — import batches of the authenticated owner (FR-BULK-001/002/005, DATA-BULK-001; S4-BULK-02/03/05/
 * 06/07, OPEN-S4-25–28/30): start a batch, read it or the resume list, link a staged photo to an item, cancel an
 * item and stop a batch. Confirming an item is {@link AcceptBulkImportItemService}.
 *
 * <p>Photos are staged one at a time through the existing photo-upload endpoint and then linked to an item, so
 * a staged photo never becomes an owned garment by itself. Item commands hold the batch in share mode and the
 * item exclusively; stop holds the batch exclusively, then each unfinished item, then its stage (OPEN-S4-28).
 * Request problems are reported before anything is read.
 */
public final class BulkImportService {

    static final String IDEMPOTENCY_KEY_FIELD = "Idempotency-Key";
    static final String ITEM_COUNT_FIELD = "itemCount";
    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String PHOTO_UPLOAD_ID_FIELD = "photoUploadId";
    static final String CONFIRMED_FIELD = "confirmed";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /** A positive decimal that fits in a long; clients treat it as opaque (OPEN-S3-04). */
    private static final Pattern REVISION_TOKEN = Pattern.compile("[1-9][0-9]{0,17}");

    private final BulkImportRepository bulkImportRepository;
    private final PhotoUploadRepository photoUploadRepository;
    private final WardrobeTransactions wardrobeTransactions;
    private final Clock clock;
    private final BulkImportPolicy bulkImportPolicy;
    private final PhotoUploadPolicy photoUploadPolicy;

    public BulkImportService(BulkImportRepository bulkImportRepository, PhotoUploadRepository photoUploadRepository,
                             WardrobeTransactions wardrobeTransactions, Clock clock,
                             BulkImportPolicy bulkImportPolicy, PhotoUploadPolicy photoUploadPolicy) {
        this.bulkImportRepository = Objects.requireNonNull(bulkImportRepository, "bulkImportRepository");
        this.photoUploadRepository = Objects.requireNonNull(photoUploadRepository, "photoUploadRepository");
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.bulkImportPolicy = Objects.requireNonNull(bulkImportPolicy, "bulkImportPolicy");
        this.photoUploadPolicy = Objects.requireNonNull(photoUploadPolicy, "photoUploadPolicy");
    }

    /**
     * Starts a batch of {@code itemCount} items, or returns the batch an earlier start with the same
     * Idempotency-Key created (newlyCreated false), whatever state it is in now.
     *
     * @throws GarmentValidationException      if the key or the item count is missing or unusable
     * @throws IdempotencyKeyReusedException   if the key started a batch with another item count
     * @throws BulkImportLimitReachedException if the owner already has the maximum number of active batches
     */
    public BulkImportCreation create(CreateBulkImportCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        List<GarmentFieldViolation> violations = new ArrayList<>();
        UUID creationKey = requiredId(command.idempotencyKey(), IDEMPOTENCY_KEY_FIELD, violations);
        int itemCount = itemCount(command, violations);
        rejectIfAny(violations);
        Instant now = now();
        return wardrobeTransactions.inTransaction(() -> {
            bulkImportRepository.lockAdmission(ownerAccountId);
            Optional<BulkImportBatch> earlier = bulkImportRepository.findByCreationKey(ownerAccountId, creationKey);
            if (earlier.isPresent()) {
                if (earlier.get().itemCount() != itemCount) {
                    throw new IdempotencyKeyReusedException();
                }
                return new BulkImportCreation(overview(ownerAccountId, earlier.get().id(), now), false);
            }
            if (!bulkImportPolicy.admitsAnotherBatch(bulkImportRepository.countActive(ownerAccountId, now))) {
                throw new BulkImportLimitReachedException();
            }
            BulkImportBatch batch = BulkImportBatch.open(UUID.randomUUID(), ownerAccountId, creationKey, itemCount,
                    now, bulkImportPolicy.batchLifetime());
            List<BulkImportItem> items = IntStream.rangeClosed(1, itemCount)
                    .mapToObj(queuePosition -> BulkImportItem.awaitingPhoto(UUID.randomUUID(), batch.id(),
                            queuePosition, UUID.randomUUID(), now))
                    .toList();
            bulkImportRepository.add(batch, items);
            return new BulkImportCreation(overview(ownerAccountId, batch.id(), now), true);
        });
    }

    /** @throws BulkImportNotFoundException if the owner has no such batch */
    public BatchOverview view(UUID ownerAccountId, String batchId) {
        UUID batch = pathId(batchId);
        Instant now = now();
        return wardrobeTransactions.inReadOnlySnapshot(() -> bulkImportRepository.findOverview(ownerAccountId, batch, now))
                .orElseThrow(BulkImportNotFoundException::new);
    }

    /** The owner's active batches, newest first, for resuming after an interruption (OPEN-S4-30). */
    public List<BatchOverview> listActive(UUID ownerAccountId) {
        Instant now = now();
        return wardrobeTransactions.inReadOnlySnapshot(() ->
                bulkImportRepository.findActiveOverviews(ownerAccountId, now));
    }

    /**
     * Links the owner's READY staged photo to an unfinished item, replacing (and cancelling) a photo linked
     * before. Linking the photo the item already has changes nothing, so a retry after a lost response is safe.
     *
     * @throws GarmentValidationException       if expectedRevision or photoUploadId is missing or malformed
     * @throws BulkImportNotFoundException      if the owner has no such batch or item
     * @throws BulkImportClosedException        if the batch is stopped or expired, or the item is final
     * @throws BulkImportItemConflictException  if the item changed after the user read it
     * @throws PhotoUploadNotFoundException     if the photo is not among the owner's staged photos
     * @throws PhotoUploadNotUsableException    if the photo is not READY, has expired or is linked to another item
     */
    public BatchOverview linkPhoto(LinkBulkImportPhotoCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        List<GarmentFieldViolation> violations = new ArrayList<>();
        long expectedRevision = expectedRevision(command.expectedRevision(), violations);
        UUID photoUploadId = requiredId(command.photoUploadId(), PHOTO_UPLOAD_ID_FIELD, violations);
        rejectIfAny(violations);
        UUID batchId = pathId(command.batchId());
        UUID itemId = pathId(command.itemId());
        Instant now = now();
        return wardrobeTransactions.inTransaction(() -> {
            BulkImportBatch batch = bulkImportRepository.lockForItemWork(ownerAccountId, batchId)
                    .orElseThrow(BulkImportNotFoundException::new);
            BulkImportItem item = bulkImportRepository.lockItem(batchId, itemId)
                    .orElseThrow(BulkImportNotFoundException::new);
            if (item.photoUploadId().equals(Optional.of(photoUploadId))) {
                return overview(ownerAccountId, batchId, now);
            }
            requireItemWork(batch, now);
            requireRevision(item, expectedRevision);
            if (item.isFinished()) {
                throw new BulkImportClosedException();
            }
            PhotoUpload photo = photoUploadRepository.lockOwned(ownerAccountId, photoUploadId)
                    .orElseThrow(PhotoUploadNotFoundException::new);
            if (!photo.isUsableAt(now) || bulkImportRepository.isPhotoLinkedToAnotherItem(photoUploadId, itemId)) {
                throw new PhotoUploadNotUsableException();
            }
            write(item.withPhoto(photoUploadId, now), item.revision());
            item.photoUploadId().ifPresent(replacedPhoto -> cancelStage(ownerAccountId, replacedPhoto, now));
            return overview(ownerAccountId, batchId, now);
        });
    }

    /**
     * Cancels one unfinished item and its unclaimed stage. Cancelling a cancelled item changes nothing.
     *
     * @throws GarmentValidationException      if expectedRevision is missing or malformed
     * @throws BulkImportNotFoundException     if the owner has no such batch or item
     * @throws BulkImportClosedException       if the item is accepted, or the batch is stopped or expired
     * @throws BulkImportItemConflictException if the item changed after the user read it
     */
    public BatchOverview cancelItem(UUID ownerAccountId, String batchIdText, String itemIdText,
                                    String expectedRevisionToken) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        List<GarmentFieldViolation> violations = new ArrayList<>();
        long expectedRevision = expectedRevision(expectedRevisionToken, violations);
        rejectIfAny(violations);
        UUID batchId = pathId(batchIdText);
        UUID itemId = pathId(itemIdText);
        Instant now = now();
        return wardrobeTransactions.inTransaction(() -> {
            BulkImportBatch batch = bulkImportRepository.lockForItemWork(ownerAccountId, batchId)
                    .orElseThrow(BulkImportNotFoundException::new);
            BulkImportItem item = bulkImportRepository.lockItem(batchId, itemId)
                    .orElseThrow(BulkImportNotFoundException::new);
            if (item.status() == BulkImportItemStatus.CANCELED) {
                return overview(ownerAccountId, batchId, now);
            }
            if (item.status() == BulkImportItemStatus.ACCEPTED) {
                throw new BulkImportClosedException();
            }
            requireItemWork(batch, now);
            requireRevision(item, expectedRevision);
            write(item.canceled(now), item.revision());
            item.photoUploadId().ifPresent(linkedPhoto -> cancelStage(ownerAccountId, linkedPhoto, now));
            return overview(ownerAccountId, batchId, now);
        });
    }

    /**
     * Stops the batch authoritatively (S4-BULK-07): every item not accepted is cancelled with its unclaimed
     * stage, accepted items stay accepted, and no new item work is taken. Stopping again changes nothing.
     *
     * @throws GarmentValidationException  if the stop is not explicitly confirmed
     * @throws BulkImportNotFoundException if the owner has no such batch
     */
    public BatchOverview stop(UUID ownerAccountId, String batchIdText, Boolean confirmed) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        List<GarmentFieldViolation> violations = new ArrayList<>();
        requireConfirmation(confirmed, violations);
        rejectIfAny(violations);
        UUID batchId = pathId(batchIdText);
        Instant now = now();
        return wardrobeTransactions.inTransaction(() -> {
            BulkImportBatch batch = bulkImportRepository.lockForStop(ownerAccountId, batchId)
                    .orElseThrow(BulkImportNotFoundException::new);
            if (batch.status() == BulkImportBatchStatus.STOPPED) {
                return overview(ownerAccountId, batchId, now);
            }
            for (BulkImportItem item : bulkImportRepository.lockUnfinishedItems(batchId)) {
                write(item.canceled(now), item.revision());
                item.photoUploadId().ifPresent(linkedPhoto -> cancelStage(ownerAccountId, linkedPhoto, now));
            }
            BulkImportBatch stopped = batch.stop(now);
            if (!bulkImportRepository.markStopped(batchId, stopped.stoppedAt().orElseThrow())) {
                throw new IllegalStateException("A locked open batch could not be stopped");
            }
            return overview(ownerAccountId, batchId, now);
        });
    }

    // ---- shared with AcceptBulkImportItemService ------------------------------------------------------

    /** The expected revision, or 0 after recording why it is missing or malformed. */
    static long expectedRevision(String token, List<GarmentFieldViolation> violations) {
        if (token == null || token.isBlank()) {
            violations.add(new GarmentFieldViolation(EXPECTED_REVISION_FIELD, GarmentValueProblem.REQUIRED));
            return 0;
        }
        if (!REVISION_TOKEN.matcher(token).matches()) {
            violations.add(new GarmentFieldViolation(EXPECTED_REVISION_FIELD, GarmentValueProblem.INVALID_FORMAT));
            return 0;
        }
        return Long.parseLong(token);
    }

    static void requireConfirmation(Boolean confirmed, List<GarmentFieldViolation> violations) {
        if (confirmed == null) {
            violations.add(new GarmentFieldViolation(CONFIRMED_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!confirmed) {
            violations.add(new GarmentFieldViolation(CONFIRMED_FIELD, GarmentValueProblem.NOT_CONFIRMED));
        }
    }

    static void rejectIfAny(List<GarmentFieldViolation> violations) {
        if (!violations.isEmpty()) {
            throw new GarmentValidationException(violations);
        }
    }

    /** A batch or item id from the path; anything that is not a canonical UUID names no resource. */
    static UUID pathId(String value) {
        if (value == null || !CANONICAL_UUID.matcher(value).matches()) {
            throw new BulkImportNotFoundException();
        }
        return UUID.fromString(value);
    }

    static void requireItemWork(BulkImportBatch batch, Instant now) {
        if (!batch.acceptsItemWorkAt(now)) {
            throw new BulkImportClosedException();
        }
    }

    static void requireRevision(BulkImportItem item, long expectedRevision) {
        if (item.revision() != expectedRevision) {
            throw new BulkImportItemConflictException();
        }
    }

    // ---- private helpers ------------------------------------------------------------------------------

    private int itemCount(CreateBulkImportCommand command, List<GarmentFieldViolation> violations) {
        if (!command.itemCountWellFormed()) {
            violations.add(new GarmentFieldViolation(ITEM_COUNT_FIELD, GarmentValueProblem.UNSUPPORTED_VALUE));
            return 0;
        }
        if (command.itemCount() == null) {
            violations.add(new GarmentFieldViolation(ITEM_COUNT_FIELD, GarmentValueProblem.REQUIRED));
            return 0;
        }
        if (!bulkImportPolicy.admitsItemCount(command.itemCount())) {
            violations.add(new GarmentFieldViolation(ITEM_COUNT_FIELD, GarmentValueProblem.UNSUPPORTED_VALUE));
            return 0;
        }
        return command.itemCount();
    }

    private static UUID requiredId(String value, String field, List<GarmentFieldViolation> violations) {
        if (value == null || value.isBlank()) {
            violations.add(new GarmentFieldViolation(field, GarmentValueProblem.REQUIRED));
            return null;
        }
        if (!CANONICAL_UUID.matcher(value).matches()) {
            violations.add(new GarmentFieldViolation(field, GarmentValueProblem.INVALID_FORMAT));
            return null;
        }
        return UUID.fromString(value);
    }

    private void write(BulkImportItem nextItem, long expectedRevision) {
        if (!bulkImportRepository.update(nextItem, expectedRevision)) {
            throw new IllegalStateException("A locked import item could not be updated");
        }
    }

    /** Only a RECEIVING or READY stage changes; a stage attached elsewhere is left alone. */
    private void cancelStage(UUID ownerAccountId, UUID photoUploadId, Instant now) {
        photoUploadRepository.cancel(ownerAccountId, photoUploadId, now, photoUploadPolicy.inFlightFence());
    }

    private BatchOverview overview(UUID ownerAccountId, UUID batchId, Instant now) {
        return bulkImportRepository.findOverview(ownerAccountId, batchId, now)
                .orElseThrow(() -> new IllegalStateException("A locked import batch could not be read"));
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
