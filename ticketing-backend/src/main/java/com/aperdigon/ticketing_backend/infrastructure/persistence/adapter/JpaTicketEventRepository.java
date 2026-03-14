package com.aperdigon.ticketing_backend.infrastructure.persistence.adapter;

import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketEventJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketEventSpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketSpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class JpaTicketEventRepository implements TicketEventRepository {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private final TicketEventSpringDataRepository eventRepository;
    private final TicketSpringDataRepository ticketRepository;
    private final UserSpringDataRepository userRepository;
    private final ObjectMapper objectMapper;

    public JpaTicketEventRepository(
            TicketEventSpringDataRepository eventRepository,
            TicketSpringDataRepository ticketRepository,
            UserSpringDataRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public TicketEvent save(TicketEvent event) {
        try {
            var ticketRef = ticketRepository.getReferenceById(event.ticketId().value());
            var actorRef = event.actorUserId() == null ? null : userRepository.getReferenceById(event.actorUserId().value());
            String payloadJson = objectMapper.writeValueAsString(event.payload());
            var saved = eventRepository.save(new TicketEventJpaEntity(
                    event.id(),
                    ticketRef,
                    event.type(),
                    actorRef,
                    payloadJson,
                    event.createdAt()
            ));
            return toDomain(saved);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to persist ticket event", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketEvent> findByTicketId(TicketId ticketId) {
        return eventRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TicketEvent toDomain(TicketEventJpaEntity e) {
        try {
            Map<String, String> payload = objectMapper.readValue(e.getPayloadJson(), MAP_TYPE);
            return new TicketEvent(
                    e.getId(),
                    new TicketId(e.getTicket().getId()),
                    e.getEventType(),
                    e.getActor() == null ? null : new UserId(e.getActor().getId()),
                    payload,
                    e.getCreatedAt()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse ticket event payload", ex);
        }
    }
}
