package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.tickets.list.ListTicketsQuery;
import com.aperdigon.ticketing_backend.application.tickets.list.ListTicketsUseCase;
import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.DomainTestDataFactory;
import com.aperdigon.ticketing_backend.test_support.InMemoryTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ListTicketsUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-AGENT-03", level = TestLevel.UNIT, feature = "tickets-agent.feature")
    void agent_can_list_manageable_tickets_filtered_by_scope_status_and_query() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new ListTicketsUseCase(ticketRepository);

        var creator = DomainTestDataFactory.activeUser("creator@test.com", "Creator", UserRole.USER);
        var agent = DomainTestDataFactory.activeUser("agent@test.com", "Agent", UserRole.AGENT);
        var anotherAgent = DomainTestDataFactory.activeUser("another@test.com", "Another Agent", UserRole.AGENT);
        var category = DomainTestDataFactory.activeCategory("General");

        var myAssignedTicket = DomainTestDataFactory.openTicket("Printer issue", "Needs onsite review", category, creator, clock);
        myAssignedTicket.assignTo(agent.id(), clock);
        myAssignedTicket.changeStatus(TicketStatus.IN_PROGRESS, clock);

        var otherAgentsTicket = DomainTestDataFactory.openTicket("Network issue", "Router offline", category, creator, clock);
        otherAgentsTicket.assignTo(anotherAgent.id(), clock);
        otherAgentsTicket.changeStatus(TicketStatus.IN_PROGRESS, clock);

        var unassignedTicket = DomainTestDataFactory.openTicket("Printer toner", "Need toner replacement", category, creator, clock);

        ticketRepository.save(myAssignedTicket);
        ticketRepository.save(otherAgentsTicket);
        ticketRepository.save(unassignedTicket);

        var result = useCase.execute(new ListTicketsQuery(
                new CurrentUser(agent.id(), agent.role()),
                TicketQueueScope.MINE,
                TicketStatus.IN_PROGRESS,
                "printer",
                PageRequest.of(0, 10)
        ));

        assertEquals(1, result.getTotalElements());
        assertEquals(myAssignedTicket.id(), result.getContent().get(0).id());
    }

    @Test
    void non_operational_user_cannot_list_manageable_tickets() {
        var useCase = new ListTicketsUseCase(new InMemoryTicketRepository());

        assertThrows(ForbiddenException.class, () -> useCase.execute(new ListTicketsQuery(
                new CurrentUser(com.aperdigon.ticketing_backend.domain.user.UserId.of(java.util.UUID.randomUUID()), UserRole.USER),
                TicketQueueScope.ALL,
                null,
                null,
                PageRequest.of(0, 10)
        )));
    }
}
