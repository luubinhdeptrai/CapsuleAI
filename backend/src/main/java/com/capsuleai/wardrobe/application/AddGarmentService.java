package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-001 / US-GAR-003 — accepts one explicitly confirmed manual garment for the
 * authenticated owner, optionally with the reviewed staged photo attached
 * (FR-AI-003/008/010, FR-GAR-003, DATA-GAR-004, DATA-INT-004, ADA-009, S3-IMG-03/04).
 *
 * <p>The client's Idempotency-Key names the logical addition. The garment, its sets, the key
 * and (with a photo) the photo's READY → ATTACHED transition commit together, so repeating the
 * same addition returns the garment already accepted instead of creating a second one. A photo
 * that cannot be used makes the addition fail; it never falls back to an imageless garment.
 *
 * <p>US-BULK-001 (OPEN-S4-29): the acceptance rules live in {@link GarmentAcceptance}, shared with bulk item
 * acceptance; this service validates the request and runs them in one transaction. Behavior is unchanged.
 */
public final class AddGarmentService {

    private final GarmentAcceptance garmentAcceptance;
    private final WardrobeTransactions wardrobeTransactions;
    private final Clock clock;

    public AddGarmentService(GarmentRepository garmentRepository, PhotoUploadRepository photoUploadRepository,
                             WardrobeTransactions wardrobeTransactions, Clock clock) {
        this.garmentAcceptance = new GarmentAcceptance(garmentRepository, photoUploadRepository);
        this.wardrobeTransactions = Objects.requireNonNull(wardrobeTransactions, "wardrobeTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws GarmentValidationException    if the entry is unconfirmed, incomplete or invalid (nothing stored)
     * @throws IdempotencyKeyReusedException if the key was used for different values or another photo
     * @throws PhotoUploadNotFoundException  if the named staged photo is not the owner's
     * @throws PhotoUploadNotUsableException if the staged photo is not READY, has expired or is attached
     * @throws GarmentNoLongerCurrentException if this logical addition was accepted earlier and its garment
     *                                         has since been removed (US-GAR-005, OPEN-S4-03)
     */
    public GarmentCreation add(AddGarmentCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ValidatedGarmentEntry entry = GarmentEntryValidator.validate(command);
        String creationFingerprint = GarmentCreationFingerprint.of(entry.profile(), entry.photoUploadId());
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        return wardrobeTransactions.inTransaction(() ->
                garmentAcceptance.acceptInCurrentTransaction(ownerAccountId, entry, creationFingerprint, now));
    }
}