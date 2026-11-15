package com.capsuleai.wardrobe.adapter.out.storage;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import java.util.Objects;
import java.util.Optional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * {@link PhotoStorage} on a private S3-compatible bucket (S3-IMG-01): Amazon S3 in deployment,
 * an S3-compatible fixture locally and in tests (OPEN-S3-13). Objects carry only a Content-Type;
 * no profile value, owner id or file name is written as metadata. The client is configured with
 * bounded call timeouts, so no call can hang a request thread (ADA-013).
 */
public final class S3PhotoStorage implements PhotoStorage {

    private static final int PRECONDITION_FAILED = 412;
    private static final int NOT_FOUND = 404;

    private final S3Client s3;
    private final String bucket;
    private final boolean conditionalWrites;

    public S3PhotoStorage(S3Client s3, String bucket, boolean conditionalWrites) {
        this.s3 = Objects.requireNonNull(s3, "s3");
        this.bucket = bucket == null ? "" : bucket.strip();
        this.conditionalWrites = conditionalWrites;
    }

    @Override
    public void store(String objectKey, byte[] content, String contentType) {
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(requireBucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) content.length);
        if (conditionalWrites) {
            request.ifNoneMatch("*"); // never overwrite an existing object (S3-IMG-04)
        }
        try {
            s3.putObject(request.build(), RequestBody.fromBytes(content));
        } catch (S3Exception refused) {
            if (refused.statusCode() == PRECONDITION_FAILED) {
                throw new IllegalStateException("A new photo key already exists in storage", refused);
            }
            throw unavailable("store", refused);
        } catch (SdkException failed) {
            throw unavailable("store", failed);
        }
    }

    @Override
    public Optional<StoredPhoto> open(String objectKey) {
        try {
            ResponseInputStream<GetObjectResponse> stream = s3.getObject(GetObjectRequest.builder()
                    .bucket(requireBucket())
                    .key(objectKey)
                    .build());
            GetObjectResponse response = stream.response();
            long size = response.contentLength() == null ? -1 : response.contentLength();
            if (size < 0) {
                stream.abort();
                throw new PhotoStorageUnavailableException("Photo storage returned no content length", null);
            }
            String contentType = response.contentType() == null ? "application/octet-stream" : response.contentType();
            return Optional.of(new StoredPhoto(stream, size, contentType));
        } catch (NoSuchKeyException missing) {
            return Optional.empty();
        } catch (S3Exception refused) {
            if (refused.statusCode() == NOT_FOUND) {
                return Optional.empty();
            }
            throw unavailable("open", refused);
        } catch (SdkException failed) {
            throw unavailable("open", failed);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(requireBucket()).key(objectKey).build());
        } catch (SdkException failed) {
            throw unavailable("delete", failed);
        }
    }

    private String requireBucket() {
        if (bucket.isEmpty()) {
            throw new PhotoStorageUnavailableException(
                    "capsuleai.wardrobe.photo-storage.bucket is not configured", null);
        }
        return bucket;
    }

    /** No object key, bucket name or credential appears in the message. */
    private static PhotoStorageUnavailableException unavailable(String operation, SdkException cause) {
        return new PhotoStorageUnavailableException("Photo storage " + operation + " failed", cause);
    }
}