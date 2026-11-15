-- US-GAR-002 (S3-GAR-02): optimistic revision of a garment's accepted profile.
-- Every garment that exists when this migration runs starts at revision 1, as does every
-- garment created later; each accepted edit that changes the profile adds exactly 1.
-- The revision is concurrency metadata, not one of the 13 garment properties (DATA-GAR-002).
--
-- Owner: Wardrobe. This migration changes only the wardrobe schema (AR-01). In PostgreSQL 11
-- and later, adding a NOT NULL column with a constant default does not rewrite the table.
ALTER TABLE wardrobe.wardrobe_garment
    ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;

ALTER TABLE wardrobe.wardrobe_garment
    ADD CONSTRAINT ck_wardrobe_garment_revision_positive CHECK (revision >= 1);