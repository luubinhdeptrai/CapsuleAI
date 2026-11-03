package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fake issuer that records what it was asked to sign. */
final class FakeAccessTokenIssuer implements AccessTokenIssuer {

    record Issued(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt) {
    }

    private final List<Issued> issued = new ArrayList<>();

    @Override
    public synchronized String issue(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt) {
        issued.add(new Issued(accountId, sessionId, issuedAt, expiresAt));
        return "access-token-" + issued.size();
    }

    synchronized List<Issued> issued() {
        return List.copyOf(issued);
    }
}
