package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.OptionalContext;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Personal Context-owned persistence of optional context (US-PROF-002, S4-PROF-02, V13). */
public interface OptionalContextRepository {

    /** The owner's record, or empty if they have never declared optional context. */
    Optional<OptionalContextProfile> find(UUID ownerAccountId);

    /**
     * Stores the owner's first declaration unless a record exists (guarded creation). Of concurrent first
     * declarations exactly one returns {@code true}. Call inside {@link PersonalContextTransactions}.
     */
    boolean createIfAbsent(OptionalContextProfile profile);

    /**
     * Replaces both values only while the revision is still {@code expectedRevision}; the revision increases
     * by one. Call inside {@link PersonalContextTransactions}.
     *
     * @return {@code false} if the revision changed or there is no record (nothing changed)
     */
    boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, OptionalContext values, Instant updatedAt);
}