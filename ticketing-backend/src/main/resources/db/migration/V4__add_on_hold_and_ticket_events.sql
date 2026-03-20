CREATE TABLE ticket_events (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL,
    actor_user_id UUID REFERENCES users(id),
    payload_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ticket_events_ticket_created_at ON ticket_events(ticket_id, created_at);
CREATE INDEX idx_ticket_events_type ON ticket_events(event_type);
