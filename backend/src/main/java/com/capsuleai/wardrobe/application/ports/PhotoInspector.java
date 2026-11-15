package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.application.ReceivedPhoto;

/**
 * Decides, from the bytes alone, whether a photo is a genuine JPEG, PNG or HEIC/HEIF image and
 * produces the normalized JPEG to store (S3-IMG-06, OPEN-S3-08/10/12). Implementations bound
 * memory, pixels, time and temporary files.
 */
public interface PhotoInspector {

    /**
     * @throws com.capsuleai.wardrobe.application.PhotoRejectedException if the photo is not accepted
     * @throws PhotoProcessingUnavailableException                        if it cannot be processed now
     */
    InspectedPhoto inspectAndNormalize(ReceivedPhoto photo);
}