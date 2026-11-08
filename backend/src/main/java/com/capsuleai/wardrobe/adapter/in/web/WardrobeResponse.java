package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.WardrobePage;
import java.util.List;

/**
 * One page of the current wardrobe overview, newest first. {@code garments} is
 * {@code []} on the first page only when the owner genuinely has no current garment
 * (US-WAR-001 AC-04). {@code nextCursor} is {@code null} on the last page; otherwise
 * the client sends it back as {@code cursor} to load the next page (AC-12).
 */
public record WardrobeResponse(List<GarmentSummaryResponse> garments, String nextCursor) {

    static WardrobeResponse from(WardrobePage page) {
        return new WardrobeResponse(page.garments().stream().map(GarmentSummaryResponse::from).toList(),
                page.next().map(WardrobeCursor::encode).orElse(null));
    }
}