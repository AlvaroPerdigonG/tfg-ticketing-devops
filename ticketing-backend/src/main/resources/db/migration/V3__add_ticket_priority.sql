ALTER TABLE tickets
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

CREATE INDEX idx_tickets_priority ON tickets(priority);
