package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.projection;

import java.util.UUID;

public interface AssigneeTicketCountProjection {
    UUID getAssigneeId();
    String getAssigneeDisplayName();
    long getTicketCount();
}
