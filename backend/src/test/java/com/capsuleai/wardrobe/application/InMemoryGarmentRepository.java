package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link GarmentRepository} for application tests. */
final class InMemoryGarmentRepository implements GarmentRepository {

    private record CreationKey(UUID ownerAccountId, UUID key) {
    }

    private final Map<CreationKey, RecordedGarmentCreation> creations = new LinkedHashMap<>();

    @Override
    public synchronized boolean addIfCreationKeyUnused(Garment garment, UUID creationKey, String creationFingerprint) {
        return creations.putIfAbsent(new CreationKey(garment.ownerAccountId(), creationKey),
                new RecordedGarmentCreation(garment, creationFingerprint)) == null;
    }

    @Override
    public synchronized Optional<RecordedGarmentCreation> findCreation(UUID ownerAccountId, UUID creationKey) {
        return Optional.ofNullable(creations.get(new CreationKey(ownerAccountId, creationKey)));
    }

    synchronized List<Garment> all() {
        return creations.values().stream().map(RecordedGarmentCreation::garment).toList();
    }
}
