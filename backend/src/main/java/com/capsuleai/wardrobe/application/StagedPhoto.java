package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.Objects;

/** The staged photo, and whether this request staged it ({@code false}: a retry of the same staging). */
public record StagedPhoto(PhotoUpload upload, boolean newlyStaged) {

    public StagedPhoto {
        Objects.requireNonNull(upload, "upload");
    }
}