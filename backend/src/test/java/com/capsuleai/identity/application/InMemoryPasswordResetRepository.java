package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link PasswordResetRepository} for application tests (no real locking). */
final class InMemoryPasswordResetRepository implements PasswordResetRepository {

    private final Map<PasswordResetTokenHash, PasswordResetToken> tokensByHash = new LinkedHashMap<>();

    @Override
    public synchronized int replaceOpenTokens(UUID accountId, Instant replacedAt) {
        int replaced = 0;
        for (Map.Entry<PasswordResetTokenHash, PasswordResetToken> entry : tokensByHash.entrySet()) {
            PasswordResetToken token = entry.getValue();
            if (token.accountId().equals(accountId) && token.consumedAt() == null && token.replacedAt() == null) {
                entry.setValue(new PasswordResetToken(token.id(), token.accountId(), token.issuedAt(),
                        token.expiresAt(), null, replacedAt));
                replaced++;
            }
        }
        return replaced;
    }

    @Override
    public synchronized void add(PasswordResetToken token, PasswordResetTokenHash hash) {
        tokensByHash.put(hash, token);
    }

    @Override
    public synchronized Optional<PasswordResetToken> findByTokenHash(PasswordResetTokenHash hash) {
        return Optional.ofNullable(tokensByHash.get(hash));
    }

    @Override
    public synchronized Optional<PasswordResetToken> lockByTokenHash(PasswordResetTokenHash hash) {
        return findByTokenHash(hash);
    }

    @Override
    public synchronized int deleteEndedBefore(Instant cutoff, int batchSize) {
        var ended = tokensByHash.entrySet().stream()
                .filter(entry -> !endOf(entry.getValue()).isAfter(cutoff))
                .limit(batchSize)
                .map(java.util.Map.Entry::getKey)
                .toList();
        ended.forEach(tokensByHash::remove);
        return ended.size();
    }

    private static Instant endOf(PasswordResetToken token) {
        if (token.consumedAt() != null) {
            return token.consumedAt();
        }
        return token.replacedAt() != null ? token.replacedAt() : token.expiresAt();
    }

    @Override
    public synchronized void consume(UUID tokenId, Instant consumedAt) {
        for (Map.Entry<PasswordResetTokenHash, PasswordResetToken> entry : tokensByHash.entrySet()) {
            PasswordResetToken token = entry.getValue();
            if (token.id().equals(tokenId)) {
                if (token.consumedAt() != null || token.replacedAt() != null) {
                    throw new IllegalStateException("Password-reset interaction is not open");
                }
                entry.setValue(new PasswordResetToken(token.id(), token.accountId(), token.issuedAt(),
                        token.expiresAt(), consumedAt, null));
                return;
            }
        }
        throw new IllegalStateException("Unknown password-reset interaction");
    }

    synchronized List<PasswordResetToken> all() {
        return List.copyOf(tokensByHash.values());
    }
}
