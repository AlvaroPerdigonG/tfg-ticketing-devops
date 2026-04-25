package com.aperdigon.ticketing_backend.application.tickets.dashboard;

import com.aperdigon.ticketing_backend.domain.user.UserId;

public record AgentTicketCount(UserId assigneeId, String assigneeDisplayName, long count) {
}
