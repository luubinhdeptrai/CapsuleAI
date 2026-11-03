-- US-AUTH-004: current-session revocation (FR-AUTH-007, DATA-AUTH-005).
-- NULL revoked_at = not revoked. Existing sessions therefore stay usable.
ALTER TABLE identity_session
    ADD COLUMN revoked_at        TIMESTAMPTZ NULL,
    ADD COLUMN revocation_reason TEXT        NULL;

ALTER TABLE identity_session
    ADD CONSTRAINT ck_identity_session_revocation_recorded
        CHECK ((revoked_at IS NULL) = (revocation_reason IS NULL)),
    ADD CONSTRAINT ck_identity_session_revocation_reason
        CHECK (revocation_reason IN ('LOGOUT')),
    ADD CONSTRAINT ck_identity_session_revoked_after_established
        CHECK (revoked_at IS NULL OR revoked_at >= established_at);