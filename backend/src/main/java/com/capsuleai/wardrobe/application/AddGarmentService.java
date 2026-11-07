package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-001 — accepts one explicitly confirmed manual garment for the
 * authenticated owner (FR-AI-003/008/010, FR-GAR-003, DATA-INT-004, ADA-009).
 *
 * <p>The client's Idempotency-Key names the logical addition. The garment, its
 * materials and the key commit together, so repeating the same addition (a
 * retry after a lost response, a duplicated delivery) returns the garment that
 * was already accepted instead of creating a second one. A new key is a new
 * intention, so two genuinely separate additions of similar garments both succeed.
 */
public final class AddGarmentService {

    private final GarmentRepository garments;
    private final WardrobeTransactions transactions;
    private final Clock clock;

    public AddGarmentService(GarmentRepository garments, WardrobeTransactions transactions, Clock clock) {
        this.garments = Objects.requireNonNull(garments, "garments");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws GarmentValidationException    if the entry is unconfirmed, incomplete or invalid (nothing stored)
     * @throws IdempotencyKeyReusedException if the key was used for different values (nothing changed)
     */
    public GarmentCreation add(AddGarmentCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ValidatedGarmentEntry entry = GarmentEntryValidator.validate(command);
        String fingerprint = GarmentCreationFingerprint.of(entry.profile());
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        Garment candidate = Garment.confirmManualEntry(UUID.randomUUID(), owner, entry.profile(), now);

        return transactions.inTransaction(() -> {
            if (garments.addIfCreationKeyUnused(candidate, entry.creationKey(), fingerprint)) {
                return new GarmentCreation(candidate, true);
            }
            RecordedGarmentCreation earlier = garments.findCreation(owner, entry.creationKey())
                    .orElseThrow(() -> new IllegalStateException("A used creation key has no recorded garment"));
            if (!earlier.creationFingerprint().equals(fingerprint)) {
                throw new IdempotencyKeyReusedException();
            }
            return new GarmentCreation(earlier.garment(), false);
        });
    }
}