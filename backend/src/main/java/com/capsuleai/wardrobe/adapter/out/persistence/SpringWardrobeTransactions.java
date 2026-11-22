package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link WardrobeTransactions} backed by Spring's {@link TransactionTemplate}.
 * Uses PostgreSQL's default READ COMMITTED isolation. A RuntimeException thrown
 * by the work rolls everything back.
 */
public final class SpringWardrobeTransactions implements WardrobeTransactions {

    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate snapshotTemplate;

    public SpringWardrobeTransactions(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.snapshotTemplate = new TransactionTemplate(transactionManager);
        this.snapshotTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        this.snapshotTemplate.setReadOnly(true);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }

    /** US-WAR-003: PostgreSQL REPEATABLE READ gives the whole read-only transaction one snapshot. */
    @Override
    public <T> T inReadOnlySnapshot(Supplier<T> work) {
        return snapshotTemplate.execute(status -> work.get());
    }
}