-- US-GAR-005 (S4-GAR-01/02/03, approved 2026-11-16; OPEN-S4-02/04): confirmed removal keeps only a
-- minimized row of the garment, and one durable cleanup obligation per removed garment drives the
-- purge of rich data and of the private photo within 30 days of the original removal.
--
-- A minimized row keeps: id, owner_account_id, creation_key, creation_fingerprint, category, color,
-- value_provenance, created_at, removed_at, revision and minimized_at. Every other garment column is
-- NULL, and its Material/Style rows and attached photo row are deleted.
--
-- Owner: Wardrobe. Every object below is in the wardrobe schema (AR-01); no other schema is referenced.
ALTER TABLE wardrobe.wardrobe_garment
    ADD COLUMN minimized_at TIMESTAMPTZ NULL;

-- Explicit IS NULL tests on every purgeable column: a minimized row cannot keep rich data, even if a
-- future code path forgot one column (defence in depth for DATA-RET-002).
ALTER TABLE wardrobe.wardrobe_garment
    ADD CONSTRAINT ck_wardrobe_garment_minimized_shape CHECK (minimized_at IS NULL OR (
        removed_at IS NOT NULL AND minimized_at >= removed_at
        AND subtype IS NULL AND pattern IS NULL AND fit IS NULL AND neckline IS NULL
        AND sleeve_length IS NULL AND length IS NULL AND silhouette IS NULL
        AND brand IS NULL AND price_amount IS NULL AND price_currency IS NULL));

-- One obligation per removed garment. purge_deadline is fixed at insert time from the original
-- removal, so a retried removal can never restart the 30-day clock. next_attempt_at doubles as the
-- worker lease. photo_object_key is copied from the attached photo at removal time, so the object can
-- still be deleted after the photo row itself has been purged.
CREATE TABLE wardrobe.wardrobe_garment_removal_cleanup (
    garment_id        UUID        PRIMARY KEY REFERENCES wardrobe.wardrobe_garment (id),
    owner_account_id  UUID        NOT NULL,
    removed_at        TIMESTAMPTZ NOT NULL,
    purge_deadline    TIMESTAMPTZ NOT NULL,
    photo_upload_id   UUID        NULL,
    photo_object_key  TEXT        NULL,
    next_attempt_at   TIMESTAMPTZ NOT NULL,
    attempts          INTEGER     NOT NULL DEFAULT 0,
    data_minimized_at TIMESTAMPTZ NULL,
    object_deleted_at TIMESTAMPTZ NULL,
    completed_at      TIMESTAMPTZ NULL,

    CONSTRAINT ck_wardrobe_garment_removal_cleanup_deadline CHECK (purge_deadline > removed_at),
    CONSTRAINT ck_wardrobe_garment_removal_cleanup_photo_pair
        CHECK ((photo_upload_id IS NULL) = (photo_object_key IS NULL)),
    CONSTRAINT ck_wardrobe_garment_removal_cleanup_object_key
        CHECK (photo_object_key IS NULL OR photo_object_key ~ '^photos/[0-9a-f-]{36}\.jpg$'),
    CONSTRAINT ck_wardrobe_garment_removal_cleanup_object_deleted
        CHECK (object_deleted_at IS NULL OR photo_object_key IS NOT NULL),
    CONSTRAINT ck_wardrobe_garment_removal_cleanup_completed
        CHECK (completed_at IS NULL OR (data_minimized_at IS NOT NULL
            AND (photo_object_key IS NULL OR object_deleted_at IS NOT NULL))),
    CONSTRAINT ck_wardrobe_garment_removal_cleanup_attempts CHECK (attempts >= 0)
);

-- Incomplete obligations in due order, for the cleanup worker.
CREATE INDEX ix_wardrobe_garment_removal_cleanup_due
    ON wardrobe.wardrobe_garment_removal_cleanup (next_attempt_at)
    WHERE completed_at IS NULL;