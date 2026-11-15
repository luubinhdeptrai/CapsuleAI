-- US-PROF-001 (S3-PROF-01/02/04): the user's declared profile styles and common-need priorities.
-- An owner without a row has declared nothing yet (absent context, not a persona). A need without a
-- row is unset; a need with priority 0 is explicitly Not Relevant (BRULE-PROF-002, BRULE-COV-002).
-- Profile styles are their own vocabulary (SRS 4.10), never garment Style values.
--
-- Owner: Personal Context. This first Personal Context migration creates the module's own schema
-- (AR-01); every object below is in it. owner_account_id is the authenticated Identity account id,
-- with no foreign key into Identity (ADA-016, AR-04).
CREATE SCHEMA IF NOT EXISTS personal_context;

CREATE TABLE personal_context.personal_context_preferences (
    owner_account_id UUID        PRIMARY KEY,
    revision         BIGINT      NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_personal_context_preferences_revision CHECK (revision >= 1),
    CONSTRAINT ck_personal_context_preferences_updated_after_created CHECK (updated_at >= created_at)
);

CREATE TABLE personal_context.personal_context_preferred_style (
    owner_account_id UUID NOT NULL
        REFERENCES personal_context.personal_context_preferences (owner_account_id) ON DELETE CASCADE,
    style            TEXT NOT NULL,

    CONSTRAINT pk_personal_context_preferred_style PRIMARY KEY (owner_account_id, style),
    CONSTRAINT ck_personal_context_preferred_style CHECK (style IN (
        'MINIMAL', 'CASUAL', 'SMART_CASUAL', 'CLASSIC', 'STREETWEAR', 'SPORTY', 'FORMAL'))
);

CREATE TABLE personal_context.personal_context_common_need (
    owner_account_id UUID     NOT NULL
        REFERENCES personal_context.personal_context_preferences (owner_account_id) ON DELETE CASCADE,
    need             TEXT     NOT NULL,
    priority         SMALLINT NOT NULL,

    CONSTRAINT pk_personal_context_common_need PRIMARY KEY (owner_account_id, need),
    CONSTRAINT ck_personal_context_common_need CHECK (need IN (
        'EVERYDAY_CASUAL', 'WORK_PROFESSIONAL', 'SCHOOL_UNIVERSITY', 'FORMAL_SPECIAL_EVENT', 'TRAVEL',
        'SPORT_ACTIVE')),
    -- Not Relevant = 0, Low = 1, Medium = 2, High = 3 (SRS 3.14.1).
    CONSTRAINT ck_personal_context_common_need_priority CHECK (priority BETWEEN 0 AND 3)
);