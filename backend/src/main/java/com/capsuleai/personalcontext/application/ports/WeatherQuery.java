package com.capsuleai.personalcontext.application.ports;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * US-CTX-001 AC-07 (OPEN-S4-20) — everything the weather provider receives: the rounded selected point. No
 * account, label, wardrobe, profile or history information is part of a query.
 */
public record WeatherQuery(BigDecimal latitude, BigDecimal longitude) {

    public WeatherQuery {
        Objects.requireNonNull(latitude, "latitude");
        Objects.requireNonNull(longitude, "longitude");
    }
}