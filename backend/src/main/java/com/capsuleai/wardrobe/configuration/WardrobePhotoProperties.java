package com.capsuleai.wardrobe.configuration;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * US-GAR-003 configuration: {@code capsuleai.wardrobe.photo.*} (photo rules and decoding bounds),
 * {@code capsuleai.wardrobe.photo-upload.*} (pending lifecycle) and
 * {@code capsuleai.wardrobe.photo-storage.*} (private bucket). The values in application.properties
 * implement choices the Product Owner approved on 2026-11-11 (OPEN-S3-07/08/09/10/11/12/13); the
 * toolchain, provider and resource limits still need the verification recorded in the open questions.
 */
@ConfigurationProperties("capsuleai.wardrobe")
public record WardrobePhotoProperties(Photo photo, Upload photoUpload, Storage photoStorage) {

    public WardrobePhotoProperties {
        Objects.requireNonNull(photo, "capsuleai.wardrobe.photo.* must be configured");
        Objects.requireNonNull(photoUpload, "capsuleai.wardrobe.photo-upload.* must be configured");
        Objects.requireNonNull(photoStorage, "capsuleai.wardrobe.photo-storage.* must be configured");
    }

    public record Photo(
            long maximumBytes,
            int minimumShortestSide,
            int derivativeLongEdge,
            long maximumDeclaredPixels,
            Duration decodeTimeout,
            int maximumConcurrentDecodes,
            String heifDecoderCommand,
            String heifInfoCommand,
            boolean heifPrimaryImageSelection) {

        public Photo {
            Objects.requireNonNull(decodeTimeout, "capsuleai.wardrobe.photo.decode-timeout");
            Objects.requireNonNull(heifDecoderCommand, "capsuleai.wardrobe.photo.heif-decoder-command");
            Objects.requireNonNull(heifInfoCommand, "capsuleai.wardrobe.photo.heif-info-command");
        }
    }

    public record Upload(Duration pendingTtl, Duration inFlightFence, Cleanup cleanup) {

        public Upload {
            Objects.requireNonNull(pendingTtl, "capsuleai.wardrobe.photo-upload.pending-ttl");
            Objects.requireNonNull(inFlightFence, "capsuleai.wardrobe.photo-upload.in-flight-fence");
            Objects.requireNonNull(cleanup, "capsuleai.wardrobe.photo-upload.cleanup.*");
        }
    }

    public record Cleanup(boolean enabled, Duration initialDelay, Duration interval, int batchSize,
                          Duration retryDelay) {

        public Cleanup {
            Objects.requireNonNull(retryDelay, "capsuleai.wardrobe.photo-upload.cleanup.retry-delay");
        }
    }

    public record Storage(
            String bucket,
            String region,
            String endpoint,
            String accessKeyId,
            String secretAccessKey,
            Duration callTimeout,
            boolean conditionalWrites,
            boolean pathStyle) {

        public Storage {
            Objects.requireNonNull(region, "capsuleai.wardrobe.photo-storage.region");
            Objects.requireNonNull(callTimeout, "capsuleai.wardrobe.photo-storage.call-timeout");
        }

        /** Never print credentials, not even when debugging configuration. */
        @Override
        public String toString() {
            return "Storage[bucket=" + bucket + ", region=" + region + ", endpoint=" + endpoint
                    + ", credentials=" + (accessKeyId == null || accessKeyId.isBlank() ? "default chain" : "static")
                    + ", callTimeout=" + callTimeout + ", conditionalWrites=" + conditionalWrites
                    + ", pathStyle=" + pathStyle + "]";
        }
    }
}