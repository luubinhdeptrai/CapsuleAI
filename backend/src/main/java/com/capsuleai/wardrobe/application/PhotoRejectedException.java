package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.util.Objects;

/**
 * The photo was not accepted and nothing was stored (FR-AI-012, ERR-AI-001). The user can
 * replace it or continue with imageless manual entry. The message never echoes photo content.
 */
public final class PhotoRejectedException extends RuntimeException {

    private final PhotoRejectionReason reason;

    public PhotoRejectedException(PhotoRejectionReason reason) {
        super("Photo not accepted: " + reason);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public PhotoRejectionReason reason() {
        return reason;
    }
}