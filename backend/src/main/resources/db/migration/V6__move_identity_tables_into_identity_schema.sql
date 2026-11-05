-- Architecture refinement AR-01 (2026-11-06): every persistence owner has its own
-- PostgreSQL schema. Identity owns the schema "identity".
--
-- V2-V5 created the Identity tables without a schema name, so they live in Flyway's
-- default schema (the connection's default schema; "public" under PostgreSQL's
-- default search_path). ${flyway:defaultSchema} names exactly that schema, so this
-- migration moves the tables from where they really are instead of assuming "public".
--
-- ALTER TABLE ... SET SCHEMA changes only the namespace of the existing tables: rows,
-- column values, indexes, constraints, foreign keys and privileges move with them.
-- Nothing is dropped, recreated or copied. Flyway runs this file in one transaction,
-- so a failure leaves every table where it was.
--
-- The schema history table stays in Flyway's default schema; Flyway's configuration
-- is unchanged, so V1-V5 are never applied again.

CREATE SCHEMA IF NOT EXISTS identity;

-- Children move after their parents only for readability; foreign keys follow the
-- tables, not their names, so the order does not affect them.
ALTER TABLE ${flyway:defaultSchema}.identity_account       SET SCHEMA identity;
ALTER TABLE ${flyway:defaultSchema}.identity_session       SET SCHEMA identity;
ALTER TABLE ${flyway:defaultSchema}.identity_refresh_token SET SCHEMA identity;
