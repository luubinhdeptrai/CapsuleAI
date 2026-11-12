package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link GarmentRepository} for application tests. */
final class InMemoryGarmentRepository implements GarmentRepository {

    private record CreationKey(UUID ownerAccountId, UUID key) {
    }

    /** The repository contract's order: creation time, then id, both descending. */
    private static final Comparator<Garment> NEWEST_FIRST =
            Comparator.comparing(Garment::createdAt).thenComparing(Garment::id).reversed();

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

    @Override
    public synchronized List<Garment> findCurrentByOwner(
            UUID ownerAccountId, Optional<WardrobePosition> after, int maximumCount) {
        return all().stream()
                .filter(garment -> garment.isOwnedBy(ownerAccountId) && garment.isCurrent())
                .filter(garment -> after.map(position -> comesAfter(garment, position)).orElse(true))
                .sorted(NEWEST_FIRST)
                .limit(maximumCount)
                .toList();
    }

    /** Strictly after the position in {@link #NEWEST_FIRST} order, like the SQL keyset. */
    private static boolean comesAfter(Garment garment, WardrobePosition position) {
        int byTime = garment.createdAt().compareTo(position.createdAt());
        return byTime < 0 || (byTime == 0 && garment.id().compareTo(position.garmentId()) < 0);
    }

    @Override
    public synchronized Optional<Garment> findOwned(UUID ownerAccountId, UUID garmentId) {
        return all().stream()
                .filter(garment -> garment.isOwnedBy(ownerAccountId) && garment.id().equals(garmentId))
                .findFirst();
    }

    @Override
    public synchronized boolean reviseIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision,
                                                  GarmentProfile revisedProfile) {
        for (Map.Entry<CreationKey, RecordedGarmentCreation> entry : creations.entrySet()) {
            Garment garment = entry.getValue().garment();
            if (garment.id().equals(garmentId) && garment.isOwnedBy(ownerAccountId) && garment.isCurrent()
                    && garment.revision() == expectedRevision) {
                entry.setValue(new RecordedGarmentCreation(
                        garment.withRevisedProfile(revisedProfile), entry.getValue().creationFingerprint()));
                return true;
            }
        }
        return false;
    }

    /** TEST HELPER: the garment leaves the current wardrobe at {@code removedAt}. */
    synchronized void remove(UUID garmentId, Instant removedAt) {
        creations.replaceAll((key, recorded) -> {
            Garment garment = recorded.garment();
            if (!garment.id().equals(garmentId)) {
                return recorded;
            }
            return new RecordedGarmentCreation(new Garment(garment.id(), garment.ownerAccountId(), garment.profile(),
                    garment.provenance(), garment.createdAt(), removedAt, garment.revision()),
                    recorded.creationFingerprint());
        });
    }

    synchronized void add(Garment garment) {
        creations.put(new CreationKey(garment.ownerAccountId(), UUID.randomUUID()),
                new RecordedGarmentCreation(garment, "0".repeat(64)));
    }

    synchronized List<Garment> all() {
        return creations.values().stream().map(RecordedGarmentCreation::garment).toList();
    }
}
