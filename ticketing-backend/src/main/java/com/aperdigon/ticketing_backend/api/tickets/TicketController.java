package com.aperdigon.ticketing_backend.api.tickets;

import com.aperdigon.ticketing_backend.api.shared.currentuser.CurrentUserProvider;
import com.aperdigon.ticketing_backend.api.shared.pagination.PageResponse;
import com.aperdigon.ticketing_backend.api.tickets.change_status.ChangeTicketStatusRequest;
import com.aperdigon.ticketing_backend.api.tickets.create.CreateTicketRequest;
import com.aperdigon.ticketing_backend.api.tickets.create.CreateTicketResponse;
import com.aperdigon.ticketing_backend.api.tickets.detail.TicketDetailResponse;
import com.aperdigon.ticketing_backend.api.tickets.list.TicketSummaryResponse;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusCommand;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusUseCase;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketCommand;
import com.aperdigon.ticketing_backend.application.tickets.create.CreateTicketUseCase;
import com.aperdigon.ticketing_backend.application.tickets.get.GetTicketUseCase;
import com.aperdigon.ticketing_backend.application.tickets.list.ListMyTicketsQuery;
import com.aperdigon.ticketing_backend.application.tickets.list.ListMyTicketsUseCase;
import com.aperdigon.ticketing_backend.application.tickets.list.ListTicketsQuery;
import com.aperdigon.ticketing_backend.application.tickets.list.ListTicketsUseCase;
import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final ChangeTicketStatusUseCase changeTicketStatusUseCase;
    private final ListMyTicketsUseCase listMyTicketsUseCase;
    private final ListTicketsUseCase listTicketsUseCase;
    private final GetTicketUseCase getTicketUseCase;
    private final CurrentUserProvider currentUserProvider;

    public TicketController(
            CreateTicketUseCase createTicketUseCase,
            ChangeTicketStatusUseCase changeTicketStatusUseCase,
            ListMyTicketsUseCase listMyTicketsUseCase,
            ListTicketsUseCase listTicketsUseCase,
            GetTicketUseCase getTicketUseCase,
            CurrentUserProvider currentUserProvider
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.changeTicketStatusUseCase = changeTicketStatusUseCase;
        this.listMyTicketsUseCase = listMyTicketsUseCase;
        this.listTicketsUseCase = listTicketsUseCase;
        this.getTicketUseCase = getTicketUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<CreateTicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        var actor = currentUserProvider.getCurrentUser();

        var result = createTicketUseCase.execute(new CreateTicketCommand(
                request.title(),
                request.description(),
                new CategoryId(request.categoryId()),
                request.priority(),
                actor
        ));

        UUID id = result.ticketId().value();
        return ResponseEntity.created(URI.create("/api/tickets/" + id))
                .body(new CreateTicketResponse(id));
    }

    @GetMapping("/me")
    public PageResponse<TicketSummaryResponse> listMine(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var actor = currentUserProvider.getCurrentUser();
        var pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "updatedAt"));

        var result = listMyTicketsUseCase.execute(new ListMyTicketsQuery(actor, status, q, pageable));
        return PageResponse.from(result.map(TicketSummaryResponse::from));
    }

    @GetMapping
    public PageResponse<TicketSummaryResponse> listQueue(
            @RequestParam(defaultValue = "MINE") TicketQueueScope scope,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var actor = currentUserProvider.getCurrentUser();
        var pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "updatedAt"));

        var result = listTicketsUseCase.execute(new ListTicketsQuery(actor, scope, status, q, pageable));
        return PageResponse.from(result.map(TicketSummaryResponse::from));
    }

    @GetMapping("/{id}")
    public TicketDetailResponse getById(@PathVariable UUID id) {
        var actor = currentUserProvider.getCurrentUser();
        var ticket = getTicketUseCase.execute(new TicketId(id), actor);
        return TicketDetailResponse.from(ticket);
    }

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
