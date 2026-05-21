package com.aperdigon.ticketing_backend.application.tickets.delete;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public final class DeleteTicketUseCase {

    private final TicketRepository ticketRepository;

    public DeleteTicketUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void execute(DeleteTicketCommand command) {
        if (command.actor().role() != UserRole.ADMIN) {
            throw new ForbiddenException("Only ADMIN can delete tickets");
        }

        if (ticketRepository.findById(command.ticketId()).isEmpty()) {
            throw new NotFoundException("Ticket not found");
        }

        ticketRepository.deleteById(command.ticketId());
    }
}
