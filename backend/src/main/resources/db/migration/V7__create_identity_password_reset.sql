-- US-AUTH-005: persistent password-reset authority (ADA-008, DATA-AUTH-005).
-- Owner: Identity. Every object is in the identity schema (AR-01); the foreign key
-- stays inside it (AR-04). V6 created the schema and moved the Sprint 1 tables there.
-- One row per issued interaction. Only the SHA-256 hash of its secret is stored.
-- An interaction ends at most one way: consumed by an accepted reset, or replaced
-- by a newer issuance (FR-AUTH-017, FR-AUTH-018). Expiry is enforced by comparing
-- expires_at with the application clock.
CREATE TABLE identity.identity_password_reset (
    id          UUID        PRIMARY KEY,
    account_id  UUID        NOT NULL REFERENCES identity.identity_account (id),
    token_hash  TEXT        NOT NULL,
    issued_at   TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ NULL,
    replaced_at TIMESTAMPTZ NULL,
    CONSTRAINT uq_identity_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_identity_password_reset_token_hash_format CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_identity_password_reset_lifetime
        CHECK (expires_at > issued_at AND expires_at <= issued_at + INTERVAL '30 minutes'),
    CONSTRAINT ck_identity_password_reset_single_outcome
        CHECK (consumed_at IS NULL OR replaced_at IS NULL),
    CONSTRAINT ck_identity_password_reset_consumed_after_issue
        CHECK (consumed_at IS NULL OR consumed_at >= issued_at),
    CONSTRAINT ck_identity_password_reset_replaced_after_issue
        CHECK (replaced_at IS NULL OR replaced_at >= issued_at)
);

-- An account has at most one open (neither consumed nor replaced) interaction.
-- Issuance replaces the open one before inserting, under the account row lock.
CREATE UNIQUE INDEX ux_identity_password_reset_open_per_account
    ON identity.identity_password_reset (account_id)
    WHERE consumed_at IS NULL AND replaced_at IS NULL;

CREATE INDEX ix_identity_password_reset_account ON identity.identity_password_reset (account_id);

-- Ended interactions are deleted 30 days after they end (OPEN-S2-07): the cleanup
-- searches by the moment an interaction ended.
CREATE INDEX ix_identity_password_reset_ended
    ON identity.identity_password_reset ((COALESCE(consumed_at, replaced_at, expires_at)));

-- Abuse protection (OPEN-S2-04, FR-AUTH-019): one counter per scope, hashed subject
-- (normalized email or client address, never stored in clear) and fixed time window.
CREATE TABLE identity.identity_password_reset_attempt (
    scope        TEXT        NOT NULL,
    subject_hash TEXT        NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    attempts     INTEGER     NOT NULL,
    CONSTRAINT pk_identity_password_reset_attempt PRIMARY KEY (scope, subject_hash, window_start),
    CONSTRAINT ck_identity_password_reset_attempt_scope
        CHECK (scope IN ('REQUEST_PER_EMAIL', 'REQUEST_PER_CLIENT', 'VERIFICATION_PER_CLIENT')),
    CONSTRAINT ck_identity_password_reset_attempt_subject_hash CHECK (subject_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_identity_password_reset_attempt_count CHECK (attempts > 0)
);

CREATE INDEX ix_identity_password_reset_attempt_window ON identity.identity_password_reset_attempt (window_start);

-- An accepted reset revokes every active session of the account with its own reason.
ALTER TABLE identity.identity_session
    DROP CONSTRAINT ck_identity_session_revocation_reason;

ALTER TABLE identity.identity_session
    ADD CONSTRAINT ck_identity_session_revocation_reason
        CHECK (revocation_reason IN ('LOGOUT', 'REFRESH_TOKEN_REUSE', 'PASSWORD_RESET'));