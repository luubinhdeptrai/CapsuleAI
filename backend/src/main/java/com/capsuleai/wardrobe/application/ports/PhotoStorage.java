package com.capsuleai.wardrobe.application.ports;

import java.util.Optional;

/**
 * Wardrobe-owned private object storage for garment photos (ADA-007, S3-IMG-01). Never called
 * inside a database transaction. Every method may throw {@link PhotoStorageUnavailableException}.
 */
public interface PhotoStorage {

    /** Stores a new object; never overwrites an existing key. */
    void store(String objectKey, byte[] content, String contentType);

    /** The stored object, or empty if none exists under the key. */
    Optional<StoredPhoto> open(String objectKey);

    /** Deletes the object; deleting a key that does not exist succeeds (idempotent). */
    void delete(String objectKey);
}