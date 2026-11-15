package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.application.CancelPhotoUploadService;
import com.capsuleai.wardrobe.application.StagePhotoCommand;
import com.capsuleai.wardrobe.application.StagePhotoUploadService;
import com.capsuleai.wardrobe.application.StagedPhoto;
import com.capsuleai.wardrobe.application.ViewPhotoUploadService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Staged garment photos of the authenticated user (US-GAR-003, S3-IMG-02/03/05). A staged photo is
 * private unconfirmed work: it never creates a garment. Confirmation is the existing garment
 * creation with {@code photoUploadId}. Every route is protected by the default-deny filter chain.
 */
@RestController
@RequestMapping("/api/v1/wardrobe/photo-uploads")
public class WardrobePhotoUploadController {

    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    static final String PHOTO_PART = "photo";

    private final StagePhotoUploadService stagePhotoUploadService;
    private final ViewPhotoUploadService viewPhotoUploadService;
    private final CancelPhotoUploadService cancelPhotoUploadService;

    public WardrobePhotoUploadController(StagePhotoUploadService stagePhotoUploadService,
                                         ViewPhotoUploadService viewPhotoUploadService,
                                         CancelPhotoUploadService cancelPhotoUploadService) {
        this.stagePhotoUploadService = stagePhotoUploadService;
        this.viewPhotoUploadService = viewPhotoUploadService;
        this.cancelPhotoUploadService = cancelPhotoUploadService;
    }

    /** 201 when this request staged the photo; 200 with the same staged photo for a retry of it. */
    @PostMapping
    public ResponseEntity<PhotoUploadResponse> stagePhoto(
            @AuthenticationPrincipal AuthenticatedAccount owner,
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String uploadKey,
            @RequestPart(name = PHOTO_PART, required = false) MultipartFile photo) {
        StagedPhoto staged = stagePhotoUploadService.stage(new StagePhotoCommand(owner.accountId(), uploadKey,
                photo == null ? null : new MultipartReceivedPhoto(photo)));
        HttpStatus status = staged.newlyStaged() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(PhotoUploadResponse.from(staged.upload()));
    }

    /** Status review, for example after an interrupted upload or confirmation. */
    @GetMapping("/{photoUploadId}")
    public PhotoUploadResponse viewPhotoUpload(@AuthenticationPrincipal AuthenticatedAccount owner,
                                               @PathVariable String photoUploadId) {
        return PhotoUploadResponse.from(viewPhotoUploadService.view(owner.accountId(), photoUploadId));
    }

    /** The staged photo itself, for review before confirmation (AC-03). Only while READY. */
    @GetMapping("/{photoUploadId}/content")
    public ResponseEntity<Resource> photoUploadContent(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                       @PathVariable String photoUploadId) {
        return PhotoResponses.image(viewPhotoUploadService.openPending(owner.accountId(), photoUploadId));
    }

    /** Cancels an unconfirmed staged photo (AC-03, FR-AI-004). Cancelling twice is fine. */
    @DeleteMapping("/{photoUploadId}")
    public ResponseEntity<Void> cancelPhotoUpload(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                  @PathVariable String photoUploadId) {
        cancelPhotoUploadService.cancel(owner.accountId(), photoUploadId);
        return ResponseEntity.noContent().build();
    }
}