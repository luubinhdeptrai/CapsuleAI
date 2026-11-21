package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.RemoveGarmentCommand;
import java.util.UUID;

/**
 * JSON body of POST /api/v1/wardrobe/garments/{garmentId}/removal (US-GAR-005, OPEN-S4-01): the explicit
 * confirmation and the revision the user reviewed. There is no owner field and no removal time: the
 * owner is the authenticated account and the server records the time of acceptance.
 */
public record GarmentRemovalRequest(Boolean confirmed, String expectedRevision) {

    RemoveGarmentCommand toCommand(UUID ownerAccountId, String garmentId) {
        return new RemoveGarmentCommand(ownerAccountId, garmentId, confirmed, expectedRevision);
    }
}