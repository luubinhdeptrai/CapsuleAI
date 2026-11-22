package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.Garment;
import java.util.Optional;
import java.util.UUID;

/**
 * Provisional US-GAR-006 (S4-GAR-05, S4-INT-01): the garment side of attaching a staged photo to an existing
 * garment. The garment is locked before any photo lock, so attachment, removal, edits and competing
 * attachments meet at the garment row first. Call both methods inside {@link WardrobeTransactions}.
 */
public interface GarmentPhotoAttachmentRepository {

    /**
     * The owner's garment in any state, with its attached photo if any, locked until the surrounding
     * transaction ends. Another owner's garment is never returned.
     */
    Optional<Garment> lockOwnedGarment(UUID ownerAccountId, UUID garmentId);

    /**
     * Advances the revision of the owner's current garment by one, only while it is at
     * {@code expectedRevision}. @return false if nothing changed
     */
    boolean advanceRevisionIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision);
}