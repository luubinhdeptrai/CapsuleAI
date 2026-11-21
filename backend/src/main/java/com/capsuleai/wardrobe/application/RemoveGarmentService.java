package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.RemovedGarment;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-GAR-005 — removes one current garment of the authenticated owner after explicit confirmation of the
 * revision the owner reviewed (FR-WAR-004–006, BRULE-GAR-009; S4-GAR-01, S4-INT-01/02, OPEN-S4-01/06).
 *
 * <p>In one Wardrobe transaction the garment leaves the current wardrobe (its revision advances by one)
 * and its durable cleanup obligation is recorded with the fixed deadline {@code removedAt + purgeLimit}.
 * Browse, detail, edit and photo delivery exclude it from that commit on. Removal is monotonic: an
 * already removed garment of the owner is reported as ALREADY_REMOVED with its original removal time,
 * whatever revision the request names, and nothing is written. A garment that changed after the owner
 * reviewed it is a conflict; nothing is removed under an older view.
 */
public final class RemoveGarmentService {

    static final String CONFIRMED_FIELD = "confirmed";
    static final String EXPECTED_REVISION_FIELD = "expectedRevision";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /** A positive decimal that fits in a long; clients treat it as opaque (OPEN-S3-04). */
    private static final Pattern REVISION_TOKEN = Pattern.compile("[1-9][0-9]{0,17}");

    private final GarmentRepository garmentRepository;
    private final GarmentRemovalRepository garmentRemovalRepository;
    private final WardrobeTransactions wardrobeTransactions;
    private final Clock clock;
    private final GarmentRemovalPolicy removalPolicy;

    public RemoveGarmentService(GarmentRepository garmentRepository,
                                GarmentRemovalRepository garmentRemovalRepository,
                                WardrobeTransactions wardrobeTransactions, Clock clock,
                                GarmentRemovalPolicy removalPolicy) {
        this.garmentRepository = Objects.requireNonNull(garmentRepository, "garmentRepository");
        this.garmentRemovalRepository = Objects.requireNonNull(garmentRemovalRepository, "garmentRemovalRepository");
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.removalPolicy = Objects.requireNonNull(removalPolicy, "removalPolicy");
    }

    /**
     * @throws GarmentValidationException      if the removal is unconfirmed or lacks a usable expected revision
     *                                         (checked before anything is read; nothing changed)
     * @throws GarmentNotFoundException        if the owner has no garment with this id
     * @throws GarmentRevisionConflictException if the current garment changed after the owner reviewed it
     */
    public GarmentRemoval remove(RemoveGarmentCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        long expectedRevision = checkRequest(command);
        UUID garmentId = garmentId(command.garmentId());

        Garment garment = garmentRepository.findOwned(ownerAccountId, garmentId)
                .orElseThrow(GarmentNotFoundException::new);
        if (!garment.isCurrent()) {
            return new GarmentRemoval(RemovedGarment.of(garment), false);
        }
        if (garment.revision() != expectedRevision) {
            throw new GarmentRevisionConflictException();
        }

        Garment removedGarment = garment.withRemovalAt(clock.instant().truncatedTo(ChronoUnit.MICROS));
        Instant removedAt = removedGarment.removedAt();
        return wardrobeTransactions.inTransaction(() -> {
            if (!garmentRemovalRepository.removeIfCurrentAt(ownerAccountId, garmentId, expectedRevision, removedAt)) {
                return outcomeAfterRefusal(ownerAccountId, garmentId);
            }
            Optional<PhotoCleanupTarget> attachedPhoto = garmentRemovalRepository.findAttachedPhoto(garmentId);
            garmentRemovalRepository.recordCleanup(garmentId, ownerAccountId, removedAt,
                    removedAt.plus(removalPolicy.purgeLimit()), attachedPhoto);
            return new GarmentRemoval(RemovedGarment.of(removedGarment), true);
        });
    }

    /** Problems visible without reading the garment; returns the expected revision. */
    private static long checkRequest(RemoveGarmentCommand command) {
        List<GarmentFieldViolation> violations = new ArrayList<>();
        if (command.confirmed() == null) {
            violations.add(new GarmentFieldViolation(CONFIRMED_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!command.confirmed()) {
            violations.add(new GarmentFieldViolation(CONFIRMED_FIELD, GarmentValueProblem.NOT_CONFIRMED));
        }
        long expectedRevision = 0;
        String token = command.expectedRevision();
        if (token == null || token.isBlank()) {
            violations.add(new GarmentFieldViolation(EXPECTED_REVISION_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!REVISION_TOKEN.matcher(token).matches()) {
            violations.add(new GarmentFieldViolation(EXPECTED_REVISION_FIELD, GarmentValueProblem.INVALID_FORMAT));
        } else {
            expectedRevision = Long.parseLong(token);
        }
        if (!violations.isEmpty()) {
            throw new GarmentValidationException(violations);
        }
        return expectedRevision;
    }

    private static UUID garmentId(String garmentId) {
        if (garmentId == null || !CANONICAL_UUID.matcher(garmentId).matches()) {
            throw new GarmentNotFoundException();
        }
        return UUID.fromString(garmentId);
    }

    /**
     * Why the conditional removal changed nothing, as seen inside the same transaction: a competing
     * removal committed first (monotonic outcome) or the garment changed (conflict).
     */
    private GarmentRemoval outcomeAfterRefusal(UUID ownerAccountId, UUID garmentId) {
        Garment latest = garmentRepository.findOwned(ownerAccountId, garmentId)
                .orElseThrow(GarmentNotFoundException::new);
        if (!latest.isCurrent()) {
            return new GarmentRemoval(RemovedGarment.of(latest), false);
        }
        throw new GarmentRevisionConflictException();
    }
}
