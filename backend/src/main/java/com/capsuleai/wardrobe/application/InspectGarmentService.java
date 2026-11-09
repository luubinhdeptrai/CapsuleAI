package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-WAR-002 — one current garment of the authenticated owner with every accepted
 * property (FR-WAR-001/002, FR-GAR-004–006).
 *
 * <p>Read-only: inspecting changes nothing. A garment of another owner, an unknown
 * id and a malformed id are all "not found"; the owner's own removed garment is
 * "no longer current" (AC-09, AC-11).
 */
public final class InspectGarmentService {

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final GarmentRepository garments;

    public InspectGarmentService(GarmentRepository garments) {
        this.garments = Objects.requireNonNull(garments, "garments");
    }

    /**
     * @throws GarmentNotFoundException        if the owner has no garment with this id
     * @throws GarmentNoLongerCurrentException if the owner's garment was removed from the current wardrobe
     */
    public Garment inspect(UUID ownerAccountId, String garmentId) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        if (garmentId == null || !CANONICAL_UUID.matcher(garmentId).matches()) {
            throw new GarmentNotFoundException();
        }
        Garment garment = garments.findOwned(ownerAccountId, UUID.fromString(garmentId))
                .orElseThrow(GarmentNotFoundException::new);
        if (!garment.isCurrent()) {
            throw new GarmentNoLongerCurrentException();
        }
        return garment;
    }
}