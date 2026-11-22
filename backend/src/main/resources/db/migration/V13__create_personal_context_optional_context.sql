-- US-PROF-002 (S4-PROF-01/02/03, approved 2026-11-16; OPEN-S4-16/17): the user's optional, self-declared
-- gender and body-shape context. Separate from the preferences of V11, with its own revision, so that a
-- change of one never rewrites, blocks or clears the other (S4-PROF-02).
--
-- A NULL value means "not provided" (absent context). Clearing sets NULL and keeps the row, so the
-- revision line continues and a stale client can never pass the first-write guard again. Values are
-- declared by the user only: never inferred, never measurements, never a garment-category gate. The
-- vocabularies are delegated defaults selected on 2026-11-16 (OPEN-S4-16), not Product Owner approvals.
--
-- Owner: Personal Context. Every object below is in the personal_context schema (AR-01); owner_account_id
-- is the authenticated Identity account id with no foreign key into Identity (ADA-016, AR-04).
CREATE TABLE personal_context.personal_context_optional_context (
    owner_account_id UUID        PRIMARY KEY,
    gender_context   TEXT        NULL,
    body_shape       TEXT        NULL,
    revision         BIGINT      NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_personal_context_optional_context_gender CHECK (gender_context IS NULL OR gender_context IN (
        'WOMAN', 'MAN', 'NON_BINARY', 'ANOTHER_GENDER')),
    CONSTRAINT ck_personal_context_optional_context_body_shape CHECK (body_shape IS NULL OR body_shape IN (
        'RECTANGLE', 'TRIANGLE', 'INVERTED_TRIANGLE', 'HOURGLASS', 'ROUND')),
    CONSTRAINT ck_personal_context_optional_context_revision CHECK (revision >= 1),
    CONSTRAINT ck_personal_context_optional_context_updated_after_created CHECK (updated_at >= created_at)
);