package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.GarmentPhoto;

/** The attached photo's metadata; its bytes are at GET /api/v1/wardrobe/garments/{garmentId}/photo. */
public record GarmentPhotoResponse(String contentType, int width, int height) {

    static GarmentPhotoResponse from(GarmentPhoto photo) {
        return new GarmentPhotoResponse(photo.contentType(), photo.width(), photo.height());
    }
}