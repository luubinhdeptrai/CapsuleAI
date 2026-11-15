package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Private photo responses (S3-IMG-05): streamed by the application after the owner check, never
 * cached by shared caches, never sniffed as another type. The stream is closed after it is written.
 */
final class PhotoResponses {

    private PhotoResponses() {
    }

    static ResponseEntity<Resource> image(StoredPhoto photo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .contentLength(photo.sizeBytes())
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(photo.content()));
    }
}