package com.aperdigon.ticketing_backend.integration.ticket;

import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketEventSpringDataRepository;
import com.aperdigon.ticketing_backend.specification.SpecificationRef;
import com.aperdigon.ticketing_backend.specification.TestLevel;
import com.aperdigon.ticketing_backend.test_support.builders.CategoryTestDataBuilder;
import com.aperdigon.ticketing_backend.test_support.integration.AbstractAuthenticatedApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CreateTicketApiIntegrationTest extends AbstractAuthenticatedApiIntegrationTest {

    private static final String DEFAULT_PASSWORD = "secret123";

    @Autowired
    private TicketEventSpringDataRepository ticketEventRepository;

    @BeforeEach
    void setUp() {
        clearPersistedData();
    }

    @Test
    @SpecificationRef(value = "TICKET-USER-01", level = TestLevel.INTEGRATION, feature = "tickets-user.feature")
    void authenticated_user_can_create_ticket_and_it_is_persisted_with_creation_event() throws Exception {
        var requester = persistActiveUser("user@test.com", "User", DEFAULT_PASSWORD, UserRole.USER);
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));
        String token = loginAndExtractAccessToken("user@test.com", DEFAULT_PASSWORD);

        var mvcResult = mockMvc.perform(post("/api/tickets")
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "title", "Printer not working",
                                "description", "The printer shows error E23",
                                "categoryId", category.getId(),
                                "priority", "HIGH"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/tickets/.+")))
                .andExpect(jsonPath("$.ticketId", notNullValue()))
                .andReturn();

        String ticketId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("ticketId").asText();
        var savedTicket = ticketRepository.findById(java.util.UUID.fromString(ticketId)).orElseThrow();
        var savedEvents = ticketEventRepository.findByTicket_IdOrderByCreatedAtAsc(savedTicket.getId());

        assertEquals("Printer not working", savedTicket.getTitle());
        assertEquals("The printer shows error E23", savedTicket.getDescription());
        assertEquals(TicketStatus.OPEN, savedTicket.getStatus());
        assertEquals(TicketPriority.HIGH, savedTicket.getPriority());
        assertEquals(requester.getId(), savedTicket.getCreatedBy().getId());
        assertEquals(category.getId(), savedTicket.getCategory().getId());
        assertEquals(1, savedEvents.size());
        assertEquals(TicketEventType.TICKET_CREATED, savedEvents.get(0).getEventType());
        assertEquals(requester.getId(), savedEvents.get(0).getActor().getId());
        assertEquals("{}", savedEvents.get(0).getPayloadJson());
    }

    @Test
    @SpecificationRef(value = "TICKET-USER-02", level = TestLevel.INTEGRATION, feature = "tickets-user.feature")
    void creating_ticket_without_token_returns_unauthorized_and_does_not_persist_any_ticket() throws Exception {
        var category = persistCategory(CategoryTestDataBuilder.aCategory().withName("Hardware"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "title", "Printer not working",
                                "description", "The printer shows error E23",
                                "categoryId", category.getId(),
                                "priority", "HIGH"
                        ))))
                .andExpect(status().isUnauthorized());

        assertTrue(ticketRepository.findAll().isEmpty());
        assertTrue(ticketEventRepository.findAll().isEmpty());
    }
}
