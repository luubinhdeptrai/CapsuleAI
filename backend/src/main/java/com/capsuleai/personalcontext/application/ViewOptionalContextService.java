package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.OptionalContextRepository;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** US-PROF-002 AC-01/03 — the owner's current or absent optional context. Read-only. */
public final class ViewOptionalContextService {

    private final OptionalContextRepository optionalContextRepository;

    public ViewOptionalContextService(OptionalContextRepository optionalContextRepository) {
        this.optionalContextRepository = Objects.requireNonNull(optionalContextRepository, "optionalContextRepository");
    }

    public Optional<OptionalContextProfile> view(UUID ownerAccountId) {
        return optionalContextRepository.find(Objects.requireNonNull(ownerAccountId, "ownerAccountId"));
    }
}