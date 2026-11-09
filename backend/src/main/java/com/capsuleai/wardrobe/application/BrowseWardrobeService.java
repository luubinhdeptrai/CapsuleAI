package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-WAR-001 — the authenticated owner's current confirmed garments, one page at a
 * time, newest first (FR-WAR-001, FR-WAR-008, BRULE-HIST-002, OPEN-S2-10).
 *
 * <p>Read-only: browsing changes no garment, records no Wear Event and evaluates no
 * eligibility. An empty first page means the owner genuinely has no current garment;
 * a retrieval failure propagates as an exception and is never turned into an empty
 * wardrobe (AC-04, AC-05).
 *
 * <p>Pages follow a keyset position, not an offset: a garment added while the owner
 * pages is newer than every position already handed out, so it appears when the
 * overview is loaded again from the first page; a garment removed meanwhile is simply
 * absent. No still-current garment is repeated or skipped (AC-12).
 */
public final class BrowseWardrobeService {

    /** Garments per page when the client does not ask for a size (TD-S2-19, OPEN-S2-10). */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** The largest page a client may ask for. */
    public static final int MAXIMUM_PAGE_SIZE = 100;

    private final GarmentRepository garments;

    public BrowseWardrobeService(GarmentRepository garments) {
        this.garments = Objects.requireNonNull(garments, "garments");
    }

    /**
     * @param pageSize from 1 to {@link #MAXIMUM_PAGE_SIZE}
     * @param after    the {@code next} position of the previous page; empty for the first page
     */
    public WardrobePage browse(UUID ownerAccountId, int pageSize, Optional<WardrobePosition> after) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(after, "after must be Optional.empty() for the first page");
        if (pageSize < 1 || pageSize > MAXIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be from 1 to " + MAXIMUM_PAGE_SIZE);
        }
        // One extra row tells whether another page follows, without a second query or a count.
        List<Garment> found = garments.findCurrentByOwner(ownerAccountId, after, pageSize + 1);
        if (found.size() <= pageSize) {
            return new WardrobePage(found, Optional.empty());
        }
        List<Garment> page = found.subList(0, pageSize);
        return new WardrobePage(page, Optional.of(WardrobePosition.of(page.getLast())));
    }
}