package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.BulkImportRepository.BatchOverview;
import java.util.Objects;

/** The batch a start request names, and whether this request started it (false: an earlier identical start). */
public record BulkImportCreation(BatchOverview batch, boolean newlyCreated) {

    public BulkImportCreation {
        Objects.requireNonNull(batch, "batch");
    }
}
