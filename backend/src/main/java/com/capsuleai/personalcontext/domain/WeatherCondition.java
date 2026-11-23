package com.capsuleai.personalcontext.domain;

import java.util.Optional;

/**
 * US-CTX-001 (OPEN-S4-20) — a coarse current condition translated from the WMO weather interpretation code the
 * provider reports. Unknown codes are not guessed: the reading is invalid instead.
 */
public enum WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    FREEZING_DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    SNOW,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    THUNDERSTORM_WITH_HAIL;

    /** The condition of a WMO code (0, 1, 2, 3, 45, 48, 51–57, 61–67, 71–77, 80–86, 95, 96, 99). */
    public static Optional<WeatherCondition> fromWmoCode(int code) {
        return Optional.ofNullable(switch (code) {
            case 0 -> CLEAR;
            case 1 -> MAINLY_CLEAR;
            case 2 -> PARTLY_CLOUDY;
            case 3 -> OVERCAST;
            case 45, 48 -> FOG;
            case 51, 53, 55 -> DRIZZLE;
            case 56, 57 -> FREEZING_DRIZZLE;
            case 61, 63, 65 -> RAIN;
            case 66, 67 -> FREEZING_RAIN;
            case 71, 73, 75, 77 -> SNOW;
            case 80, 81, 82 -> RAIN_SHOWERS;
            case 85, 86 -> SNOW_SHOWERS;
            case 95 -> THUNDERSTORM;
            case 96, 99 -> THUNDERSTORM_WITH_HAIL;
            default -> null;
        });
    }
}