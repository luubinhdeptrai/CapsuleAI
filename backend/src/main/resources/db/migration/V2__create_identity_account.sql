-- US-AUTH-001: Identity-owned account identity and protected credential.
-- email stores the NORMALIZED identity (stripped, lower-cased by the application).
CREATE TABLE identity_account (
    id            UUID        PRIMARY KEY,
    email         TEXT        NOT NULL,
    password_hash TEXT        NOT NULL,
    display_name  TEXT        NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_identity_account_email UNIQUE (email),
    CONSTRAINT ck_identity_account_email_not_empty CHECK (length(email) > 0),
    CONSTRAINT ck_identity_account_password_hash_not_empty CHECK (length(password_hash) > 0)
);