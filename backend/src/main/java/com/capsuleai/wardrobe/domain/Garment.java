package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * One confirmed garment owned by one account (DATA-GAR-001, BRULE-GAR-001).
 * Only accepted creation produces a Garment; drafts and staged photos never do.
 *
 * <p>{@code removedAt} is null while the garment is in the owner's current
 * wardrobe. Ownership is independent of optional properties and of
 * recommendation eligibility, which is not evaluated (DATA-GAR-005).
 *
 * <p>{@code revision} identifies the accepted profile version (US-GAR-002, S3-GAR-02).
 * {@code photo} is the private photo attached at confirmation, if any (US-GAR-003); it is
 * metadata, not a garment property, and edits never change it (S3-IMG-07).
 */
public record Garment(
        UUID id,
        UUID ownerAccountId,
        GarmentProfile profile,
        ValueProvenance provenance,
        Instant createdAt,
        Instant removedAt,
        long revision,
        Optional<GarmentPhoto> photo) {

    /** The revision of a newly accepted garment. */
    public static final long FIRST_REVISION = 1;

    public Garment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(provenance, "provenance");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(photo, "photo must be Optional.empty() when absent");
        if (removedAt != null && removedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A garment cannot be removed before it was created");
        }
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A garment revision starts at " + FIRST_REVISION);
        }
    }

    /** A garment without a photo. */
    public Garment(UUID id, UUID ownerAccountId, GarmentProfile profile, ValueProvenance provenance,
                   Instant createdAt, Instant removedAt, long revision) {
        this(id, ownerAccountId, profile, provenance, createdAt, removedAt, revision, Optional.empty());
    }

    /** A garment at its first revision, without a photo. */
    public Garment(UUID id, UUID ownerAccountId, GarmentProfile profile, ValueProvenance provenance,
                   Instant createdAt, Instant removedAt) {
        this(id, ownerAccountId, profile, provenance, createdAt, removedAt, FIRST_REVISION, Optional.empty());
    }

    /** A newly confirmed manual entry: user-entered values, currently owned, no photo. */
    public static Garment confirmManualEntry(UUID id, UUID ownerAccountId, GarmentProfile profile, Instant createdAt) {
        return new Garment(id, ownerAccountId, profile, ValueProvenance.USER_ENTERED, createdAt, null);
    }

    /** A newly confirmed manual entry with the reviewed photo attached (US-GAR-003 AC-04). */
    public static Garment confirmWithPhoto(UUID id, UUID ownerAccountId, GarmentProfile profile, Instant createdAt,
                                           GarmentPhoto photo) {
        return new Garment(id, ownerAccountId, profile, ValueProvenance.USER_ENTERED, createdAt, null,
                FIRST_REVISION, Optional.of(photo));
    }

    /**
     * The same garment with an accepted revised profile (US-GAR-002). Identity, owner,
     * provenance, creation time, removal state and photo are kept; the revision advances by one.
     */
    public Garment withRevisedProfile(GarmentProfile revisedProfile) {
        Objects.requireNonNull(revisedProfile, "revisedProfile");
        if (!isCurrent()) {
            throw new IllegalStateException("Only a garment in the current wardrobe can be revised");
        }
        return new Garment(id, ownerAccountId, revisedProfile, provenance, createdAt, removedAt,
                Math.addExact(revision, 1), photo);
    }

    /**
     * US-GAR-005 (S4-GAR-01): the same garment after an accepted removal from the current wardrobe.
     * Identity, owner, profile, provenance, creation time and photo are kept; the revision advances by
     * one, because leaving the current wardrobe changes what the owner reviewed. Removal is monotonic:
     * an already removed garment cannot be removed again (callers report ALREADY_REMOVED instead). A
     * removal time before the creation time (clock skew) is recorded as the creation time, which keeps
     * the V8 constraint removed_at >= created_at.
     */
    public Garment withRemovalAt(Instant removalTime) {
        Objects.requireNonNull(removalTime, "removalTime");
        if (!isCurrent()) {
            throw new IllegalStateException("Only a garment in the current wardrobe can be removed");
        }
        Instant effectiveRemovalTime = removalTime.isBefore(createdAt) ? createdAt : removalTime;
        return new Garment(id, ownerAccountId, profile, provenance, createdAt, effectiveRemovalTime,
                Math.addExact(revision, 1), photo);
    }

    /**
     * Provisional US-GAR-006 (S4-GAR-05): the same garment with the reviewed photo attached. Every property,
     * the creation time and provenance are kept; the revision advances by one, because the attached photo
     * is part of the garment's visible current state. Only a current garment without a photo can receive
     * one: attachment never replaces a photo and never applies to a removed garment.
     */
    public Garment withAttachedPhoto(GarmentPhoto attachedPhoto) {
        Objects.requireNonNull(attachedPhoto, "attachedPhoto");
        if (!isCurrent()) {
            throw new IllegalStateException("Only a garment in the current wardrobe can receive a photo");
        }
        if (photo.isPresent()) {
            throw new IllegalStateException("A garment that already has a photo cannot receive another");
        }
        return new Garment(id, ownerAccountId, profile, provenance, createdAt, removedAt,
                Math.addExact(revision, 1), Optional.of(attachedPhoto));
    }

    public boolean isCurrent() {
        return removedAt == null;
    }

    public boolean isOwnedBy(UUID accountId) {
        return ownerAccountId.equals(accountId);
    }
}