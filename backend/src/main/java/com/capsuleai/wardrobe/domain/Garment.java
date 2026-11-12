package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One confirmed garment owned by one account (DATA-GAR-001, BRULE-GAR-001).
 * Only accepted creation produces a Garment; drafts never do.
 *
 * <p>{@code removedAt} is null while the garment is in the owner's current
 * wardrobe. Ownership is independent of optional properties and of
 * recommendation eligibility, which is not evaluated (DATA-GAR-005).
 *
 * <p>{@code revision} identifies the accepted profile version (US-GAR-002, S3-GAR-02):
 * {@link #FIRST_REVISION} on creation, one more after each accepted edit that changes the
 * profile. It is concurrency metadata, not one of the 13 garment properties.
 */
public record Garment(
        UUID id,
        UUID ownerAccountId,
        GarmentProfile profile,
        ValueProvenance provenance,
        Instant createdAt,
        Instant removedAt,
        long revision) {

    /** The revision of a newly accepted garment. */
    public static final long FIRST_REVISION = 1;

    public Garment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(provenance, "provenance");
        Objects.requireNonNull(createdAt, "createdAt");
        if (removedAt != null && removedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A garment cannot be removed before it was created");
        }
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A garment revision starts at " + FIRST_REVISION);
        }
    }

    /** A garment at its first revision. */
    public Garment(UUID id, UUID ownerAccountId, GarmentProfile profile, ValueProvenance provenance,
                   Instant createdAt, Instant removedAt) {
        this(id, ownerAccountId, profile, provenance, createdAt, removedAt, FIRST_REVISION);
    }

    /** A newly confirmed manual entry: user-entered values, currently owned. */
    public static Garment confirmManualEntry(UUID id, UUID ownerAccountId, GarmentProfile profile, Instant createdAt) {
        return new Garment(id, ownerAccountId, profile, ValueProvenance.USER_ENTERED, createdAt, null);
    }

    /**
     * The same garment with an accepted revised profile (US-GAR-002). Identity, owner,
     * provenance, creation time and removal state are kept; the revision advances by one.
     * Only a current garment can be revised (AC-08).
     */
    public Garment withRevisedProfile(GarmentProfile revisedProfile) {
        Objects.requireNonNull(revisedProfile, "revisedProfile");
        if (!isCurrent()) {
            throw new IllegalStateException("Only a garment in the current wardrobe can be revised");
        }
        return new Garment(id, ownerAccountId, revisedProfile, provenance, createdAt, removedAt,
                Math.addExact(revision, 1));
    }

    public boolean isCurrent() {
        return removedAt == null;
    }

    public boolean isOwnedBy(UUID accountId) {
        return ownerAccountId.equals(accountId);
    }
}