package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ReceivedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Decodes HEIC/HEIF with libheif's command-line tools in separate, time-bounded processes
 * (S3-IMG-06 Option B; toolchain OPEN-S3-08, approved 2026-11-11, verification V1–V8 still required).
 * A decoder crash or runaway allocation cannot take down the JVM; libheif's default security limits
 * stay enabled (the {@code --disable-limits} option is never passed). heif-dec applies rotation and
 * mirroring by default and writes EXIF or XMP only when asked, and it is never asked.
 *
 * <p>One decode, inside one private temporary directory and one shared deadline (the decode timeout):
 * <ol>
 *   <li>{@code heif-info input.heif} lists the top-level images as {@code image: WxH (id=N)} lines and
 *       marks the primary image with {@code , primary}.</li>
 *   <li>Every listed image must be within the declared-pixel ceiling (OPEN-S3-12) before anything is
 *       decoded, because heif-dec decodes every top-level image.</li>
 *   <li>The image to use is chosen (OPEN-S3-11, below).</li>
 *   <li>{@code heif-dec input.heif decoded.png} writes {@code decoded.png} for one top-level image, or
 *       {@code decoded-1.png} … {@code decoded-N.png} in the same top-level order for N images.</li>
 * </ol>
 *
 * <p>Multi-image files (OPEN-S3-11, approved 2026-11-11). The approved product behaviour is the file's
 * primary image. heif-info and heif-dec walk the same libheif top-level list, so the position of the
 * primary line in heif-info's listing names heif-dec's numbered output. That mapping must be verified on
 * the deployed libheif (V4, V8) before it is relied on. Until then {@code primaryImageSelection} stays
 * {@code false} and a file with several top-level images is rejected explicitly as MULTIPLE_IMAGES: the
 * approved temporary fail-closed fallback. With selection enabled, any doubt fails closed the same way
 * (no primary line, several primary lines, or outputs that do not match the listing). An arbitrary
 * image is never chosen.
 */
public final class HeifDecoder {

    static final String INPUT = "input.heif";
    static final String OUTPUT = "decoded.png";
    private static final String OUTPUT_STEM = "decoded";
    private static final String LISTING = "heif-info.txt";
    private static final long MAXIMUM_LISTING_BYTES = 1_048_576;
    private static final Pattern TOP_LEVEL_IMAGE =
            Pattern.compile("^image: (\\d{1,9})x(\\d{1,9}) \\(id=(\\d{1,10})\\)(, primary)?\\s*$");

    private final String decoderCommand;
    private final String infoCommand;
    private final Duration timeout;
    private final long maximumDeclaredPixels;
    private final boolean primaryImageSelection;

    /**
     * @param decoderCommand        heif-dec (named heif-convert on some older distributions)
     * @param infoCommand           heif-info from the same libheif installation
     * @param timeout               one budget shared by both processes of a decode (OPEN-S3-12)
     * @param maximumDeclaredPixels technical ceiling applied to every top-level image (OPEN-S3-12)
     * @param primaryImageSelection OPEN-S3-11 target behaviour; enable only after V4 and V8 pass on the
     *                              deployed libheif. While false, several top-level images are rejected.
     */
    public HeifDecoder(String decoderCommand, String infoCommand, Duration timeout, long maximumDeclaredPixels,
                       boolean primaryImageSelection) {
        this.decoderCommand = Objects.requireNonNull(decoderCommand, "decoderCommand").strip();
        this.infoCommand = Objects.requireNonNull(infoCommand, "infoCommand").strip();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.maximumDeclaredPixels = maximumDeclaredPixels;
        this.primaryImageSelection = primaryImageSelection;
        if (timeout.isNegative() || timeout.isZero() || maximumDeclaredPixels < 1) {
            throw new IllegalArgumentException("HEIF decoding limits must be positive");
        }
    }

    /** One decoded HEIF image as a PNG file in a private directory, deleted on close. */
    public record DecodedHeif(Path directory, Path png) implements AutoCloseable {

        @Override
        public void close() {
            deleteRecursively(directory);
        }
    }

    /** One top-level image as heif-info lists it, in libheif's top-level order. */
    record TopLevelImage(long width, long height, long itemId, boolean primary) {
    }

    /**
     * @throws PhotoRejectedException              UNREADABLE (corrupt, truncated, above the pixel ceiling, too
     *                                             slow) or MULTIPLE_IMAGES (several images, no reliable choice)
     * @throws PhotoProcessingUnavailableException if a libheif tool is not installed or cannot be started
     */
    public DecodedHeif decode(ReceivedPhoto photo) {
        long deadline = System.nanoTime() + timeout.toNanos();
        Path directory = privateTemporaryDirectory();
        try {
            try (InputStream content = photo.openStream()) {
                Files.copy(content, directory.resolve(INPUT), StandardCopyOption.REPLACE_EXISTING);
            }

            Path listing = directory.resolve(LISTING);
            run(List.of(infoCommand, INPUT), directory, ProcessBuilder.Redirect.to(listing.toFile()), deadline);
            if (Files.size(listing) > MAXIMUM_LISTING_BYTES) {
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
            List<TopLevelImage> images =
                    parseTopLevelImages(Files.readAllLines(listing, StandardCharsets.ISO_8859_1));
            requireDeclaredPixelsWithin(images, maximumDeclaredPixels);
            int selected = selectImage(images, primaryImageSelection);

            run(List.of(decoderCommand, INPUT, OUTPUT), directory, ProcessBuilder.Redirect.DISCARD, deadline);
            List<String> expected = expectedOutputNames(images.size());
            Set<String> produced;
            try (Stream<Path> files = Files.list(directory)) {
                produced = files.map(file -> file.getFileName().toString())
                        .filter(name -> name.endsWith(".png"))
                        .collect(Collectors.toSet());
            }
            if (!produced.equals(Set.copyOf(expected))) {
                // heif-dec did not write exactly the images heif-info listed, so the mapping is not trusted.
                throw new PhotoRejectedException(images.size() > 1
                        ? PhotoRejectionReason.MULTIPLE_IMAGES
                        : PhotoRejectionReason.UNREADABLE);
            }
            return new DecodedHeif(directory, directory.resolve(expected.get(selected)));
        } catch (IOException ioFailure) {
            deleteRecursively(directory);
            throw new UncheckedIOException("HEIF decoding workspace failed", ioFailure);
        } catch (RuntimeException failure) {
            deleteRecursively(directory);
            throw failure;
        }
    }

    /** The heif-info lines that describe top-level images, in listed order; every other line is ignored. */
    static List<TopLevelImage> parseTopLevelImages(List<String> lines) {
        List<TopLevelImage> images = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = TOP_LEVEL_IMAGE.matcher(line);
            if (matcher.matches()) {
                images.add(new TopLevelImage(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)),
                        Long.parseLong(matcher.group(3)), matcher.group(4) != null));
            }
        }
        return List.copyOf(images);
    }

    /** heif-dec decodes every top-level image, so each one must be within the ceiling before it runs. */
    static void requireDeclaredPixelsWithin(List<TopLevelImage> images, long maximumDeclaredPixels) {
        for (TopLevelImage image : images) {
            if (image.width() < 1 || image.height() < 1 || image.width() * image.height() > maximumDeclaredPixels) {
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
        }
    }

    /**
     * The position, in heif-info's listing, of the image to use (OPEN-S3-11).
     *
     * @throws PhotoRejectedException UNREADABLE if no image is listed; MULTIPLE_IMAGES if several are listed
     *                                and selection is disabled, or no single image is marked primary
     */
    static int selectImage(List<TopLevelImage> images, boolean primaryImageSelection) {
        if (images.isEmpty()) {
            throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
        }
        if (images.size() == 1) {
            return 0;
        }
        if (!primaryImageSelection) {
            // OPEN-S3-11 temporary fail-closed fallback until primary selection is verified (V4, V8).
            throw new PhotoRejectedException(PhotoRejectionReason.MULTIPLE_IMAGES);
        }
        List<Integer> primaries = IntStream.range(0, images.size())
                .filter(index -> images.get(index).primary())
                .boxed()
                .toList();
        if (primaries.size() != 1) {
            // The primary image cannot be identified reliably: never guess.
            throw new PhotoRejectedException(PhotoRejectionReason.MULTIPLE_IMAGES);
        }
        return primaries.get(0);
    }

    /** heif-dec's output names: the given name for one image, numbered from 1 in listed order otherwise. */
    static List<String> expectedOutputNames(int topLevelImages) {
        if (topLevelImages == 1) {
            return List.of(OUTPUT);
        }
        return IntStream.rangeClosed(1, topLevelImages)
                .mapToObj(number -> OUTPUT_STEM + "-" + number + ".png")
                .toList();
    }

    /** Runs one libheif tool inside the private directory within what is left of the shared deadline. */
    private static void run(List<String> arguments, Path workingDirectory, ProcessBuilder.Redirect output,
                            long deadline) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
            throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
        }
        Process process;
        try {
            process = new ProcessBuilder(arguments)
                    .directory(workingDirectory.toFile())
                    .redirectOutput(output)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } catch (IOException notInstalled) {
            throw new PhotoProcessingUnavailableException("A libheif tool could not be started", notInstalled);
        }
        try {
            if (!process.waitFor(remaining, TimeUnit.NANOSECONDS)) {
                process.destroyForcibly();
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
        } catch (InterruptedException interrupted) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new PhotoProcessingUnavailableException("HEIF decoding was interrupted", interrupted);
        }
        if (process.exitValue() != 0) {
            throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
        }
    }

    /** A new directory only the application user can read (POSIX); the platform default elsewhere. */
    static Path privateTemporaryDirectory() {
        try {
            try {
                return Files.createTempDirectory("capsuleai-heif-",
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            } catch (UnsupportedOperationException notPosix) {
                return Files.createTempDirectory("capsuleai-heif-");
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("No temporary directory for HEIF decoding", failure);
        }
    }

    static void deleteRecursively(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // A leftover temporary file is removed by the OS temp cleanup; it holds no metadata.
                }
            });
        } catch (IOException ignored) {
            // Nothing to delete.
        }
    }
}