package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.application.AddGarmentService;
import com.capsuleai.wardrobe.application.GarmentCreation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Wardrobe garment API: the authenticated user's own garments.
 *
 * <p>Every operation is protected by the application's default-deny filter
 * chain (JWT plus usable persistent session). The owner is always the
 * authenticated account; no request names another owner (FR-AUTH-008).
 */
@RestController
@RequestMapping("/api/v1/wardrobe/garments")
public class WardrobeGarmentController {

    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final AddGarmentService addGarmentService;

    public WardrobeGarmentController(AddGarmentService addGarmentService) {
        this.addGarmentService = addGarmentService;
    }

    /**
     * US-GAR-001 — accepts one explicitly confirmed manual garment. 201 when this
     * request created it; 200 with the same garment when the same logical
     * addition (same Idempotency-Key and values) was already accepted.
     */
    @PostMapping
    public ResponseEntity<GarmentResponse> addGarment(
            @AuthenticationPrincipal AuthenticatedAccount owner,
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @RequestBody GarmentCreationRequest request) {
        GarmentCreation creation = addGarmentService.add(request.toCommand(owner.accountId(), idempotencyKey));
        HttpStatus status = creation.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(GarmentResponse.from(creation.garment()));
    }
}