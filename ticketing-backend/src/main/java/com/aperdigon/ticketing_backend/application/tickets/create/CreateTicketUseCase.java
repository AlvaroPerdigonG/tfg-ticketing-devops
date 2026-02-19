package com.aperdigon.ticketing_backend.application.tickets.create;

import com.aperdigon.ticketing_backend.application.ports.CategoryRepository;
import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.user.User;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public final class CreateTicketUseCase {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public CreateTicketUseCase(
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.ticketRepository = Guard.notNull(ticketRepository, "ticketRepository");
        this.categoryRepository = Guard.notNull(categoryRepository, "categoryRepository");
        this.userRepository = Guard.notNull(userRepository, "userRepository");
        this.clock = Guard.notNull(clock, "clock");
    }

    public CreateTicketResult execute(CreateTicketCommand command) {
        Guard.notNull(command, "command");

        User creator = userRepository.findById(command.actor().id())
                .orElseThrow(() -> new NotFoundException("User not found: " + command.actor().id().value()));

        Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + command.categoryId().value()));

        Ticket ticket = Ticket.openNew(
                command.title(),
                command.description(),
                category,
                creator,
                clock
        );

        Ticket saved = ticketRepository.save(ticket);
        return new CreateTicketResult(saved.id());
    }
}
