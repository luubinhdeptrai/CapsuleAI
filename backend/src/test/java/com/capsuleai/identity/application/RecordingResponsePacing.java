package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.RecoveryResponsePacing;
import java.util.concurrent.atomic.AtomicInteger;

/** Test fake: counts how many requests started and finished pacing; never waits. */
public final class RecordingResponsePacing implements RecoveryResponsePacing {

    private final AtomicInteger started = new AtomicInteger();
    private final AtomicInteger finished = new AtomicInteger();

    @Override
    public Pace start() {
        started.incrementAndGet();
        return finished::incrementAndGet;
    }

    public int started() {
        return started.get();
    }

    public int finished() {
        return finished.get();
    }
}
