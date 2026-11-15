package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.support.TestImages;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * US-GAR-003 (S3-IMG-06, OPEN-S3-08/11/12): real HEIC/HEIF decoding through libheif. Fixtures are synthetic
 * images encoded with heif-enc at test time. With CAPSULEAI_REQUIRE_HEIF=true (CI) a missing toolchain
 * fails the build; otherwise these tests are skipped locally. A green run is the automated part of
 * verification V4/V6/V8 on that machine; real device photos remain manual evidence (V3).
 */
class HeifDecodingIT {

    private static final boolean REQUIRED = "true".equalsIgnoreCase(System.getenv("CAPSULEAI_REQUIRE_HEIF"));
    private static final String DECODER = environment("CAPSULE_HEIF_DECODER", "heif-dec");
    private static final String INFO = environment("CAPSULE_HEIF_INFO", "heif-info");
    private static final String ENCODER = environment("CAPSULE_HEIF_ENCODER", "heif-enc");

    /** The shipped default: the OPEN-S3-11 temporary fail-closed fallback. */
    private static ImageIoPhotoInspector rejecting;
    /** The approved OPEN-S3-11 target: the primary image of a multi-image file. */
    private static ImageIoPhotoInspector selecting;

    @TempDir
    Path work;

    @BeforeAll
    static void requireLibheif() {
        boolean available = starts(DECODER) && starts(INFO) && starts(ENCODER);
        if (REQUIRED) {
            assertThat(available)
                    .as("libheif's %s, %s and %s are installed (CAPSULEAI_REQUIRE_HEIF=true)", DECODER, INFO, ENCODER)
                    .isTrue();
        } else {
            assumeTrue(available, "libheif is not installed; set CAPSULEAI_REQUIRE_HEIF=true to fail instead");
        }
        rejecting = inspector(false);
        selecting = inspector(true);
    }

    @AfterAll
    static void stopInspectors() {
        if (rejecting != null) {
            rejecting.close();
        }
        if (selecting != null) {
            selecting.close();
        }
    }

    @Test
    void aRealHeicPhotoIsDecodedNormalizedAndMeasured() throws Exception {
        byte[] heic = encode("single.heic", TestImages.png(640, 800));

        InspectedPhoto photo = rejecting.inspectAndNormalize(TestImages.received(heic));

        assertThat(photo.sourceFormat()).isEqualTo("HEIF");
        assertThat(photo.originalWidth()).isEqualTo(640);
        assertThat(photo.originalHeight()).isEqualTo(800);
        assertThat(TestImages.isRed(TestImages.decode(photo.jpeg()), 50, 50)).as("orientation kept").isTrue();
    }

    @Test
    void whileSelectionIsDisabledATwoImageHeifIsRejectedExplicitly() throws Exception {
        byte[] twoImages = encode("two.heic", TestImages.png(640, 800), TestImages.png(800, 640));

        assertThatThrownBy(() -> rejecting.inspectAndNormalize(TestImages.received(twoImages)))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.MULTIPLE_IMAGES));
    }

    @Test
    void withSelectionEnabledThePrimaryImageIsUsedWhenItIsListedFirst() throws Exception {
        byte[] twoImages = encode("two.heic", TestImages.png(640, 800), TestImages.png(800, 640));

        InspectedPhoto photo = selecting.inspectAndNormalize(TestImages.received(twoImages));

        assertThat(photo.originalWidth()).as("heif-enc makes its first input primary").isEqualTo(640);
        assertThat(photo.originalHeight()).isEqualTo(800);
    }

    @Test
    void withSelectionEnabledThePrimaryImageIsUsedWhenItIsNotTheFirstDecodedImage() throws Exception {
        Path file = work.resolve("second-primary.heic");
        Files.write(file, encode("two.heic", TestImages.png(640, 800), TestImages.png(800, 640)));
        List<HeifDecoder.TopLevelImage> encoded = list(file);
        assertThat(encoded).extracting(HeifDecoder.TopLevelImage::primary).containsExactly(true, false);

        Files.write(file, withPrimaryItem(Files.readAllBytes(file), encoded.get(1).itemId()));

        assertThat(list(file))
                .as("libheif keeps file order, so the primary is now the second listed and decoded image")
                .extracting(HeifDecoder.TopLevelImage::itemId, HeifDecoder.TopLevelImage::primary)
                .containsExactly(tuple(encoded.get(0).itemId(), false), tuple(encoded.get(1).itemId(), true));
        InspectedPhoto photo = selecting.inspectAndNormalize(TestImages.received(Files.readAllBytes(file)));
        assertThat(photo.originalWidth()).as("the primary 800 x 640 image, not the first decoded one").isEqualTo(800);
        assertThat(photo.originalHeight()).isEqualTo(640);
    }

    @Test
    void aTruncatedHeicIsUnreadable() throws Exception {
        byte[] truncated = TestImages.truncated(encode("cut.heic", TestImages.png(640, 800)), 0.6);

        assertThatThrownBy(() -> rejecting.inspectAndNormalize(TestImages.received(truncated)))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.UNREADABLE));
    }

    private static ImageIoPhotoInspector inspector(boolean primaryImageSelection) {
        HeifDecoder decoder = new HeifDecoder(DECODER, INFO, Duration.ofSeconds(20), 250_000_000L,
                primaryImageSelection);
        return new ImageIoPhotoInspector(decoder, 2048, 250_000_000, Duration.ofSeconds(20), 2);
    }

    /** heif-enc [inputs...] -o output; several inputs become several top-level images (first is primary). */
    private byte[] encode(String output, byte[]... pngs) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of(ENCODER));
        for (int index = 0; index < pngs.length; index++) {
            Path input = work.resolve("input-" + index + ".png");
            Files.write(input, pngs[index]);
            command.add(input.toString());
        }
        Path target = work.resolve(output);
        command.addAll(List.of("-o", target.toString()));
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("heif-enc finished").isTrue();
        assertThat(process.exitValue()).as("heif-enc exit code").isZero();
        return Files.readAllBytes(target);
    }

    /** heif-info's top-level image lines for a file, read with the production parser. */
    private List<HeifDecoder.TopLevelImage> list(Path heif) throws IOException, InterruptedException {
        Path listing = work.resolve("listing.txt");
        Process process = new ProcessBuilder(INFO, heif.toString())
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .redirectOutput(listing.toFile())
                .start();
        assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("heif-info finished").isTrue();
        assertThat(process.exitValue()).as("heif-info exit code").isZero();
        return HeifDecoder.parseTopLevelImages(Files.readAllLines(listing, StandardCharsets.ISO_8859_1));
    }

    /**
     * TEST-ONLY: a copy of a HEIF file whose primary item (the 'pitm' box inside the top-level 'meta'
     * box) is the given item. The box keeps its size, so every other offset stays valid. heif-enc cannot
     * produce a file whose primary image is not its first image, so the test builds one this way.
     */
    private static byte[] withPrimaryItem(byte[] heif, long itemId) {
        byte[] patched = heif.clone();
        ByteBuffer file = ByteBuffer.wrap(patched);
        int meta = findBox(file, 0, patched.length, "meta");
        int metaEnd = meta + (int) boxSize(file, meta, patched.length);
        // 'meta' is a full box: an 8-byte header and 4 bytes of version and flags precede its children.
        int pitm = findBox(file, meta + 12, metaEnd, "pitm");
        int version = patched[pitm + 8] & 0xFF;
        if (version == 0) {
            assertThat(itemId).as("pitm version 0 stores a 16-bit item id").isLessThanOrEqualTo(0xFFFF);
            file.putShort(pitm + 12, (short) itemId);
        } else {
            file.putInt(pitm + 12, (int) itemId);
        }
        return patched;
    }

    private static int findBox(ByteBuffer file, int from, int to, String type) {
        int offset = from;
        while (offset + 8 <= to) {
            long size = boxSize(file, offset, to);
            if (type.equals(new String(file.array(), offset + 4, 4, StandardCharsets.US_ASCII))) {
                return offset;
            }
            offset += (int) size;
        }
        throw new AssertionError("No '" + type + "' box in the encoded HEIF file");
    }

    /** ISO-BMFF box size including its header: 1 means a 64-bit size follows, 0 means "to the end". */
    private static long boxSize(ByteBuffer file, int offset, int end) {
        long size = Integer.toUnsignedLong(file.getInt(offset));
        if (size == 1) {
            size = file.getLong(offset + 8);
        } else if (size == 0) {
            size = end - offset;
        }
        if (size < 8 || offset + size > end) {
            throw new AssertionError("Malformed ISO-BMFF box at offset " + offset);
        }
        return size;
    }

    /** Whether the tool can be started at all (its exit code for --help varies between versions). */
    private static boolean starts(String tool) {
        try {
            Process process = new ProcessBuilder(tool, "--help").redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            process.waitFor(10, TimeUnit.SECONDS);
            process.destroyForcibly();
            return true;
        } catch (IOException | InterruptedException notAvailable) {
            return false;
        }
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
