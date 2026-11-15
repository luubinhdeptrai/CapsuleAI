package com.capsuleai.wardrobe.support;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TEST-ONLY {@link PhotoStorage}: an in-memory store that never overwrites (like a conditional write)
 * and can be told to fail stores or deletions, run a competing action just before a store, or lose
 * an object, so lifecycle and recovery paths can be driven deterministically.
 */
public final class FaultInjectingPhotoStorage implements PhotoStorage {

    private record StoredObject(byte[] content, String contentType) {
    }

    private final Map<String, StoredObject> objects = new ConcurrentHashMap<>();
    private final AtomicBoolean failStores = new AtomicBoolean();
    private final AtomicBoolean failDeletes = new AtomicBoolean();
    private final AtomicInteger deletions = new AtomicInteger();
    private final AtomicReference<Runnable> beforeNextStore = new AtomicReference<>();

    @Override
    public void store(String objectKey, byte[] content, String contentType) {
        Runnable competing = beforeNextStore.getAndSet(null);
        if (competing != null) {
            competing.run();
        }
        if (failStores.get()) {
            throw new PhotoStorageUnavailableException("simulated store failure", null);
        }
        if (objects.putIfAbsent(objectKey, new StoredObject(content.clone(), contentType)) != null) {
            throw new IllegalStateException("A new photo key already exists in storage");
        }
    }

    @Override
    public Optional<StoredPhoto> open(String objectKey) {
        return Optional.ofNullable(objects.get(objectKey)).map(stored -> new StoredPhoto(
                new ByteArrayInputStream(stored.content()), stored.content().length, stored.contentType()));
    }

    @Override
    public void delete(String objectKey) {
        if (failDeletes.get()) {
            throw new PhotoStorageUnavailableException("simulated delete failure", null);
        }
        deletions.incrementAndGet();
        objects.remove(objectKey);
    }

    public void failStores(boolean fail) {
        failStores.set(fail);
    }

    public void failDeletes(boolean fail) {
        failDeletes.set(fail);
    }

    public void beforeNextStore(Runnable competingAction) {
        beforeNextStore.set(competingAction);
    }

    /** Simulates an object lost outside the application. */
    public void lose(String objectKey) {
        objects.remove(objectKey);
    }

    /** Places an object directly, as a store that finished late would. */
    public void put(String objectKey, byte[] content) {
        objects.put(objectKey, new StoredObject(content.clone(), "image/jpeg"));
    }

    public boolean contains(String objectKey) {
        return objects.containsKey(objectKey);
    }

    public byte[] content(String objectKey) {
        return objects.get(objectKey).content().clone();
    }

    public int objectCount() {
        return objects.size();
    }

    public int deletionCount() {
        return deletions.get();
    }

    public void reset() {
        objects.clear();
        failStores.set(false);
        failDeletes.set(false);
        deletions.set(0);
        beforeNextStore.set(null);
    }
}
