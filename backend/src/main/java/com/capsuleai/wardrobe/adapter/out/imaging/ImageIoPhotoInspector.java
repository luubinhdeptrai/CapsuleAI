package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ReceivedPhoto;
import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * Genuine, bounded photo validation and normalization (S3-IMG-06; OPEN-S3-08/10/11/12).
 *
 * <ul>
 *   <li>The format comes from the bytes: JPEG and PNG signatures, or an ISO-BMFF {@code ftyp} box with
 *       an HEVC image brand. File names and declared content types are never consulted.</li>
 *   <li>JPEG and PNG are decoded by Java ImageIO; HEIC/HEIF by {@link HeifDecoder} into a PNG of the
 *       file's single (or, once selection is enabled, primary) image that is then decoded the same way.
 *       A multi-image HEIF is otherwise rejected explicitly (OPEN-S3-11).</li>
 *   <li>The declared size is read from the header first; above the technical pixel ceiling the photo
 *       is refused before any pixel is allocated. Decoding subsamples so the long edge of the result is
 *       at most the derivative bound, which bounds memory without rejecting large photos.</li>
 *   <li>A watchdog aborts a decode that exceeds the timeout; any decoder warning (for example a
 *       truncated JPEG) means the photo could not be read completely.</li>
 *   <li>The derivative is an opaque RGB JPEG written without metadata (no EXIF, no GPS), with JPEG
 *       orientation applied.</li>
 * </ul>
 */
public final class ImageIoPhotoInspector implements PhotoInspector, AutoCloseable {

    private static final int SNIFF_BYTES = 256;
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final Set<String> HEVC_IMAGE_BRANDS = Set.of("heic", "heix", "hevc", "hevx", "heim", "heis");
    private static final Set<String> STRUCTURAL_BRANDS = Set.of("mif1", "msf1");
    private static final float JPEG_QUALITY = 0.9f;

    private final HeifDecoder heifDecoder;
    private final int derivativeLongEdge;
    private final long maximumDeclaredPixels;
    private final Duration decodeTimeout;
    private final Semaphore decodeSlots;
    private final ScheduledExecutorService watchdog;

    public ImageIoPhotoInspector(HeifDecoder heifDecoder, int derivativeLongEdge, long maximumDeclaredPixels,
                                 Duration decodeTimeout, int maximumConcurrentDecodes) {
        this.heifDecoder = Objects.requireNonNull(heifDecoder, "heifDecoder");
        this.derivativeLongEdge = derivativeLongEdge;
        this.maximumDeclaredPixels = maximumDeclaredPixels;
        this.decodeTimeout = Objects.requireNonNull(decodeTimeout, "decodeTimeout");
        this.decodeSlots = new Semaphore(maximumConcurrentDecodes, true);
        this.watchdog = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "photo-decode-watchdog");
            thread.setDaemon(true);
            return thread;
        });
        if (derivativeLongEdge < 1 || maximumDeclaredPixels < 1 || maximumConcurrentDecodes < 1) {
            throw new IllegalArgumentException("Photo decoding limits must be positive");
        }
    }

    @Override
    public InspectedPhoto inspectAndNormalize(ReceivedPhoto photo) {
        acquireDecodeSlot();
        try {
            byte[] head = head(photo);
            if (isJpeg(head)) {
                int orientation = jpegOrientation(photo);
                try (InputStream content = photo.openStream()) {
                    return normalize(new MemoryCacheImageInputStream(content), "JPEG", orientation);
                }
            }
            if (isPng(head)) {
                try (InputStream content = photo.openStream()) {
                    return normalize(new MemoryCacheImageInputStream(content), "PNG", JpegOrientation.NORMAL);
                }
            }
            if (isHeif(head)) {
                try (HeifDecoder.DecodedHeif decoded = heifDecoder.decode(photo)) {
                    return normalize(new FileImageInputStream(decoded.png().toFile()), "HEIF", JpegOrientation.NORMAL);
                }
            }
            throw new PhotoRejectedException(PhotoRejectionReason.UNSUPPORTED_FORMAT);
        } catch (IOException unreadable) {
            throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
        } finally {
            decodeSlots.release();
        }
    }

    @Override
    public void close() {
        watchdog.shutdownNow();
    }

    private InspectedPhoto normalize(ImageInputStream input, String format, int orientation) throws IOException {
        try (input) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
            ImageReader reader = readers.next();
            AtomicBoolean timedOut = new AtomicBoolean();
            AtomicBoolean warned = new AtomicBoolean();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || (long) width * height > maximumDeclaredPixels) {
                    throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
                }
                int step = Math.max(1, (int) Math.ceil(Math.max(width, height) / (double) derivativeLongEdge));
                ImageReadParam parameters = reader.getDefaultReadParam();
                parameters.setSourceSubsampling(step, step, 0, 0);
                reader.addIIOReadWarningListener((source, warning) -> warned.set(true));
                ScheduledFuture<?> deadline = watchdog.schedule(() -> {
                    timedOut.set(true);
                    reader.abort();
                }, decodeTimeout.toMillis(), TimeUnit.MILLISECONDS);
                BufferedImage decoded;
                try {
                    decoded = reader.read(0, parameters);
                } finally {
                    deadline.cancel(false);
                }
                if (decoded == null || timedOut.get() || warned.get()) {
                    throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
                }
                BufferedImage normalized = JpegOrientation.apply(opaqueRgb(decoded), orientation);
                return new InspectedPhoto(format, width, height, normalized.getWidth(), normalized.getHeight(),
                        jpeg(normalized));
            } catch (PhotoRejectedException rejected) {
                throw rejected;
            } catch (RuntimeException malformed) {
                // ImageIO can throw unchecked exceptions for crafted input; it is still unreadable input.
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            } finally {
                reader.dispose();
            }
        }
    }

    private void acquireDecodeSlot() {
        try {
            if (!decodeSlots.tryAcquire(decodeTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new PhotoProcessingUnavailableException("Every photo decoding slot is busy", null);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new PhotoProcessingUnavailableException("Interrupted while waiting to decode a photo", interrupted);
        }
    }

    private static byte[] head(ReceivedPhoto photo) {
        try (InputStream content = photo.openStream()) {
            return content.readNBytes(SNIFF_BYTES);
        } catch (IOException failure) {
            throw new UncheckedIOException("The received photo could not be read", failure);
        }
    }

    private static int jpegOrientation(ReceivedPhoto photo) {
        try (InputStream content = photo.openStream()) {
            return JpegOrientation.read(content);
        } catch (IOException failure) {
            return JpegOrientation.NORMAL;
        }
    }

    static boolean isJpeg(byte[] head) {
        return head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
    }

    static boolean isPng(byte[] head) {
        return head.length >= PNG_SIGNATURE.length
                && Arrays.equals(head, 0, PNG_SIGNATURE.length, PNG_SIGNATURE, 0, PNG_SIGNATURE.length);
    }

    /** An ISO-BMFF ftyp box whose major brand, or (for mif1/msf1) a compatible brand, is an HEVC image brand. */
    static boolean isHeif(byte[] head) {
        if (head.length < 16 || !"ftyp".equals(ascii(head, 4))) {
            return false;
        }
        String majorBrand = ascii(head, 8);
        if (HEVC_IMAGE_BRANDS.contains(majorBrand)) {
            return true;
        }
        if (!STRUCTURAL_BRANDS.contains(majorBrand)) {
            return false;
        }
        long boxSize = ((head[0] & 0xFFL) << 24) | ((head[1] & 0xFFL) << 16) | ((head[2] & 0xFFL) << 8) | (head[3] & 0xFFL);
        int end = (int) Math.min(boxSize, head.length);
        for (int offset = 16; offset + 4 <= end; offset += 4) {
            if (HEVC_IMAGE_BRANDS.contains(ascii(head, offset))) {
                return true;
            }
        }
        return false;
    }

    private static String ascii(byte[] bytes, int offset) {
        return new String(bytes, offset, 4, StandardCharsets.US_ASCII);
    }

    /** Flattens transparency onto white and converts every colour model to 8-bit RGB. */
    private static BufferedImage opaqueRgb(BufferedImage decoded) {
        BufferedImage rgb = new BufferedImage(decoded.getWidth(), decoded.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            graphics.drawImage(decoded, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return rgb;
    }

    /** A baseline JPEG with no metadata at all: IIOImage without metadata writes only the JFIF header. */
    private static byte[] jpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(bytes)) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(image, null, null), parameters);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }
}