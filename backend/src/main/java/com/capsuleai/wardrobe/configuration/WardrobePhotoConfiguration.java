package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.out.imaging.HeifDecoder;
import com.capsuleai.wardrobe.adapter.out.imaging.ImageIoPhotoInspector;
import com.capsuleai.wardrobe.adapter.out.storage.S3PhotoStorage;
import com.capsuleai.wardrobe.application.CancelPhotoUploadService;
import com.capsuleai.wardrobe.application.GarmentPhotoContentService;
import com.capsuleai.wardrobe.application.InspectGarmentService;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.application.PhotoUploadPolicy;
import com.capsuleai.wardrobe.application.StagePhotoUploadService;
import com.capsuleai.wardrobe.application.ViewPhotoUploadService;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Wires US-GAR-003's private photo capability: the S3 client and storage adapter, the bounded
 * decoder, the policy and the photo use cases. Creating the S3 client opens no connection, so the
 * application starts even when storage is not configured; photo operations then answer 503.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WardrobePhotoProperties.class)
public class WardrobePhotoConfiguration {

    @Bean
    PhotoUploadPolicy photoUploadPolicy(WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Photo photo = properties.photo();
        WardrobePhotoProperties.Upload upload = properties.photoUpload();
        Duration slowestStore = properties.photoStorage().callTimeout().plus(photo.decodeTimeout().multipliedBy(2));
        if (upload.inFlightFence().compareTo(slowestStore) <= 0) {
            throw new IllegalStateException("capsuleai.wardrobe.photo-upload.in-flight-fence must be longer than "
                    + "the storage call timeout plus twice the decode timeout (" + slowestStore + ")");
        }
        return new PhotoUploadPolicy(photo.maximumBytes(), photo.minimumShortestSide(), upload.pendingTtl(),
                upload.inFlightFence(), upload.cleanup().batchSize(), upload.cleanup().retryDelay());
    }

    @Bean
    S3Client photoStorageClient(WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Storage storage = properties.photoStorage();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(storage.region()))
                .forcePathStyle(storage.pathStyle())
                .overrideConfiguration(configuration -> configuration
                        .apiCallTimeout(storage.callTimeout())
                        .apiCallAttemptTimeout(storage.callTimeout()));
        if (hasText(storage.endpoint())) {
            builder.endpointOverride(URI.create(storage.endpoint().strip()));
        }
        if (hasText(storage.accessKeyId())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(storage.accessKeyId(), storage.secretAccessKey())));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
        }
        return builder.build();
    }

    @Bean
    PhotoStorage photoStorage(S3Client photoStorageClient, WardrobePhotoProperties properties) {
        return new S3PhotoStorage(photoStorageClient, properties.photoStorage().bucket(),
                properties.photoStorage().conditionalWrites());
    }

    @Bean
    HeifDecoder heifDecoder(WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Photo photo = properties.photo();
        return new HeifDecoder(photo.heifDecoderCommand(), photo.heifInfoCommand(), photo.decodeTimeout(),
                photo.maximumDeclaredPixels(), photo.heifPrimaryImageSelection());
    }

    @Bean
    PhotoInspector photoInspector(HeifDecoder heifDecoder, WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Photo photo = properties.photo();
        return new ImageIoPhotoInspector(heifDecoder, photo.derivativeLongEdge(), photo.maximumDeclaredPixels(),
                photo.decodeTimeout(), photo.maximumConcurrentDecodes());
    }

    @Bean
    StagePhotoUploadService stagePhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage,
                                                    PhotoInspector inspector, WardrobeTransactions transactions,
                                                    Clock clock, PhotoUploadPolicy policy) {
        return new StagePhotoUploadService(uploads, storage, inspector, transactions, clock, policy);
    }

    @Bean
    ViewPhotoUploadService viewPhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage, Clock clock) {
        return new ViewPhotoUploadService(uploads, storage, clock);
    }

    @Bean
    CancelPhotoUploadService cancelPhotoUploadService(PhotoUploadRepository uploads,
                                                      WardrobeTransactions transactions, Clock clock,
                                                      PhotoUploadPolicy policy) {
        return new CancelPhotoUploadService(uploads, transactions, clock, policy);
    }

    @Bean
    GarmentPhotoContentService garmentPhotoContentService(InspectGarmentService garments,
                                                          PhotoUploadRepository uploads, PhotoStorage storage) {
        return new GarmentPhotoContentService(garments, uploads, storage);
    }

    @Bean
    PhotoUploadCleanupService photoUploadCleanupService(PhotoUploadRepository uploads, PhotoStorage storage,
                                                        WardrobeTransactions transactions, Clock clock,
                                                        PhotoUploadPolicy policy) {
        return new PhotoUploadCleanupService(uploads, storage, transactions, clock, policy);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}