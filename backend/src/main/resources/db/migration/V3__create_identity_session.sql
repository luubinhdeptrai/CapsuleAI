-- US-AUTH-002: persistent login-session authority (ADA-008) and refresh credentials.
-- One row per successful login. expires_at is fixed at login and never extended (FR-AUTH-010).
CREATE TABLE identity_session (
    id             UUID        PRIMARY KEY,
    account_id     UUID        NOT NULL REFERENCES identity_account (id),
    established_at TIMESTAMPTZ NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_identity_session_lifetime
        CHECK (expires_at > established_at
               AND expires_at <= established_at + INTERVAL '720 hours')
);

CREATE INDEX ix_identity_session_account ON identity_session (account_id);

-- Only the SHA-256 hash of each refresh token is stored, never the token itself.
CREATE TABLE identity_refresh_token (
    id         UUID        PRIMARY KEY,
    session_id UUID        NOT NULL REFERENCES identity_session (id),
    token_hash TEXT        NOT NULL,
    issued_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_identity_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_identity_refresh_token_hash_format CHECK (token_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX ix_identity_refresh_token_session ON identity_refresh_token (session_id);