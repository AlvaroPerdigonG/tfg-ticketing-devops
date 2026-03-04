package com.aperdigon.ticketing_backend.application.tickets.create;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;

public record CreateTicketCommand(
        String title,
        String description,
        CategoryId categoryId,
        TicketPriority priority,
        CurrentUser actor
) {
    public CreateTicketCommand {
        Guard.notBlank(title, "title");
        Guard.notBlank(description, "description");
        Guard.notNull(categoryId, "categoryId");
        Guard.notNull(priority, "priority");
        Guard.notNull(actor, "actor");
    }
}
