package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.util.Objects;

/**
 * US-CTX-001 (S4-CTX-06, OPEN-S4-23) — the environmental basis of the owner's current selection: none, current
 * evidence, or explicitly unavailable. {@code locationRevision} names the selection the basis applies to, so a
 * future consumer can compare bases at use. No consumer exists yet; this type is not published (no api change).
 */
public sealed interface EnvironmentalContext {

    /** No location is selected; no weather is ever requested. */
    record NoSelection() implements EnvironmentalContext {
    }

    /** Current evidence (at most 30 minutes old) for the selection at {@code locationRevision}. */
    record Current(long locationRevision, WeatherReading reading) implements EnvironmentalContext {

        public Current {
            Objects.requireNonNull(reading, "reading");
        }
    }

    /** No usable current weather for the selection at {@code locationRevision}. */
    record Unavailable(long locationRevision, EnvironmentUnavailableReason reason) implements EnvironmentalContext {

        public Unavailable {
            Objects.requireNonNull(reason, "reason");
        }
    }
}