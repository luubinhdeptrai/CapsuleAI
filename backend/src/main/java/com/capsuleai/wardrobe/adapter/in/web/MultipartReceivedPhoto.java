package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ReceivedPhoto;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

/** The uploaded photo part as {@link ReceivedPhoto}. The client file name and declared type are ignored. */
record MultipartReceivedPhoto(MultipartFile file) implements ReceivedPhoto {

    MultipartReceivedPhoto {
        Objects.requireNonNull(file, "file");
    }

    @Override
    public long sizeBytes() {
        return file.getSize();
    }

    @Override
    public InputStream openStream() throws IOException {
        return file.getInputStream();
    }
}