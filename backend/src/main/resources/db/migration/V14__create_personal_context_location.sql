-- US-CTX-001 (S4-CTX-01/02/04, approved 2026-11-16; OPEN-S4-18/19/21): the user's one current location
-- selection and the latest weather evidence acquired for it. No history is kept.
--
-- A selection is a point rounded by the server to 2 decimals (about 1.1 km), an optional label (required for
-- MANUAL) and its source. Clearing sets every selection column to NULL and keeps the row, so the revision line
-- continues. Weather evidence is bound to the location revision it was acquired for and is deleted in the same
-- transaction as any change or clear of the selection; a provider refresh never changes the location revision.
--
-- Owner: Personal Context. Every object below is in the personal_context schema (AR-01); owner_account_id is
-- the authenticated Identity account id with no foreign key into Identity (ADA-016, AR-04).
CREATE TABLE personal_context.personal_context_location (
    owner_account_id UUID          PRIMARY KEY,
    latitude         NUMERIC(4, 2) NULL,
    longitude        NUMERIC(5, 2) NULL,
    label            TEXT          NULL,
    source           TEXT          NULL,
    revision         BIGINT        NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL,

    -- A selection is complete or absent; IS NULL comparisons are never NULL themselves.
    CONSTRAINT ck_personal_context_location_complete CHECK ((latitude IS NULL) = (longitude IS NULL)
        AND (latitude IS NULL) = (source IS NULL) AND (latitude IS NOT NULL OR label IS NULL)),
    CONSTRAINT ck_personal_context_location_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_personal_context_location_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_personal_context_location_source CHECK (source IS NULL OR source IN ('DEVICE', 'MANUAL')),
    CONSTRAINT ck_personal_context_location_manual_label CHECK (source IS DISTINCT FROM 'MANUAL' OR label IS NOT NULL),
    CONSTRAINT ck_personal_context_location_label CHECK (label IS NULL OR char_length(label) BETWEEN 1 AND 80),
    CONSTRAINT ck_personal_context_location_revision CHECK (revision >= 1),
    CONSTRAINT ck_personal_context_location_updated_after_created CHECK (updated_at >= created_at)
);

-- The latest translated weather evidence for the owner's current selection (no archive, no raw payload).
CREATE TABLE personal_context.personal_context_weather_evidence (
    owner_account_id               UUID          PRIMARY KEY
        REFERENCES personal_context.personal_context_location (owner_account_id) ON DELETE CASCADE,
    location_revision              BIGINT        NOT NULL,
    observed_at                    TIMESTAMPTZ   NOT NULL,
    retrieved_at                   TIMESTAMPTZ   NOT NULL,
    temperature_celsius            NUMERIC(6, 2) NOT NULL,
    apparent_temperature_celsius   NUMERIC(6, 2) NULL,
    relative_humidity_percent      SMALLINT      NULL,
    precipitation_millimetres      NUMERIC(7, 2) NULL,
    wind_speed_kilometres_per_hour NUMERIC(6, 2) NULL,
    weather_code                   SMALLINT      NOT NULL,
    provider                       TEXT          NOT NULL,
    basis                          TEXT          NOT NULL,

    CONSTRAINT ck_personal_context_weather_evidence_revision CHECK (location_revision >= 1),
    CONSTRAINT ck_personal_context_weather_evidence_humidity
        CHECK (relative_humidity_percent IS NULL OR relative_humidity_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_personal_context_weather_evidence_precipitation
        CHECK (precipitation_millimetres IS NULL OR precipitation_millimetres >= 0),
    CONSTRAINT ck_personal_context_weather_evidence_wind
        CHECK (wind_speed_kilometres_per_hour IS NULL OR wind_speed_kilometres_per_hour >= 0),
    CONSTRAINT ck_personal_context_weather_evidence_code CHECK (weather_code BETWEEN 0 AND 99),
    CONSTRAINT ck_personal_context_weather_evidence_provider CHECK (provider IN ('OPEN_METEO')),
    CONSTRAINT ck_personal_context_weather_evidence_basis CHECK (basis IN ('MODELLED', 'OBSERVED'))
);