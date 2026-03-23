package com.aperdigon.ticketing_backend.unit.ticket;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.tickets.list.ListMyTicketsQuery;
import com.aperdigon.ticketing_backend.application.tickets.list.ListMyTicketsUseCase;
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

final class ListMyTicketsUseCaseTest {

    @Test
    @SpecificationRef(value = "TICKET-USER-03", level = TestLevel.UNIT, feature = "tickets-user.feature")
    void returns_only_tickets_created_by_actor_and_applies_status_and_text_filters() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneOffset.UTC);
        var ticketRepository = new InMemoryTicketRepository();
        var useCase = new ListMyTicketsUseCase(ticketRepository);

        var actor = DomainTestDataFactory.activeUser("actor@test.com", "Actor", UserRole.USER);
        var otherUser = DomainTestDataFactory.activeUser("other@test.com", "Other", UserRole.USER);
        var category = DomainTestDataFactory.activeCategory("General");

        var matchingTicket = DomainTestDataFactory.openTicket("Printer issue", "Needs toner", category, actor, clock);
        var otherStatusTicket = DomainTestDataFactory.openTicket("Laptop issue", "Battery warning", category, actor, clock);
        otherStatusTicket.changeStatus(TicketStatus.IN_PROGRESS, clock);
        var foreignTicket = DomainTestDataFactory.openTicket("Printer issue", "Other user's ticket", category, otherUser, clock);

        ticketRepository.save(matchingTicket);
        ticketRepository.save(otherStatusTicket);
        ticketRepository.save(foreignTicket);

        var result = useCase.execute(new ListMyTicketsQuery(
                new CurrentUser(actor.id(), actor.role()),
                TicketStatus.OPEN,
                "printer",
                PageRequest.of(0, 10)
        ));

        assertEquals(1, result.getTotalElements());
        assertEquals(matchingTicket.id(), result.getContent().get(0).id());
    }
}
