package com.capsuleai.identity.application.ports;

/**
 * Evens out the response time of successful Forgot Password requests (OPEN-S2-05).
 *
 * <p>A registered email does more work than an unregistered one (it issues and stores an
 * interaction). Holding every successful request until a configured minimum duration has
 * passed hides that difference as long as the work fits within the minimum. It is a
 * mitigation, not a constant-time guarantee; the residual risk is documented.
 */
public interface RecoveryResponsePacing {

    /** Starts timing one request. */
    Pace start();

    /** One timed request. */
    @FunctionalInterface
    interface Pace {

        /** Returns no sooner than the configured minimum after {@link #start()}. */
        void finishNoSoonerThanMinimum();
    }
}