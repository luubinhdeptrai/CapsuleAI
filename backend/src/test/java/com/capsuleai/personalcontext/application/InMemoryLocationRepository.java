package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link LocationRepository} with the same guard, deletion and publication rules. */
final class InMemoryLocationRepository implements LocationRepository {

    private final Map<UUID, LocationProfile> profiles = new HashMap<>();
    private final Map<UUID, StoredWeatherEvidence> evidence = new HashMap<>();
    private int selectionWrites;

    @Override
    public synchronized Optional<LocationProfile> find(UUID ownerAccountId) {
        return Optional.ofNullable(profiles.get(ownerAccountId));
    }

    @Override
    public synchronized boolean createIfAbsent(LocationProfile profile) {
        if (profiles.putIfAbsent(profile.ownerAccountId(), profile) != null) {
            return false;
        }
        selectionWrites++;
        return true;
    }

    @Override
    public synchronized boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision,
                                                  Optional<LocationSelection> selection, Instant updatedAt) {
        LocationProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        profiles.put(ownerAccountId, current.revisedTo(selection, updatedAt));
        evidence.remove(ownerAccountId);
        selectionWrites++;
        return true;
    }

    @Override
    public synchronized Optional<StoredWeatherEvidence> findEvidence(UUID ownerAccountId) {
        return Optional.ofNullable(evidence.get(ownerAccountId));
    }

    @Override
    public synchronized WeatherPublication publishEvidenceIfCurrent(UUID ownerAccountId, long locationRevision,
                                                                    WeatherReading reading) {
        LocationProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != locationRevision || current.selection().isEmpty()) {
            return WeatherPublication.SELECTION_CHANGED;
        }
        StoredWeatherEvidence stored = evidence.get(ownerAccountId);
        if (stored != null && stored.locationRevision() == locationRevision
                && !stored.reading().observedAt().isBefore(reading.observedAt())) {
            return WeatherPublication.KEPT_NEWER;
        }
        evidence.put(ownerAccountId, new StoredWeatherEvidence(locationRevision, reading));
        return WeatherPublication.PUBLISHED;
    }

    synchronized int selectionWrites() {
        return selectionWrites;
    }
}
