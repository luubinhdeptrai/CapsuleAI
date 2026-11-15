package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-003 (OPEN-S3-11, OPEN-S3-12): how HeifDecoder reads heif-info's listing and chooses the image to
 * use, without libheif. HeifDecodingIT proves the same choices against the real tools.
 */
class HeifDecoderTest {

    private static final HeifDecoder.TopLevelImage FIRST = new HeifDecoder.TopLevelImage(640, 800, 1, false);
    private static final HeifDecoder.TopLevelImage SECOND_PRIMARY = new HeifDecoder.TopLevelImage(800, 640, 2, true);

    @Test
    void onlyTopLevelImageLinesAreReadInListedOrderWithThePrimaryMarker() {
        List<HeifDecoder.TopLevelImage> images = HeifDecoder.parseTopLevelImages(List.of(
                "MIME type: image/heic",
                "main brand: heic",
                "compatible brands: mif1, heic",
                "",
                "image: 640x800 (id=1)",
                "  colorspace: YCbCr, 4:2:0",
                "  thumbnail: 320x400",
                "image: 800x640 (id=2), primary",
                "  alpha channel: no"));

        assertThat(images).containsExactly(FIRST, SECOND_PRIMARY);
    }

    @Test
    void aSingleTopLevelImageNeedsNoChoice() {
        HeifDecoder.TopLevelImage only = new HeifDecoder.TopLevelImage(640, 800, 1, true);

        assertThat(HeifDecoder.selectImage(List.of(only), false)).isZero();
        assertThat(HeifDecoder.selectImage(List.of(only), true)).isZero();
    }

    @Test
    void withSelectionEnabledThePrimaryIsChosenEvenWhenItIsNotTheFirstImage() {
        assertThat(HeifDecoder.selectImage(List.of(FIRST, SECOND_PRIMARY), true)).isEqualTo(1);
    }

    @Test
    void withSelectionDisabledSeveralImagesAreRejectedExplicitly() {
        assertRejected(() -> HeifDecoder.selectImage(List.of(FIRST, SECOND_PRIMARY), false),
                PhotoRejectionReason.MULTIPLE_IMAGES);
    }

    @Test
    void aPrimaryThatIsNotExactlyOneImageFailsClosedInsteadOfGuessing() {
        HeifDecoder.TopLevelImage secondPlain = new HeifDecoder.TopLevelImage(800, 640, 2, false);
        HeifDecoder.TopLevelImage firstPrimary = new HeifDecoder.TopLevelImage(640, 800, 1, true);

        assertRejected(() -> HeifDecoder.selectImage(List.of(FIRST, secondPlain), true),
                PhotoRejectionReason.MULTIPLE_IMAGES);
        assertRejected(() -> HeifDecoder.selectImage(List.of(firstPrimary, SECOND_PRIMARY), true),
                PhotoRejectionReason.MULTIPLE_IMAGES);
        assertRejected(() -> HeifDecoder.selectImage(List.of(), true), PhotoRejectionReason.UNREADABLE);
    }

    @Test
    void everyListedImageMustBeWithinThePixelCeilingBeforeDecoding() {
        HeifDecoder.requireDeclaredPixelsWithin(List.of(FIRST, SECOND_PRIMARY), 640 * 800);

        assertRejected(() -> HeifDecoder.requireDeclaredPixelsWithin(List.of(FIRST, SECOND_PRIMARY), 640 * 800 - 1),
                PhotoRejectionReason.UNREADABLE);
    }

    @Test
    void decoderOutputsAreNamedInListedOrderFromOne() {
        assertThat(HeifDecoder.expectedOutputNames(1)).containsExactly("decoded.png");
        assertThat(HeifDecoder.expectedOutputNames(3))
                .containsExactly("decoded-1.png", "decoded-2.png", "decoded-3.png");
    }

    private static void assertRejected(Runnable action, PhotoRejectionReason reason) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                assertThat(rejected.reason()).isEqualTo(reason));
    }
}
