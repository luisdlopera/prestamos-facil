-- Concurrent approve/evaluate flows can race on the same loan or loan
-- application. Add optimistic locking so a stale write fails loudly
-- instead of silently overwriting a concurrent change.
ALTER TABLE loans ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE loan_applications ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
