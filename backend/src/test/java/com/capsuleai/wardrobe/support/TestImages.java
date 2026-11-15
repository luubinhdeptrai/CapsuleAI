package com.capsuleai.wardrobe.support;

import com.capsuleai.wardrobe.application.ReceivedPhoto;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * TEST-ONLY synthetic images: no binary fixtures and no personal photos. Every image has a red
 * top-left quadrant on blue, so orientation handling can be checked by colour.
 */
public final class TestImages {

    private TestImages() {
    }

    public static BufferedImage quadrants(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.RED);
            graphics.fillRect(0, 0, width / 2, height / 2);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    public static byte[] jpeg(int width, int height) {
        return encode(quadrants(width, height), "jpeg");
    }

    public static byte[] png(int width, int height) {
        return encode(quadrants(width, height), "png");
    }

    public static byte[] gif(int width, int height) {
        return encode(quadrants(width, height), "gif");
    }

    /**
     * A JPEG whose APP1 EXIF block (placed after the JFIF header) carries an orientation tag and
     * location-like text, to check that orientation is applied and nothing of it is kept.
     */
    public static byte[] jpegWithExif(int width, int height, int orientation) {
        byte[] plain = jpeg(width, height);
        byte[] tiff = ByteBuffer.allocate(26)
                .put(new byte[] {'M', 'M', 0, 42, 0, 0, 0, 8})      // big-endian TIFF header, IFD0 at offset 8
                .putShort((short) 1)                                    // one directory entry
                .putShort((short) 0x0112).putShort((short) 3).putInt(1) // Orientation, SHORT, count 1
                .putShort((short) orientation).putShort((short) 0)      // value, padding
                .putInt(0)                                              // no next directory
                .array();
        byte[] marker = "GPSLatitude=10.7769;GPSLongitude=106.7009".getBytes(StandardCharsets.US_ASCII);
        byte[] segment = concat("Exif\0\0".getBytes(StandardCharsets.US_ASCII), tiff, marker);
        byte[] app1 = concat(new byte[] {(byte) 0xFF, (byte) 0xE1,
                (byte) ((segment.length + 2) >> 8), (byte) (segment.length + 2)}, segment);
        int afterApp0 = 4 + (((plain[4] & 0xFF) << 8) | (plain[5] & 0xFF)); // SOI + APP0 marker + APP0 length
        return concat(Arrays.copyOfRange(plain, 0, afterApp0), app1, Arrays.copyOfRange(plain, afterApp0, plain.length));
    }

    /** The bytes of an ISO-BMFF file type box with the given major brand, padded; not a decodable image. */
    public static byte[] isoMediaHeader(String majorBrand) {
        return ByteBuffer.allocate(64)
                .putInt(24).put("ftyp".getBytes(StandardCharsets.US_ASCII))
                .put(majorBrand.getBytes(StandardCharsets.US_ASCII)).putInt(0)
                .put("mif1".getBytes(StandardCharsets.US_ASCII)).put(majorBrand.getBytes(StandardCharsets.US_ASCII))
                .array();
    }

    public static byte[] truncated(byte[] image, double keptFraction) {
        return Arrays.copyOf(image, (int) (image.length * keptFraction));
    }

    public static ReceivedPhoto received(byte[] bytes) {
        return new ReceivedPhoto() {
            @Override
            public long sizeBytes() {
                return bytes.length;
            }

            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream(bytes);
            }
        };
    }

    public static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public static boolean containsAscii(byte[] haystack, String needle) {
        return new String(haystack, StandardCharsets.ISO_8859_1).contains(needle);
    }

    /** Whether the pixel is clearly red (lossy JPEG tolerance). */
    public static boolean isRed(BufferedImage image, int x, int y) {
        Color color = new Color(image.getRGB(x, y));
        return color.getRed() > 180 && color.getBlue() < 90;
    }

    private static byte[] encode(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (!ImageIO.write(image, format, bytes)) {
                throw new IllegalStateException("No ImageIO writer for " + format);
            }
            return bytes.toByteArray();
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream joined = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            joined.writeBytes(part);
        }
        return joined.toByteArray();
    }
}
