-- US-BULK-001 (S4-BULK-02/05/06/07, approved 2026-11-16; OPEN-S4-25/26/27/28 delegated defaults): one import
-- batch per explicit start, with stable server-generated items. Nothing here makes anyone own a garment: a
-- garment exists only after an explicit per-item confirmation, which also sets the item to ACCEPTED in the
-- same transaction (S4-BULK-04).
--
-- Minimal by design (OPEN-S4-25): no garment values, no drafts and no photo bytes are stored here. An item
-- names its staged photo while REVIEWABLE and its garment once ACCEPTED. The staged photo is not a foreign key:
-- a stage may be used elsewhere and later purged by US-GAR-005 cleanup, and the item then reports its photo as
-- no longer usable. Batches and items stay as minimal action identity while the account exists (no purge yet).
--
-- Owner: Wardrobe. Every object below is in the wardrobe schema (AR-01); owner_account_id is the authenticated
-- Identity account id with no foreign key into Identity (ADA-016, AR-04).
CREATE TABLE wardrobe.wardrobe_bulk_import_batch (
    id               UUID        PRIMARY KEY,
    owner_account_id UUID        NOT NULL,
    creation_key     UUID        NOT NULL,
    item_count       SMALLINT    NOT NULL,
    status           TEXT        NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    expires_at       TIMESTAMPTZ NOT NULL,
    stopped_at       TIMESTAMPTZ NULL,

    -- The Idempotency-Key of the start request: one batch per logical start (OPEN-S4-27).
    CONSTRAINT uq_wardrobe_bulk_import_batch_creation_key UNIQUE (owner_account_id, creation_key),
    -- A hard ceiling; the configured limit (20 by default) is enforced by the application.
    CONSTRAINT ck_wardrobe_bulk_import_batch_item_count CHECK (item_count BETWEEN 1 AND 100),
    -- EXPIRED is derived from expires_at and never stored.
    CONSTRAINT ck_wardrobe_bulk_import_batch_status CHECK (status IN ('OPEN', 'STOPPED')),
    CONSTRAINT ck_wardrobe_bulk_import_batch_stopped CHECK ((status = 'STOPPED') = (stopped_at IS NOT NULL)),
    CONSTRAINT ck_wardrobe_bulk_import_batch_lifetime CHECK (expires_at > created_at),
    CONSTRAINT ck_wardrobe_bulk_import_batch_stopped_after_created CHECK (stopped_at IS NULL OR stopped_at >= created_at)
);

-- The owner's active batches (admission limit and the resume list).
CREATE INDEX ix_wardrobe_bulk_import_batch_owner_open
    ON wardrobe.wardrobe_bulk_import_batch (owner_account_id, expires_at)
    WHERE status = 'OPEN';

CREATE TABLE wardrobe.wardrobe_bulk_import_item (
    id              UUID        PRIMARY KEY,
    batch_id        UUID        NOT NULL
        REFERENCES wardrobe.wardrobe_bulk_import_batch (id) ON DELETE CASCADE,
    queue_position  SMALLINT    NOT NULL,
    acceptance_key  UUID        NOT NULL,
    status          TEXT        NOT NULL,
    photo_upload_id UUID        NULL,
    garment_id      UUID        NULL REFERENCES wardrobe.wardrobe_garment (id),
    revision        BIGINT      NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_wardrobe_bulk_import_item_position UNIQUE (batch_id, queue_position),
    -- The creation key of the one garment this item may become (S4-BULK-05).
    CONSTRAINT uq_wardrobe_bulk_import_item_acceptance_key UNIQUE (acceptance_key),
    CONSTRAINT ck_wardrobe_bulk_import_item_position CHECK (queue_position BETWEEN 1 AND 100),
    CONSTRAINT ck_wardrobe_bulk_import_item_status
        CHECK (status IN ('AWAITING_PHOTO', 'REVIEWABLE', 'ACCEPTED', 'CANCELED')),
    CONSTRAINT ck_wardrobe_bulk_import_item_photo CHECK ((status = 'REVIEWABLE') = (photo_upload_id IS NOT NULL)),
    CONSTRAINT ck_wardrobe_bulk_import_item_garment CHECK ((status = 'ACCEPTED') = (garment_id IS NOT NULL)),
    CONSTRAINT ck_wardrobe_bulk_import_item_revision CHECK (revision >= 1)
);

-- One staged photo belongs to at most one item (no cross-item association, AC-06).
CREATE UNIQUE INDEX uq_wardrobe_bulk_import_item_linked_photo
    ON wardrobe.wardrobe_bulk_import_item (photo_upload_id)
    WHERE photo_upload_id IS NOT NULL;

-- One garment comes from at most one item.
CREATE UNIQUE INDEX uq_wardrobe_bulk_import_item_garment
    ON wardrobe.wardrobe_bulk_import_item (garment_id)
    WHERE garment_id IS NOT NULL;
