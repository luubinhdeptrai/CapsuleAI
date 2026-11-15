package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** {@link PersonalContextTransactions} on Spring's {@link TransactionTemplate} (PostgreSQL READ COMMITTED). */
public final class SpringPersonalContextTransactions implements PersonalContextTransactions {

    private final TransactionTemplate transactionTemplate;

    public SpringPersonalContextTransactions(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}