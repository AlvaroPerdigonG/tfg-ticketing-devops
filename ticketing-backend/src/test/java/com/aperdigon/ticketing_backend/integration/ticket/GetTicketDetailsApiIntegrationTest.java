package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketEventSpringDataRepository;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.builders.TicketTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetTicketDetailsApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    @Autowired
    private TicketEventSpringDataRepository ticketEventRepository;

    @BeforeEach
    void setUp() {
        clearPersistedData();
    }

    @Test
    @SpecificationRef(value = "TICKET-USER-04", level = TestLevel.INTEGRATION, feature = "tickets-user.feature")
    void user_can_get_detail_of_their_own_ticket() throws Exception {
        var requester = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Printer issue")
                .withDescription("Paper jam on tray 2")
                .withStatus(TicketStatus.OPEN)
                .createdAt(Instant.parse("2026-03-04T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-04T10:00:00Z"))
                .createdBy(requester)
                .inCategory(category));

        String token = loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.getId().toString()))
                .andExpect(jsonPath("$.title").value("Printer issue"))
                .andExpect(jsonPath("$.description").value("Paper jam on tray 2"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdByUserId").value(requester.getId().toString()))
                .andExpect(jsonPath("$.createdByDisplayName").value("User"))
                .andExpect(jsonPath("$.timeline[0].kind").value("MESSAGE"))
                .andExpect(jsonPath("$.timeline[0].content").value("Paper jam on tray 2"))
                .andExpect(jsonPath("$.availableTransitions[0]").value("IN_PROGRESS"));
    }


    @Test
    void user_cannot_get_ticket_detail_of_another_user() throws Exception {
        var owner = persistActiveUser("owner@test.com", "Owner", DEFAULT_PASSWORD, UserRole.USER);
        persistActiveUser("viewer@test.com", "Viewer", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Private ticket")
                .withDescription("Should not be visible")
                .createdAt(Instant.parse("2026-03-04T11:00:00Z"))
                .updatedAt(Instant.parse("2026-03-04T11:00:00Z"))
                .createdBy(owner)
                .inCategory(category));

        String viewerToken = loginAndExtractAccessToken("viewer@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .with(bearerToken(viewerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @SpecificationRef(value = "TICKET-AGENT-04", level = TestLevel.INTEGRATION, feature = "tickets-agent.feature")
    void agent_can_assign_ticket_to_self_and_assignment_is_reflected_in_detail_and_events() throws Exception {
        var creator = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var agent = persistActiveUser("agent@test.com", "Agent", DEFAULT_PASSWORD, UserRole.AGENT);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        var ticket = persistTicket(TicketTestDataBuilder.aTicket()
                .withTitle("Network issue")
                .withDescription("Switch offline")
                .createdAt(Instant.parse("2026-03-05T10:00:00Z"))
                .updatedAt(Instant.parse("2026-03-05T10:00:00Z"))
                .createdBy(creator)
                .inCategory(category));

        String agentToken = loginAndExtractAccessToken("agent@test.com", DEFAULT_PASSWORD);

        mockMvc.perform(patch("/api/tickets/{id}/assignment/me", ticket.getId())
                        .with(bearerToken(agentToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .with(bearerToken(agentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedToUserId").value(agent.getId().toString()))
                .andExpect(jsonPath("$.assignedToDisplayName").value("Agent"))
                .andExpect(jsonPath("$.timeline[?(@.eventType=='ASSIGNED_TO_ME')]").exists());

        var savedEvents = ticketEventRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId());
        assertEquals(1, savedEvents.size());
        assertEquals(TicketEventType.ASSIGNED_TO_ME, savedEvents.get(0).getEventType());
        assertEquals(agent.getId(), savedEvents.get(0).getActor().getId());
    }
}
