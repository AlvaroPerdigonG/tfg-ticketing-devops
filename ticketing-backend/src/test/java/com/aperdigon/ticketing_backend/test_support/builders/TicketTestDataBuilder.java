package com.aperdigon.ticketing_backend.test_support.builders;

import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;

import java.time.Instant;
import java.util.UUID;

public final class TicketTestDataBuilder {
    private UUID id = UUID.randomUUID();
    private String title = "Test ticket";
    private String description = "Test ticket description";
    private TicketStatus status = TicketStatus.OPEN;
    private TicketPriority priority = TicketPriority.MEDIUM;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = createdAt;
    private UserJpaEntity createdBy;
    private CategoryJpaEntity category;
    private UserJpaEntity assignedTo;

    private TicketTestDataBuilder() {
    }

    public static TicketTestDataBuilder aTicket() {
        return new TicketTestDataBuilder();
    }

    public TicketTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public TicketTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public TicketTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TicketTestDataBuilder withStatus(TicketStatus status) {
        this.status = status;
        return this;
    }

    public TicketTestDataBuilder withPriority(TicketPriority priority) {
        this.priority = priority;
        return this;
    }

    public TicketTestDataBuilder createdBy(UserJpaEntity createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public TicketTestDataBuilder inCategory(CategoryJpaEntity category) {
        this.category = category;
        return this;
    }

    public TicketTestDataBuilder assignedTo(UserJpaEntity assignedTo) {
        this.assignedTo = assignedTo;
        return this;
    }

    public TicketTestDataBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TicketTestDataBuilder updatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public TicketJpaEntity build() {
        if (createdBy == null) {
            throw new IllegalStateException("createdBy is required");
        }
        if (category == null) {
            throw new IllegalStateException("category is required");
        }

        return new TicketJpaEntity(
                id,
                title,
                description,
                status,
                priority,
                createdAt,
                updatedAt,
                createdBy,
                category,
                assignedTo
        );
    }
}
