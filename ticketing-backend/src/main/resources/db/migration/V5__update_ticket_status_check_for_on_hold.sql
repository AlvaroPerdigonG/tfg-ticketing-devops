ALTER TABLE tickets DROP CONSTRAINT IF EXISTS tickets_status_check;

ALTER TABLE tickets
    ADD CONSTRAINT tickets_status_check
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED'));
