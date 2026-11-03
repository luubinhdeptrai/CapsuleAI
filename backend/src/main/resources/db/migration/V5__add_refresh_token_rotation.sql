-- US-AUTH-003: refresh-token rotation and reuse detection (FR-AUTH-013, FR-AUTH-014).
-- consumed_at NULL = the session's current refresh token.
ALTER TABLE identity_refresh_token
    ADD COLUMN consumed_at TIMESTAMPTZ NULL;

ALTER TABLE identity_refresh_token
    ADD CONSTRAINT ck_identity_refresh_token_consumed_after_issue
        CHECK (consumed_at IS NULL OR consumed_at >= issued_at);

-- A login session has at most one current (unconsumed) refresh token.
CREATE UNIQUE INDEX ux_identity_refresh_token_current
    ON identity_refresh_token (session_id)
    WHERE consumed_at IS NULL;

-- Reuse detection revokes the affected session with its own reason.
ALTER TABLE identity_session
    DROP CONSTRAINT ck_identity_session_revocation_reason;

ALTER TABLE identity_session
    ADD CONSTRAINT ck_identity_session_revocation_reason
        CHECK (revocation_reason IN ('LOGOUT', 'REFRESH_TOKEN_REUSE'));