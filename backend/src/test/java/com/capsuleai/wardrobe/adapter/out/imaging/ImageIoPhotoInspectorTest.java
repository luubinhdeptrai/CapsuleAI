package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ReceivedPhoto;
import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.support.TestImages;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-003 (S3-IMG-06, FR-AI-012): genuine decoding from the bytes, bounded resources, orientation and
 * metadata stripping. JPEG and PNG only; real HEIC/HEIF decoding is HeifDecodingIT.
 */
class ImageIoPhotoInspectorTest {

    private final ImageIoPhotoInspector inspector = inspector(250_000_000);

    @AfterEach
    void stopWatchdog() {
        inspector.close();
    }

    @Test
    void aGenuineJpegIsAcceptedWithItsOriginalDimensions() {
        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(TestImages.jpeg(1200, 900)));

        assertThat(photo.sourceFormat()).isEqualTo("JPEG");
        assertThat(photo.originalWidth()).isEqualTo(1200);
        assertThat(photo.originalHeight()).isEqualTo(900);
        assertThat(TestImages.decode(photo.jpeg()).getWidth()).isEqualTo(1200);
    }

    @Test
    void aGenuinePngIsAcceptedAndStoredAsJpeg() {
        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(TestImages.png(700, 512)));

        assertThat(photo.sourceFormat()).isEqualTo("PNG");
        assertThat(photo.originalHeight()).isEqualTo(512);
        assertThat(photo.jpeg()[0] & 0xFF).isEqualTo(0xFF);
        assertThat(photo.jpeg()[1] & 0xFF).isEqualTo(0xD8);
    }

    @Test
    void theFormatComesFromTheBytesAloneAndOtherFormatsAreUnsupported() {
        assertRejected(TestImages.gif(800, 800), PhotoRejectionReason.UNSUPPORTED_FORMAT);
        assertRejected("this is not a photo".getBytes(StandardCharsets.UTF_8), PhotoRejectionReason.UNSUPPORTED_FORMAT);
    }

    @Test
    void aTruncatedJpegOrPngIsUnreadable() {
        assertRejected(TestImages.truncated(TestImages.jpeg(900, 900), 0.5), PhotoRejectionReason.UNREADABLE);
        assertRejected(TestImages.truncated(TestImages.png(900, 900), 0.5), PhotoRejectionReason.UNREADABLE);
    }

    @Test
    void jpegOrientationIsAppliedToTheStoredPhoto() {
        InspectedPhoto photo = inspector.inspectAndNormalize(
                TestImages.received(TestImages.jpegWithExif(600, 800, 6)));

        BufferedImage stored = TestImages.decode(photo.jpeg());
        assertThat(photo.originalWidth()).as("the rule uses the original header size").isEqualTo(600);
        assertThat(stored.getWidth()).isEqualTo(800);
        assertThat(stored.getHeight()).isEqualTo(600);
        assertThat(TestImages.isRed(stored, 700, 100)).as("red quadrant rotated to the top right").isTrue();
        assertThat(TestImages.isRed(stored, 100, 100)).isFalse();
    }

    @Test
    void theStoredPhotoCarriesNoExifOrLocationMetadata() {
        byte[] withExif = TestImages.jpegWithExif(800, 600, 1);
        assertThat(TestImages.containsAscii(withExif, "GPSLatitude")).isTrue();

        byte[] stored = inspector.inspectAndNormalize(TestImages.received(withExif)).jpeg();

        assertThat(TestImages.containsAscii(stored, "Exif")).isFalse();
        assertThat(TestImages.containsAscii(stored, "GPSLatitude")).isFalse();
    }

    @Test
    void largePhotosAreDownscaledSoTheLongEdgeIsAtMost2048() {
        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(TestImages.jpeg(4100, 1000)));

        assertThat(photo.originalWidth()).isEqualTo(4100);
        assertThat(Math.max(photo.width(), photo.height())).isLessThanOrEqualTo(2048);
        assertThat(TestImages.decode(photo.jpeg()).getWidth()).isEqualTo(photo.width());
    }

    @Test
    void aPhotoDeclaringMorePixelsThanTheSafetyCeilingIsRefusedBeforeDecoding() {
        try (ImageIoPhotoInspector strict = inspector(1_000_000)) {
            assertThatThrownBy(() -> strict.inspectAndNormalize(TestImages.received(TestImages.png(1100, 1000))))
                    .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                            assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.UNREADABLE));
        }
    }

    @Test
    void aHeifPhotoWithoutAnInstalledDecoderIsTemporarilyUnavailableNotUnsupported() {
        assertThatThrownBy(() -> inspector.inspectAndNormalize(
                TestImages.received(TestImages.isoMediaHeader("heic"))))
                .isInstanceOf(PhotoProcessingUnavailableException.class);
    }

    @Test
    void anIsoMediaFileThatIsNotHeifIsUnsupported() {
        assertRejected(TestImages.isoMediaHeader("avif"), PhotoRejectionReason.UNSUPPORTED_FORMAT);
    }

    /** OPEN-S3-12: busy decode slots are a retryable 503, never a claim that the photo is unreadable. */
    @Test
    void whenEveryDecodeSlotIsBusyAPhotoIsTemporarilyUnavailableNotUnreadable() throws Exception {
        byte[] png = TestImages.png(600, 600);
        CountDownLatch slotTaken = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ReceivedPhoto heldPhoto = new ReceivedPhoto() {
            @Override
            public long sizeBytes() {
                return png.length;
            }

            @Override
            public InputStream openStream() {
                slotTaken.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
                return new ByteArrayInputStream(png);
            }
        };
        HeifDecoder missingTools = new HeifDecoder("capsuleai-missing-heif-decoder", "capsuleai-missing-heif-info",
                Duration.ofSeconds(5), 250_000_000L, false);
        try (ImageIoPhotoInspector oneSlot = new ImageIoPhotoInspector(missingTools, 2048, 250_000_000,
                Duration.ofMillis(300), 1)) {
            CompletableFuture<InspectedPhoto> holder =
                    CompletableFuture.supplyAsync(() -> oneSlot.inspectAndNormalize(heldPhoto));
            assertThat(slotTaken.await(5, TimeUnit.SECONDS)).as("the only slot is taken").isTrue();

            assertThatThrownBy(() -> oneSlot.inspectAndNormalize(TestImages.received(png)))
                    .isInstanceOf(PhotoProcessingUnavailableException.class);

            release.countDown();
            holder.exceptionally(failure -> null).get(10, TimeUnit.SECONDS);
        }
    }

    private void assertRejected(byte[] bytes, PhotoRejectionReason reason) {
        assertThatThrownBy(() -> inspector.inspectAndNormalize(TestImages.received(bytes)))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(reason));
    }

    /** The libheif commands deliberately do not exist, so no test here depends on libheif. */
    private static ImageIoPhotoInspector inspector(long maximumDeclaredPixels) {
        HeifDecoder missingTools = new HeifDecoder("capsuleai-missing-heif-decoder", "capsuleai-missing-heif-info",
                Duration.ofSeconds(5), maximumDeclaredPixels, false);
        return new ImageIoPhotoInspector(missingTools, 2048, maximumDeclaredPixels, Duration.ofSeconds(20), 2);
    }
}
