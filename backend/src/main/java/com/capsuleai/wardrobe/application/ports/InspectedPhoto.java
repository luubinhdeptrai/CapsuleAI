package com.capsuleai.wardrobe.application.ports;

import java.util.Objects;

/**
 * The result of genuinely decoding an accepted-format photo (S3-IMG-06): the detected format, the
 * original pixel dimensions (used for the 512-pixel rule) and the normalized JPEG to store, with
 * its own dimensions (orientation applied, metadata removed, long edge bounded; OPEN-S3-10).
 */
public record InspectedPhoto(
        String sourceFormat,
        int originalWidth,
        int originalHeight,
        int width,
        int height,
        byte[] jpeg) {

    public InspectedPhoto {
        Objects.requireNonNull(sourceFormat, "sourceFormat");
        Objects.requireNonNull(jpeg, "jpeg");
        if (originalWidth < 1 || originalHeight < 1 || width < 1 || height < 1 || jpeg.length == 0) {
            throw new IllegalArgumentException("An inspected photo has positive dimensions and content");
        }
    }
}