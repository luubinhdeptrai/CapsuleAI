package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.IdentityTransactions;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link IdentityTransactions} backed by Spring's {@link TransactionTemplate}.
 * Uses PostgreSQL's default READ COMMITTED isolation. A RuntimeException thrown
 * by the work rolls everything back.
 */
public final class SpringIdentityTransactions implements IdentityTransactions {

    private final TransactionTemplate transactionTemplate;

    public SpringIdentityTransactions(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}