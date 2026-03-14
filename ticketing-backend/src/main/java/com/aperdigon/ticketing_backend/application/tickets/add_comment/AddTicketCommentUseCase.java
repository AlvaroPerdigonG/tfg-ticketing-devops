package com.aperdigon.ticketing_backend.application.tickets.add_comment;

import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AddTicketCommentUseCase {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final Clock clock;

    public AddTicketCommentUseCase(TicketRepository ticketRepository, TicketEventRepository ticketEventRepository, Clock clock) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
        this.ticketEventRepository = Guard.notNull(ticketEventRepository, "ticketEventRepository");
        this.clock = Guard.notNull(clock, "clock");
    }

    public AddTicketCommentResult execute(AddTicketCommentCommand command) {
        Guard.notNull(command, "command");

        Ticket ticket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + command.ticketId().value()));

        if (!command.actor().isAgentOrAdmin() && !ticket.createdBy().equals(command.actor().id())) {
            throw new ForbiddenException("You cannot comment this ticket");
        }

        if (ticket.status() == TicketStatus.RESOLVED) {
            throw new ForbiddenException("Resolved ticket does not accept new comments");
        }

        var createdComment = ticket.addComment(command.content(), command.actor().id(), clock);
        ticketRepository.save(ticket);

        ticketEventRepository.save(new TicketEvent(
                UUID.randomUUID(),
                ticket.id(),
                TicketEventType.COMMENT_ADDED,
                command.actor().id(),
                Map.of("commentId", createdComment.id().value().toString()),
                Instant.now(clock)
        ));

        return new AddTicketCommentResult(
                createdComment.id(),
                ticket.id(),
                createdComment.authorId(),
                createdComment.content(),
                createdComment.createdAt()
        );
    }
}
