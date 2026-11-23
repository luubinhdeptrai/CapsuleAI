package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.BulkImportRepository.BatchOverview;
import java.util.List;

/** The owner's active batches, newest first, for resuming after an interruption (US-BULK-001, OPEN-S4-30). */
public record BulkImportListResponse(List<BulkImportResponse> batches) {

    static BulkImportListResponse from(List<BatchOverview> overviews) {
        return new BulkImportListResponse(overviews.stream().map(BulkImportResponse::from).toList());
    }
}
