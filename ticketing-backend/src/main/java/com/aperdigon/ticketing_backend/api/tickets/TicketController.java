package com.aperdigon.ticketing_backend.api.tickets;

import java.net.URI;
import java.util.UUID;

import com.aperdigon.ticketing_backend.api.shared.currentuser.CurrentUserProvider;
import com.aperdigon.ticketing_backend.api.tickets.change_status.ChangeTicketStatusRequest;
import com.aperdigon.ticketing_backend.api.tickets.create.CreateTicketRequest;
import com.aperdigon.ticketing_backend.api.tickets.create.CreateTicketResponse;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusCommand;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusUseCase;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketCommand;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketUseCase;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final ChangeTicketStatusUseCase changeTicketStatusUseCase;
    private final CurrentUserProvider currentUserProvider;

    public TicketController(
            CreateTicketUseCase createTicketUseCase,
            ChangeTicketStatusUseCase changeTicketStatusUseCase,
            CurrentUserProvider currentUserProvider
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.changeTicketStatusUseCase = changeTicketStatusUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    // UC1: Crear ticket (USER también puede, AGENT/ADMIN también si quieres)
    @PostMapping
    public ResponseEntity<CreateTicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        var actor = currentUserProvider.getCurrentUser();

        var result = createTicketUseCase.execute(new CreateTicketCommand(
                request.title(),
                request.description(),
                new CategoryId(request.categoryId()),
                actor
        ));

        UUID id = result.ticketId().value();
        return ResponseEntity.created(URI.create("/api/tickets/" + id))
                .body(new CreateTicketResponse(id));
    }

    // UC4: Cambiar estado (AGENT/ADMIN está protegido ya por SecurityConfig)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeTicketStatusRequest request) {
        var actor = currentUserProvider.getCurrentUser();

        changeTicketStatusUseCase.execute(new ChangeTicketStatusCommand(
                new TicketId(id),
                request.status(),
                actor
        ));

        return ResponseEntity.noContent().build();
    }
}
