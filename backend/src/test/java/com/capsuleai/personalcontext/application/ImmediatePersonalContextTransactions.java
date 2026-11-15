package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import java.util.function.Supplier;

/** Fake transaction boundary: runs the work directly. */
final class ImmediatePersonalContextTransactions implements PersonalContextTransactions {

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return work.get();
    }
}
