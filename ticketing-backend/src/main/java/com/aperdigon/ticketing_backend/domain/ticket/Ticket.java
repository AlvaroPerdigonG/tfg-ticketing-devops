package com.aperdigon.ticketing_backend.domain.ticket;

import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketAlreadyResolved;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketInvalidStatusTransition;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Ticket {
    private final TicketId id;

    private String title;
    private String description;

    private final CategoryId categoryId;
    private final UserId createdBy;

    private UserId assignedTo; // nullable en MVP
    private TicketStatus status;

    private final Instant createdAt;
    private Instant updatedAt;

    private final List<Comment> comments;

    private Ticket(
            TicketId id,
            String title,
            String description,
            CategoryId categoryId,
            UserId createdBy,
            TicketStatus status,
            Instant createdAt,
            Instant updatedAt,
            UserId assignedTo,
            List<Comment> comments
    ) {
        this.id = Guard.notNull(id, "id");
        this.title = Guard.notBlank(title, "title");
        this.description = Guard.notBlank(description, "description");
        this.categoryId = Guard.notNull(categoryId, "categoryId");
        this.createdBy = Guard.notNull(createdBy, "createdBy");
        this.status = Guard.notNull(status, "status");
        this.createdAt = Guard.notNull(createdAt, "createdAt");
        this.updatedAt = Guard.notNull(updatedAt, "updatedAt");
        this.assignedTo = assignedTo;
        this.comments = new ArrayList<>(comments == null ? List.of() : comments);
    }

    // Factory de dominio (UC1)
    public static Ticket openNew(String title, String description, Category category, User creator, Clock clock) {
        Guard.notNull(category, "category");
        Guard.notNull(creator, "creator");
        Instant now = Instant.now(Guard.notNull(clock, "clock"));

        return new Ticket(
                new TicketId(UUID.randomUUID()),
                title,
                description,
                category.id(),
                creator.id(),
                TicketStatus.OPEN,
                now,
                now,
                null,
                List.of()
        );
    }

    // Comportamiento de dominio (UC4)
    public void changeStatus(TicketStatus newStatus, Clock clock) {
        Guard.notNull(newStatus, "newStatus");

        if (this.status == TicketStatus.RESOLVED) {
            throw new TicketAlreadyResolved();
        }

        if (!isValidTransition(this.status, newStatus)) {
            throw new TicketInvalidStatusTransition(this.status, newStatus);
        }

        this.status = newStatus;
        touch(clock);
    }

    // Comportamiento de dominio (UC5)
    public Comment addComment(String content, User author, Clock clock) {
        Guard.notNull(author, "author");
        Instant now = Instant.now(Guard.notNull(clock, "clock"));

        Comment comment = new Comment(new CommentId(UUID.randomUUID()), content, author, now);
        this.comments.add(comment);
        touch(clock);
        return comment;
    }

    // Opcional: asignación (para UC6/realismo)
    public void assignTo(User agent, Clock clock) {
        Guard.notNull(agent, "agent");
        this.assignedTo = agent.id();
        touch(clock);
    }

    private void touch(Clock clock) {
        this.updatedAt = Instant.now(Guard.notNull(clock, "clock"));
    }

    private static boolean isValidTransition(TicketStatus from, TicketStatus to) {
        return switch (from) {
            case OPEN -> to == TicketStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == TicketStatus.RESOLVED;
            case RESOLVED -> false;
        };
    }

    // Getters (inmutabilidad parcial: estado interno controlado por métodos)
    public TicketId id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public CategoryId categoryId() { return categoryId; }
    public UserId createdBy() { return createdBy; }
    public UserId assignedTo() { return assignedTo; }
    public TicketStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public List<Comment> comments() { return Collections.unmodifiableList(comments); }
}
