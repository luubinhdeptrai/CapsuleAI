package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.PasswordHash;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link AccountRepository} for application tests. */
final class InMemoryAccountRepository implements AccountRepository {

    private final Map<EmailAddress, Account> accountsByEmail = new LinkedHashMap<>();

    @Override
    public synchronized boolean addIfEmailAvailable(Account account) {
        return accountsByEmail.putIfAbsent(account.email(), account) == null;
    }

    @Override
    public synchronized Optional<Account> findByEmail(EmailAddress email) {
        return Optional.ofNullable(accountsByEmail.get(email));
    }

    @Override
    public synchronized Optional<PasswordHash> lockCredentialAgainstChange(UUID accountId) {
        return findById(accountId).map(Account::passwordHash);
    }

    @Override
    public synchronized boolean lockForCredentialChange(UUID accountId) {
        return findById(accountId).isPresent();
    }

    @Override
    public synchronized void replacePasswordHash(UUID accountId, PasswordHash newPasswordHash) {
        Account account = findById(accountId).orElseThrow(() -> new IllegalStateException("Unknown account"));
        accountsByEmail.put(account.email(), new Account(
                account.id(), account.email(), newPasswordHash, account.displayName(), account.createdAt()));
    }

    synchronized Optional<Account> findById(UUID accountId) {
        return accountsByEmail.values().stream().filter(account -> account.id().equals(accountId)).findFirst();
    }

    synchronized List<Account> all() {
        return List.copyOf(accountsByEmail.values());
    }
}
