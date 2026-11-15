package com.capsuleai.wardrobe.application;

import java.io.IOException;
import java.io.InputStream;

/**
 * The bytes of one uploaded photo as the web adapter received them (US-GAR-003). Each call to
 * {@link #openStream()} reads from the start, so the size, hash, format sniffing and decoding can
 * each read independently. Nothing about the client's file name or declared type is included.
 */
public interface ReceivedPhoto {

    /** The number of bytes the client sent. */
    long sizeBytes();

    /** A new stream over the received bytes; the caller closes it. */
    InputStream openStream() throws IOException;
}