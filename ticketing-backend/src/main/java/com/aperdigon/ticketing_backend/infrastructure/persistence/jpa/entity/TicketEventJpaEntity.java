package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity;

import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_events")
public class TicketEventJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketJpaEntity ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private TicketEventType eventType;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserJpaEntity actor;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TicketEventJpaEntity() {
    }

    public TicketEventJpaEntity(UUID id, TicketJpaEntity ticket, TicketEventType eventType, UserJpaEntity actor, String payloadJson, Instant createdAt) {
        this.id = id;
        this.ticket = ticket;
        this.eventType = eventType;
        this.actor = actor;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public TicketJpaEntity getTicket() { return ticket; }
    public TicketEventType getEventType() { return eventType; }
    public UserJpaEntity getActor() { return actor; }
    public String getPayloadJson() { return payloadJson; }
    public Instant getCreatedAt() { return createdAt; }
}
