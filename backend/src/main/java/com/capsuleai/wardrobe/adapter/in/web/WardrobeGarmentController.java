package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.application.AddGarmentService;
import com.capsuleai.wardrobe.application.AttachGarmentPhotoService;
import com.capsuleai.wardrobe.application.BrowseWardrobeService;
import com.capsuleai.wardrobe.application.GarmentCreation;
import com.capsuleai.wardrobe.application.GarmentPhotoContentService;
import com.capsuleai.wardrobe.application.GarmentRevision;
import com.capsuleai.wardrobe.application.InspectGarmentService;
import com.capsuleai.wardrobe.application.RemoveGarmentService;
import com.capsuleai.wardrobe.application.ReviseGarmentService;
import com.capsuleai.wardrobe.application.SearchWardrobeService;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final SearchWardrobeService searchWardrobeService;
    private final InspectGarmentService inspectGarmentService;
    private final ReviseGarmentService reviseGarmentService;
    private final GarmentPhotoContentService garmentPhotoContentService;
    private final RemoveGarmentService removeGarmentService;
    private final AttachGarmentPhotoService attachGarmentPhotoService;

    public WardrobeGarmentController(AddGarmentService addGarmentService,
                                     SearchWardrobeService searchWardrobeService,
                                     InspectGarmentService inspectGarmentService,
                                     ReviseGarmentService reviseGarmentService,
                                     GarmentPhotoContentService garmentPhotoContentService,
                                     RemoveGarmentService removeGarmentService,
                                     AttachGarmentPhotoService attachGarmentPhotoService) {
        this.addGarmentService = addGarmentService;
        this.searchWardrobeService = searchWardrobeService;
        this.inspectGarmentService = inspectGarmentService;
        this.reviseGarmentService = reviseGarmentService;
        this.garmentPhotoContentService = garmentPhotoContentService;
        this.removeGarmentService = removeGarmentService;
        this.attachGarmentPhotoService = attachGarmentPhotoService;
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

    /**
     * US-WAR-001 / US-WAR-003 — one page of the current wardrobe, newest first, optionally restricted by a
     * literal text phrase and category/color conditions. A genuinely empty wardrobe, no match and the end of
     * a traversal are told apart by resultState; a failure or an unusable request is never an empty list.
     */
    @GetMapping
    public WardrobeResponse browseWardrobe(@AuthenticationPrincipal AuthenticatedAccount owner,
                                           @RequestParam(name = WardrobeSearchRequest.QUERY, required = false)
                                           String query,
                                           @RequestParam(name = WardrobeSearchRequest.CATEGORY, required = false)
                                           List<String> categories,
                                           @RequestParam(name = WardrobeSearchRequest.COLOR, required = false)
                                           List<String> colors,
                                           @RequestParam(name = WardrobePageRequest.LIMIT, required = false)
                                           String limit,
                                           @RequestParam(name = WardrobePageRequest.CURSOR, required = false)
                                           String cursor) {
        WardrobeSearchRequest request = WardrobeSearchRequest.parse(query, categories, colors, limit, cursor);
        return WardrobeResponse.from(searchWardrobeService.search(owner.accountId(), request.criteria(),
                request.pageSize(), request.after()), request.criteria());
    }

    /**
     * US-WAR-002 — one owned garment with every accepted property. The id is read as
     * text so that a malformed id is simply "not found", like any id the caller does not own.
     */
    @GetMapping("/{garmentId}")
    public GarmentResponse inspectGarment(@AuthenticationPrincipal AuthenticatedAccount owner,
                                          @PathVariable String garmentId) {
        return GarmentResponse.from(inspectGarmentService.inspect(owner.accountId(), garmentId));
    }

    /**
     * US-GAR-002 — applies one explicitly confirmed, presence-aware edit and returns the garment
     * as it now is (200). A stale expected revision is 409 and changes nothing; the client
     * rereads with GET /{garmentId} before asking the user to confirm again.
     */
    @PatchMapping("/{garmentId}")
    public GarmentResponse reviseGarment(@AuthenticationPrincipal AuthenticatedAccount owner,
                                         @PathVariable String garmentId,
                                         @RequestBody GarmentRevisionRequest request) {
        GarmentRevision revision = reviseGarmentService.revise(request.toCommand(owner.accountId(), garmentId));
        return GarmentResponse.from(revision.garment());
    }

    /**
     * US-GAR-005 — removes one current garment after explicit confirmation of the reviewed revision
     * (S4-GAR-01, S4-INT-03). 200 REMOVED, or 200 ALREADY_REMOVED with the original time when the garment
     * was already removed (safe repeat after a lost response). A stale revision is 409 and removes nothing.
     */
    @PostMapping("/{garmentId}/removal")
    public GarmentRemovalResponse removeGarment(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                @PathVariable String garmentId,
                                                @RequestBody GarmentRemovalRequest request) {
        return GarmentRemovalResponse.from(removeGarmentService.remove(request.toCommand(owner.accountId(), garmentId)));
    }

    /**
     * Provisional US-GAR-006 — attaches the owner's reviewed READY photo to the owner's current garment without
     * a photo, under the reviewed revision (S4-GAR-04/05). 200 with the garment as it now is; repeating the
     * same pair returns the same garment without another revision.
     */
    @PostMapping("/{garmentId}/photo-attachment")
    public GarmentResponse attachGarmentPhoto(@AuthenticationPrincipal AuthenticatedAccount owner,
                                              @PathVariable String garmentId,
                                              @RequestBody GarmentPhotoAttachmentRequest request) {
        return GarmentResponse.from(
                attachGarmentPhotoService.attach(request.toCommand(owner.accountId(), garmentId)).garment());
    }

    /**
     * US-GAR-003 — the garment's private photo, streamed after the current owner check (S3-IMG-05).
     * 404 GARMENT_PHOTO_UNAVAILABLE when there is none or its object is missing; the garment's
     * information stays available through GET /{garmentId} (AC-06).
     */
    @GetMapping("/{garmentId}/photo")
    public ResponseEntity<Resource> garmentPhoto(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                 @PathVariable String garmentId) {
        return PhotoResponses.image(garmentPhotoContentService.open(owner.accountId(), garmentId));
    }
}