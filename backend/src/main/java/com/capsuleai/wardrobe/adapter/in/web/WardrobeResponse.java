package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.WardrobeSearchCriteria;
import com.capsuleai.wardrobe.application.WardrobeSearchPage;
import java.util.List;

/**
 * One page of the current wardrobe overview, newest first, optionally restricted by conditions (US-WAR-001,
 * US-WAR-003). {@code nextCursor} is {@code null} on the last page; otherwise the client sends it back as
 * {@code cursor} with the same conditions. {@code resultState} tells a genuinely empty wardrobe (Add Garment)
 * from no match (change or reset the conditions) and from the end of a traversal (AC-04).
 * {@code conditions} echoes the active, normalized conditions (AC-03).
 */
public record WardrobeResponse(List<GarmentSummaryResponse> garments, String nextCursor, String resultState,
                               WardrobeConditionsResponse conditions) {

    static WardrobeResponse from(WardrobeSearchPage page, WardrobeSearchCriteria criteria) {
        String nextCursor = page.next()
                .map(position -> criteria.isUnrestricted()
                        ? WardrobeCursor.encode(position)
                        : WardrobeCursor.encodeFiltered(position, criteria.fingerprint()))
                .orElse(null);
        return new WardrobeResponse(page.garments().stream().map(GarmentSummaryResponse::from).toList(), nextCursor,
                page.resultState().name(), WardrobeConditionsResponse.from(criteria));
    }
}