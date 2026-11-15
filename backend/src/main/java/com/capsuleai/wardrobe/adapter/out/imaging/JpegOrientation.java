package com.capsuleai.wardrobe.adapter.out.imaging;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the EXIF orientation (TIFF tag 0x0112, values 1–8) of a JPEG and applies it to decoded
 * pixels, so the stored derivative shows the garment the right way up without keeping any EXIF
 * (S3-IMG-06). Anything unexpected means "normal orientation": orientation never rejects a photo.
 */
final class JpegOrientation {

    static final int NORMAL = 1;

    private static final int START_OF_IMAGE = 0xFFD8;
    private static final int APP1 = 0xFFE1;
    private static final int START_OF_SCAN = 0xFFDA;
    private static final int END_OF_IMAGE = 0xFFD9;
    private static final int ORIENTATION_TAG = 0x0112;

    private JpegOrientation() {
    }

    /** The EXIF orientation of a JPEG stream, or {@link #NORMAL}. Does not close the stream. */
    static int read(InputStream jpeg) {
        try {
            DataInputStream data = new DataInputStream(jpeg);
            if (data.readUnsignedShort() != START_OF_IMAGE) {
                return NORMAL;
            }
            while (true) {
                int marker = data.readUnsignedShort();
                if ((marker & 0xFF00) != 0xFF00 || marker == START_OF_SCAN || marker == END_OF_IMAGE) {
                    return NORMAL;
                }
                int length = data.readUnsignedShort();
                if (length < 2) {
                    return NORMAL;
                }
                if (marker == APP1) {
                    byte[] segment = new byte[length - 2];
                    data.readFully(segment);
                    int orientation = exifOrientation(segment);
                    if (orientation != 0) {
                        return orientation;
                    }
                } else {
                    skipFully(data, length - 2);
                }
            }
        } catch (IOException | RuntimeException unreadable) {
            return NORMAL;
        }
    }

    /** The orientation in one APP1 segment, or 0 if it is not EXIF or has no valid orientation. */
    static int exifOrientation(byte[] segment) {
        if (segment.length < 14 || segment[0] != 'E' || segment[1] != 'x' || segment[2] != 'i'
                || segment[3] != 'f' || segment[4] != 0 || segment[5] != 0) {
            return 0;
        }
        int tiff = 6;
        boolean littleEndian = segment[tiff] == 'I' && segment[tiff + 1] == 'I';
        boolean bigEndian = segment[tiff] == 'M' && segment[tiff + 1] == 'M';
        if (!littleEndian && !bigEndian) {
            return 0;
        }
        long firstDirectory = tiff + unsignedInt(segment, tiff + 4, littleEndian);
        if (firstDirectory < tiff || firstDirectory + 2 > segment.length) {
            return 0;
        }
        int directory = (int) firstDirectory;
        int entries = unsignedShort(segment, directory, littleEndian);
        for (int index = 0; index < entries; index++) {
            int entry = directory + 2 + index * 12;
            if (entry + 12 > segment.length) {
                return 0;
            }
            if (unsignedShort(segment, entry, littleEndian) == ORIENTATION_TAG) {
                int value = unsignedShort(segment, entry + 8, littleEndian);
                return value >= 1 && value <= 8 ? value : 0;
            }
        }
        return 0;
    }

    /** The image as it should be displayed for an EXIF orientation (2–8 transform; others return it unchanged). */
    static BufferedImage apply(BufferedImage source, int orientation) {
        if (orientation < 2 || orientation > 8) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        boolean swapsSides = orientation >= 5;
        BufferedImage target = new BufferedImage(swapsSides ? height : width, swapsSides ? width : height,
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int targetX;
                int targetY;
                switch (orientation) {
                    case 2 -> { targetX = width - 1 - x; targetY = y; }               // mirror horizontally
                    case 3 -> { targetX = width - 1 - x; targetY = height - 1 - y; }  // rotate 180°
                    case 4 -> { targetX = x; targetY = height - 1 - y; }              // mirror vertically
                    case 5 -> { targetX = y; targetY = x; }                           // transpose
                    case 6 -> { targetX = height - 1 - y; targetY = x; }              // rotate 90° clockwise
                    case 7 -> { targetX = height - 1 - y; targetY = width - 1 - x; }  // transverse
                    default -> { targetX = y; targetY = width - 1 - x; }              // 8: rotate 90° anticlockwise
                }
                target.setRGB(targetX, targetY, source.getRGB(x, y));
            }
        }
        return target;
    }

    private static void skipFully(DataInputStream data, int count) throws IOException {
        int remaining = count;
        while (remaining > 0) {
            int skipped = data.skipBytes(remaining);
            if (skipped <= 0) {
                throw new EOFException("Truncated JPEG segment");
            }
            remaining -= skipped;
        }
    }

    private static int unsignedShort(byte[] bytes, int offset, boolean littleEndian) {
        int first = bytes[offset] & 0xFF;
        int second = bytes[offset + 1] & 0xFF;
        return littleEndian ? first | (second << 8) : (first << 8) | second;
    }

    private static long unsignedInt(byte[] bytes, int offset, boolean littleEndian) {
        long value = 0;
        for (int index = 0; index < 4; index++) {
            int shift = littleEndian ? 8 * index : 8 * (3 - index);
            value |= (long) (bytes[offset + index] & 0xFF) << shift;
        }
        return value;
    }
}