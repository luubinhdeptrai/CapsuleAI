package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.util.Objects;

/** The outcome of one bounded weather acquisition: translated evidence, or an explicit reason it is unavailable. */
public sealed interface WeatherAcquisition {

    record Acquired(WeatherReading reading) implements WeatherAcquisition {

        public Acquired {
            Objects.requireNonNull(reading, "reading");
        }
    }

    record Unavailable(EnvironmentUnavailableReason reason) implements WeatherAcquisition {

        public Unavailable {
            Objects.requireNonNull(reason, "reason");
        }
    }
}