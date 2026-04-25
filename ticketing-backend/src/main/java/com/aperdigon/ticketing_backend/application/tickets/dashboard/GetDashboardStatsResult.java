package com.aperdigon.ticketing_backend.application.tickets.dashboard;

import java.util.List;

public record GetDashboardStatsResult(
        long unassigned,
        long assignedToMe,
        long inProgress,
        long onHold,
        List<AgentTicketCount> resolvedByAssignee,
        List<AgentTicketCount> assignedByAssignee
) {
}
