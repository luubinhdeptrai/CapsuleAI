package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.BulkImportRepository.BatchOverview;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository.ItemOverview;
import com.capsuleai.wardrobe.domain.BulkImportItem;
import com.capsuleai.wardrobe.domain.BulkImportItemStatus;
import java.util.List;
import java.util.UUID;

/**
 * US-BULK-001 AC-02/AC-05 — one batch and every item's own outcome. Every field is always present: stoppedAt is
 * null unless stopped, photoUploadId is null unless a photo is linked, garmentId and garmentCurrent are null
 * unless the item was accepted. Only an ACCEPTED item has a garment; a linked photo alone is never a garment.
 */
public record BulkImportResponse(
        UUID id,
        String status,
        int itemCount,
        int acceptedCount,
        int canceledCount,
        int pendingCount,
        String createdAt,
        String expiresAt,
        String stoppedAt,
        List<ItemResponse> items) {

    /** {@code photoState}: NONE, READY or EXPIRED (no longer usable for this item: select a photo again). */
    public record ItemResponse(
            UUID id,
            int position,
            String status,
            String revision,
            String photoState,
            UUID photoUploadId,
            UUID garmentId,
            Boolean garmentCurrent) {
    }

    static BulkImportResponse from(BatchOverview overview) {
        List<ItemResponse> items = overview.items().stream().map(BulkImportResponse::item).toList();
        return new BulkImportResponse(
                overview.batch().id(),
                overview.currentStatus().name(),
                overview.batch().itemCount(),
                count(overview, BulkImportItemStatus.ACCEPTED),
                count(overview, BulkImportItemStatus.CANCELED),
                overview.batch().itemCount() - count(overview, BulkImportItemStatus.ACCEPTED)
                        - count(overview, BulkImportItemStatus.CANCELED),
                overview.batch().createdAt().toString(),
                overview.batch().expiresAt().toString(),
                overview.batch().stoppedAt().map(Object::toString).orElse(null),
                items);
    }

    private static ItemResponse item(ItemOverview overview) {
        BulkImportItem item = overview.item();
        return new ItemResponse(item.id(), item.queuePosition(), item.status().name(), Long.toString(item.revision()),
                overview.photoState().name(), item.photoUploadId().orElse(null), item.garmentId().orElse(null),
                overview.garmentCurrent().orElse(null));
    }

    private static int count(BatchOverview overview, BulkImportItemStatus status) {
        return (int) overview.items().stream().filter(item -> item.item().status() == status).count();
    }
}
