package com.capsuleai.wardrobe.application.ports;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/** An open stored photo. The receiver must close it (it holds a storage connection). */
public record StoredPhoto(InputStream content, long sizeBytes, String contentType) implements AutoCloseable {

    public StoredPhoto {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(contentType, "contentType");
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must not be negative");
        }
    }

    @Override
    public void close() throws IOException {
        content.close();
    }
}