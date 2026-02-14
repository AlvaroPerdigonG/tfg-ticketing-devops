package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_comments")
public class CommentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketJpaEntity ticket;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private UserJpaEntity author;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentJpaEntity() {}

    public CommentJpaEntity(UUID id, TicketJpaEntity ticket, UserJpaEntity author, String content, Instant createdAt) {
        this.id = id;
        this.ticket = ticket;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public TicketJpaEntity getTicket() { return ticket; }
    public UserJpaEntity getAuthor() { return author; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
