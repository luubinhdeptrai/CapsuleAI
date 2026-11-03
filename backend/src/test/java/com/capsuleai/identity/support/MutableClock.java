package com.capsuleai.identity.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A test clock that can be set or advanced, so expiry boundaries need no real waiting. */
public final class MutableClock extends Clock {

    public static final Instant DEFAULT_START = Instant.parse("2026-01-05T08:00:00Z");

    private final ZoneId zone;
    private Instant current;

    public MutableClock(Instant start) {
        this(start, ZoneOffset.UTC);
    }

    private MutableClock(Instant start, ZoneId zone) {
        this.current = start;
        this.zone = zone;
    }

    public synchronized void setInstant(Instant instant) {
        this.current = instant;
    }

    public synchronized void advance(Duration duration) {
        this.current = current.plus(duration);
    }

    @Override
    public synchronized Instant instant() {
        return current;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public synchronized Clock withZone(ZoneId newZone) {
        return new MutableClock(current, newZone);
    }
}