package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.application.AcceptBulkImportItemService;
import com.capsuleai.wardrobe.application.BulkImportCreation;
import com.capsuleai.wardrobe.application.BulkImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Wardrobe bulk import API (US-BULK-001): the authenticated user's own import batches. Protected by the
 * default-deny filter chain; the owner is always the authenticated account. Photos are staged one at a time
 * through POST /api/v1/wardrobe/photo-uploads and linked here (OPEN-S4-30). Every command answers with the
 * whole batch, so each item's outcome is visible after every step.
 */
@RestController
@RequestMapping("/api/v1/wardrobe/bulk-imports")
public class WardrobeBulkImportController {

    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final BulkImportService bulkImportService;
    private final AcceptBulkImportItemService acceptBulkImportItemService;

    public WardrobeBulkImportController(BulkImportService bulkImportService,
                                        AcceptBulkImportItemService acceptBulkImportItemService) {
        this.bulkImportService = bulkImportService;
        this.acceptBulkImportItemService = acceptBulkImportItemService;
    }

    /** 201 when this request started the batch; 200 with the same batch for a repeated start. */
    @PostMapping
    public ResponseEntity<BulkImportResponse> startBulkImport(
            @AuthenticationPrincipal AuthenticatedAccount owner,
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @RequestBody BulkImportCreationRequest request) {
        BulkImportCreation creation = bulkImportService.create(request.toCommand(owner.accountId(), idempotencyKey));
        HttpStatus status = creation.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(BulkImportResponse.from(creation.batch()));
    }

    @GetMapping
    public BulkImportListResponse listActiveBulkImports(@AuthenticationPrincipal AuthenticatedAccount owner) {
        return BulkImportListResponse.from(bulkImportService.listActive(owner.accountId()));
    }

    @GetMapping("/{batchId}")
    public BulkImportResponse viewBulkImport(@AuthenticationPrincipal AuthenticatedAccount owner,
                                             @PathVariable String batchId) {
        return BulkImportResponse.from(bulkImportService.view(owner.accountId(), batchId));
    }

    @PostMapping("/{batchId}/items/{itemId}/photo")
    public BulkImportResponse linkBulkImportItemPhoto(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                      @PathVariable String batchId, @PathVariable String itemId,
                                                      @RequestBody BulkImportPhotoRequest request) {
        return BulkImportResponse.from(
                bulkImportService.linkPhoto(request.toCommand(owner.accountId(), batchId, itemId)));
    }

    @PostMapping("/{batchId}/items/{itemId}/acceptance")
    public BulkImportResponse acceptBulkImportItem(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                   @PathVariable String batchId, @PathVariable String itemId,
                                                   @RequestBody BulkImportItemAcceptanceRequest request) {
        return BulkImportResponse.from(
                acceptBulkImportItemService.accept(request.toCommand(owner.accountId(), batchId, itemId)));
    }

    @PostMapping("/{batchId}/items/{itemId}/cancellation")
    public BulkImportResponse cancelBulkImportItem(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                   @PathVariable String batchId, @PathVariable String itemId,
                                                   @RequestBody BulkImportItemCancellationRequest request) {
        return BulkImportResponse.from(
                bulkImportService.cancelItem(owner.accountId(), batchId, itemId, request.expectedRevision()));
    }

    @PostMapping("/{batchId}/stop")
    public BulkImportResponse stopBulkImport(@AuthenticationPrincipal AuthenticatedAccount owner,
                                             @PathVariable String batchId,
                                             @RequestBody BulkImportStopRequest request) {
        return BulkImportResponse.from(bulkImportService.stop(owner.accountId(), batchId, request.confirmed()));
    }
}
