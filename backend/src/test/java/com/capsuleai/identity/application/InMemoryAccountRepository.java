package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory fake of {@link AccountRepository} for application tests. */
final class InMemoryAccountRepository implements AccountRepository {

    private final Map<EmailAddress, Account> accountsByEmail = new LinkedHashMap<>();

    @Override
    public synchronized boolean addIfEmailAvailable(Account account) {
        return accountsByEmail.putIfAbsent(account.email(), account) == null;
    }

    synchronized List<Account> all() {
        return List.copyOf(accountsByEmail.values());
    }
}