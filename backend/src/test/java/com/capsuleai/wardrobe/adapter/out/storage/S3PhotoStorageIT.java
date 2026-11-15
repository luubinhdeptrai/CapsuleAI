package com.capsuleai.wardrobe.adapter.out.storage;

import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-003 (S3-IMG-01, OPEN-S3-13): the real S3 adapter against an S3-compatible container. An
 * emulator pass is protocol evidence only; the real-provider check in V-S3 is still required.
 */
@Testcontainers
class S3PhotoStorageIT {

    private static final int S3_PORT = 9090;
    private static final String BUCKET = "capsuleai-photos-it";

    @Container
    private static final GenericContainer<?> S3MOCK = new GenericContainer<>("adobe/s3mock:5.1.0")
            .withExposedPorts(S3_PORT)
            .withEnv("COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS", BUCKET)
            .withEnv("initialBuckets", BUCKET)
            .waitingFor(Wait.forListeningPort());

    private static S3Client client;

    @BeforeAll
    static void connect() {
        client = S3Client.builder()
                .endpointOverride(URI.create("http://" + S3MOCK.getHost() + ":" + S3MOCK.getMappedPort(S3_PORT)))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test-only", "test-only")))
                .overrideConfiguration(configuration -> configuration.apiCallTimeout(Duration.ofSeconds(30)))
                .build();
        try {
            client.createBucket(request -> request.bucket(BUCKET));
        } catch (S3Exception alreadyCreated) {
            // created by the container's initial-bucket setting
        }
    }

    @AfterAll
    static void disconnect() {
        client.close();
    }

    @Test
    void aStoredPhotoIsReadBackWithItsLengthAndType() throws Exception {
        String key = newKey();
        byte[] content = {(byte) 0xFF, (byte) 0xD8, 1, 2, 3};
        storage().store(key, content, "image/jpeg");

        try (StoredPhoto photo = storage().open(key).orElseThrow()) {
            assertThat(photo.content().readAllBytes()).isEqualTo(content);
            assertThat(photo.sizeBytes()).isEqualTo(content.length);
            assertThat(photo.contentType()).isEqualTo("image/jpeg");
        }
    }

    @Test
    void aMissingObjectIsEmptyNotAnError() {
        assertThat(storage().open(newKey())).isEmpty();
    }

    @Test
    void deletingRemovesTheObjectAndIsIdempotent() {
        String key = newKey();
        storage().store(key, new byte[] {1}, "image/jpeg");

        storage().delete(key);
        storage().delete(key);

        assertThat(storage().open(key)).isEmpty();
    }

    /** V-S3 step 2: with conditional writes, an existing key is never overwritten (S3-IMG-04). */
    @Test
    void aConditionalWriteNeverOverwritesAnExistingKey() throws Exception {
        String key = newKey();
        storage().store(key, new byte[] {1}, "image/jpeg");

        assertThatThrownBy(() -> storage().store(key, new byte[] {2}, "image/jpeg"))
                .isInstanceOf(IllegalStateException.class);
        try (StoredPhoto photo = storage().open(key).orElseThrow()) {
            assertThat(photo.content().readAllBytes()).containsExactly(1);
        }
    }

    private static S3PhotoStorage storage() {
        return new S3PhotoStorage(client, BUCKET, true);
    }

    private static String newKey() {
        return "photos/" + UUID.randomUUID() + ".jpg";
    }
}
