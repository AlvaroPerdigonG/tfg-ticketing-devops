package com.aperdigon.ticketing_backend.application.tickets.dashboard;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public final class GetDashboardStatsUseCase {

    private static final Set<UserRole> DASHBOARD_ASSIGNEE_ROLES = Set.of(UserRole.AGENT, UserRole.ADMIN);

    private final TicketRepository ticketRepository;

    public GetDashboardStatsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
    }

    public GetDashboardStatsResult execute(CurrentUser actor) {
        Guard.notNull(actor, "actor");

        if (!actor.isAgentOrAdmin()) {
            throw new ForbiddenException("Only AGENT/ADMIN can access dashboard stats");
        }

        long unassigned = ticketRepository.countUnassigned();
        long assignedToMe = ticketRepository.countAssignedTo(actor.id());
        long inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long onHold = ticketRepository.countByStatus(TicketStatus.ON_HOLD);

        var resolvedByAssignee = ticketRepository.countByStatusGroupedByAssigneeRoles(
                TicketStatus.RESOLVED,
                DASHBOARD_ASSIGNEE_ROLES
        );

        var assignedByAssignee = ticketRepository.countAssignedByAssigneeRoles(DASHBOARD_ASSIGNEE_ROLES);

        return new GetDashboardStatsResult(
                unassigned,
                assignedToMe,
                inProgress,
                onHold,
                resolvedByAssignee,
                assignedByAssignee
        );
    }
}
