package com.aperdigon.ticketing_backend.api.tickets.dashboard;

import com.aperdigon.ticketing_backend.application.tickets.dashboard.AgentTicketCount;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.GetDashboardStatsResult;

import java.util.List;
import java.util.UUID;

public record DashboardStatsResponse(
        DashboardCardsResponse cards,
        DashboardChartsResponse charts
) {

    public static DashboardStatsResponse from(GetDashboardStatsResult result) {
        return new DashboardStatsResponse(
                new DashboardCardsResponse(
                        result.unassigned(),
                        result.assignedToMe(),
                        result.inProgress(),
                        result.onHold()
                ),
                new DashboardChartsResponse(
                        result.resolvedByAssignee().stream().map(AgentCountResponse::from).toList(),
                        result.assignedByAssignee().stream().map(AgentCountResponse::from).toList()
                )
        );
    }

    public record DashboardCardsResponse(
            long unassigned,
            long assignedToMe,
            long inProgress,
            long onHold
    ) {
    }

    public record DashboardChartsResponse(
            List<AgentCountResponse> resolvedByAgent,
            List<AgentCountResponse> assignedByAgent
    ) {
    }

    public record AgentCountResponse(
            UUID assigneeUserId,
            String assigneeDisplayName,
            long count
    ) {
        public static AgentCountResponse from(AgentTicketCount source) {
            return new AgentCountResponse(source.assigneeId().value(), source.assigneeDisplayName(), source.count());
        }
    }
}
