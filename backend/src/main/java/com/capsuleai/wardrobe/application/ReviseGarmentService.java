package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-GAR-002 — accepts one explicitly confirmed correction or enrichment of a current garment
 * the authenticated owner holds (FR-WAR-003, FR-GAR-001–013, BRULE-GAR-002/005/006/009/010).
 *
 * <p>The edit is presence-aware (S3-GAR-01): untouched properties keep their accepted values.
 * The complete resulting profile is validated with the creation rules (S3-GAR-05); a value
 * that no longer applies after a Category change must be corrected or cleared in the same
 * edit and is never cleared automatically (S3-GAR-03). The edit is accepted only if the
 * garment is still owned, current and at the revision the editor read, checked atomically
 * in the database (S3-GAR-02). A stale edit is refused, never merged or retried; after an
 * uncertain outcome the client rereads (S3-GAR-04). An edit that changes nothing writes
 * nothing and keeps the revision (OPEN-S3-06).
 */
public final class ReviseGarmentService {

    static final String CONFIRMED_FIELD = "confirmed";
    static final String EXPECTED_REVISION_FIELD = "expectedRevision";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /** A positive decimal that fits in a long; clients treat it as opaque (OPEN-S3-04). */
    private static final Pattern REVISION_TOKEN = Pattern.compile("[1-9][0-9]{0,17}");

    private final GarmentRepository garments;
    private final WardrobeTransactions transactions;

    public ReviseGarmentService(GarmentRepository garments, WardrobeTransactions transactions) {
        this.garments = Objects.requireNonNull(garments, "garments");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    /**
     * @throws GarmentValidationException        if the edit is unconfirmed, lacks a usable expected revision, or
     *                                           would produce an invalid profile (nothing changed)
     * @throws GarmentNotFoundException          if the owner has no garment with this id
     * @throws GarmentNoLongerCurrentException   if the owner's garment left the current wardrobe
     * @throws GarmentRevisionConflictException  if the garment changed after the editor read it
     */
    public GarmentRevision revise(ReviseGarmentCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        Objects.requireNonNull(command.changes(), "changes");
        long expectedRevision = checkRequest(command);
        UUID garmentId = garmentId(command.garmentId());

        Garment current = garments.findOwned(owner, garmentId).orElseThrow(GarmentNotFoundException::new);
        if (!current.isCurrent()) {
            throw new GarmentNoLongerCurrentException();
        }
        if (current.revision() != expectedRevision) {
            throw new GarmentRevisionConflictException();
        }

        GarmentValues resulting = command.changes().applyTo(GarmentValues.of(current.profile()));
        GarmentProfile revisedProfile = GarmentEntryValidator.validateProfile(resulting);
        if (revisedProfile.equals(current.profile())) {
            return new GarmentRevision(current, false);
        }

        Garment revised = current.withRevisedProfile(revisedProfile);
        return transactions.inTransaction(() -> {
            if (garments.reviseIfCurrentAt(owner, garmentId, expectedRevision, revisedProfile)) {
                return new GarmentRevision(revised, true);
            }
            throw refusalReason(owner, garmentId);
        });
    }

    /** Problems visible without reading the garment; returns the expected revision. */
    private static long checkRequest(ReviseGarmentCommand command) {
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
        violations.addAll(command.changes().shapeViolations());
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

    /** Why the atomic revision changed nothing, as seen inside the same transaction. */
    private RuntimeException refusalReason(UUID owner, UUID garmentId) {
        Optional<Garment> now = garments.findOwned(owner, garmentId);
        if (now.isEmpty()) {
            return new GarmentNotFoundException();
        }
        if (!now.get().isCurrent()) {
            return new GarmentNoLongerCurrentException();
        }
        return new GarmentRevisionConflictException();
    }
}