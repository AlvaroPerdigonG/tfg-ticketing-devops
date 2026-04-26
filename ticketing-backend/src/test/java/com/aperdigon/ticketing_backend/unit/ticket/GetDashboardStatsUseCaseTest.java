package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.AgentTicketCount;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.GetDashboardStatsUseCase;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class GetDashboardStatsUseCaseTest {

    @Test
    void agent_gets_dashboard_cards_and_charts_from_repository() {
        var repository = new StubTicketRepository();
        repository.unassigned = 4;
        repository.assignedToMe = 6;
        repository.inProgress = 3;
        repository.onHold = 2;
        repository.resolvedByAssignee = List.of(
                new AgentTicketCount(UserId.of(UUID.randomUUID()), "Agent One", 9)
        );
        repository.assignedByAssignee = List.of(
                new AgentTicketCount(UserId.of(UUID.randomUUID()), "Admin One", 11)
        );

        var actorId = UserId.of(UUID.randomUUID());
        var useCase = new GetDashboardStatsUseCase(repository);

        var result = useCase.execute(new CurrentUser(actorId, UserRole.AGENT));

        assertEquals(4, result.unassigned());
        assertEquals(6, result.assignedToMe());
        assertEquals(3, result.inProgress());
        assertEquals(2, result.onHold());
        assertEquals(1, result.resolvedByAssignee().size());
        assertEquals("Agent One", result.resolvedByAssignee().get(0).assigneeDisplayName());
        assertEquals(1, result.assignedByAssignee().size());
        assertEquals("Admin One", result.assignedByAssignee().get(0).assigneeDisplayName());
        assertEquals(actorId, repository.lastAssigneeRequested);
        assertEquals(TicketStatus.IN_PROGRESS, repository.lastCountByStatusRequested.get(0));
        assertEquals(TicketStatus.ON_HOLD, repository.lastCountByStatusRequested.get(1));
    }

    @Test
    void user_cannot_access_dashboard_stats() {
        var useCase = new GetDashboardStatsUseCase(new StubTicketRepository());

        assertThrows(ForbiddenException.class, () -> useCase.execute(
                new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.USER)
        ));
    }

    @Test
    void dashboard_queries_are_scoped_to_agent_and_admin_roles_only() {
        var repository = new StubTicketRepository();
        var useCase = new GetDashboardStatsUseCase(repository);

        useCase.execute(new CurrentUser(UserId.of(UUID.randomUUID()), UserRole.ADMIN));

        assertEquals(Set.of(UserRole.AGENT, UserRole.ADMIN), repository.lastRolesForResolved);
        assertEquals(Set.of(UserRole.AGENT, UserRole.ADMIN), repository.lastRolesForAssigned);
    }

    @Test
    void null_actor_is_rejected() {
        var useCase = new GetDashboardStatsUseCase(new StubTicketRepository());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    private static final class StubTicketRepository implements TicketRepository {
        private long unassigned;
        private long assignedToMe;
        private long inProgress;
        private long onHold;
        private List<AgentTicketCount> resolvedByAssignee = List.of();
        private List<AgentTicketCount> assignedByAssignee = List.of();

        private UserId lastAssigneeRequested;
        private final List<TicketStatus> lastCountByStatusRequested = new java.util.ArrayList<>();
        private Set<UserRole> lastRolesForResolved = Set.of();
        private Set<UserRole> lastRolesForAssigned = Set.of();

        @Override
        public Ticket save(Ticket ticket) {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public Optional<Ticket> findById(TicketId id) {
            return Optional.empty();
        }

        @Override
        public Page<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, Pageable pageable) {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public Page<Ticket> findAgentTickets(UserId actorId, com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope scope, TicketStatus status, String q, Pageable pageable) {
            throw new UnsupportedOperationException("Not needed in this test");
        }

        @Override
        public long countUnassigned() {
            return unassigned;
        }

        @Override
        public long countAssignedTo(UserId assigneeId) {
            this.lastAssigneeRequested = assigneeId;
            return assignedToMe;
        }

        @Override
        public long countByStatus(TicketStatus status) {
            lastCountByStatusRequested.add(status);
            return switch (status) {
                case IN_PROGRESS -> inProgress;
                case ON_HOLD -> onHold;
                default -> 0;
            };
        }

        @Override
        public List<AgentTicketCount> countAssignedByAssigneeRoles(Set<UserRole> roles) {
            this.lastRolesForAssigned = roles;
            return assignedByAssignee;
        }

        @Override
        public List<AgentTicketCount> countByStatusGroupedByAssigneeRoles(TicketStatus status, Set<UserRole> roles) {
            assertTrue(status == TicketStatus.RESOLVED);
            this.lastRolesForResolved = roles;
            return resolvedByAssignee;
        }
    }
}
