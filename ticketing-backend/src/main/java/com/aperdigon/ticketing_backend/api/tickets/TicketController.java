package com.aperdigon.ticketing_backend.api.tickets;

import com.aperdigon.ticketing_backend.api.shared.currentuser.CurrentUserProvider;
import com.aperdigon.ticketing_backend.api.shared.pagination.PageResponse;
import com.aperdigon.ticketing_backend.api.tickets.change_status.ChangeTicketStatusRequest;
import com.aperdigon.ticketing_backend.api.tickets.comments.AddTicketCommentRequest;
import com.aperdigon.ticketing_backend.api.tickets.comments.AddTicketCommentResponse;
import com.aperdigon.ticketing_backend.api.tickets.create.CreateTicketRequest;
import com.aperdigon.ticketing_backend.api.tickets.create.CreateTicketResponse;
import com.aperdigon.ticketing_backend.api.tickets.detail.TicketDetailResponse;
import com.aperdigon.ticketing_backend.api.tickets.list.TicketSummaryResponse;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusCommand;
import com.aperdigon.ticketing_backend.application.tickets.change_status.ChangeTicketStatusUseCase;
import com.aperdigon.ticketing_backend.application.tickets.add_comment.AddTicketCommentCommand;
import com.aperdigon.ticketing_backend.application.tickets.add_comment.AddTicketCommentUseCase;
import com.aperdigon.ticketing_backend.application.tickets.assign.AssignTicketToMeCommand;
import com.aperdigon.ticketing_backend.application.tickets.assign.AssignTicketToMeUseCase;
import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.application.ports.UserRepository;
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
import com.aperdigon.ticketing_backend.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Ticket management endpoints")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final ChangeTicketStatusUseCase changeTicketStatusUseCase;
    private final AssignTicketToMeUseCase assignTicketToMeUseCase;
    private final ListMyTicketsUseCase listMyTicketsUseCase;
    private final ListTicketsUseCase listTicketsUseCase;
    private final GetTicketUseCase getTicketUseCase;
    private final AddTicketCommentUseCase addTicketCommentUseCase;
    private final TicketEventRepository ticketEventRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public TicketController(
            CreateTicketUseCase createTicketUseCase,
            ChangeTicketStatusUseCase changeTicketStatusUseCase,
            AssignTicketToMeUseCase assignTicketToMeUseCase,
            ListMyTicketsUseCase listMyTicketsUseCase,
            ListTicketsUseCase listTicketsUseCase,
            GetTicketUseCase getTicketUseCase,
            AddTicketCommentUseCase addTicketCommentUseCase,
            TicketEventRepository ticketEventRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.changeTicketStatusUseCase = changeTicketStatusUseCase;
        this.assignTicketToMeUseCase = assignTicketToMeUseCase;
        this.listMyTicketsUseCase = listMyTicketsUseCase;
        this.listTicketsUseCase = listTicketsUseCase;
        this.getTicketUseCase = getTicketUseCase;
        this.addTicketCommentUseCase = addTicketCommentUseCase;
        this.ticketEventRepository = ticketEventRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @Operation(summary = "Create a new ticket")
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
    @Operation(summary = "List tickets created by the authenticated user")
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
    @Operation(summary = "List operational ticket queue for agents/admins")
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
    @Operation(summary = "Get full ticket detail by id")
    public TicketDetailResponse getById(@PathVariable UUID id) {
        var actor = currentUserProvider.getCurrentUser();
        var ticket = getTicketUseCase.execute(new TicketId(id), actor);
        var events = ticketEventRepository.findByTicketId(new TicketId(id));

        Set<UUID> userIds = new HashSet<>();
        userIds.add(ticket.createdBy().value());
        if (ticket.assignedTo() != null) {
            userIds.add(ticket.assignedTo().value());
        }
        for (var comment : ticket.comments()) {
            userIds.add(comment.authorId().value());
        }
        for (var event : events) {
            if (event.actorUserId() != null) {
                userIds.add(event.actorUserId().value());
            }
        }

        Map<UUID, String> userNamesById = new HashMap<>();
        for (var userId : userIds) {
            userRepository.findById(new UserId(userId))
                    .ifPresent(user -> userNamesById.put(userId, user.displayName()));
        }

        return TicketDetailResponse.from(ticket, events, userId -> userNamesById.getOrDefault(userId, userId.toString()));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to a ticket")
    public ResponseEntity<AddTicketCommentResponse> addComment(@PathVariable UUID id, @Valid @RequestBody AddTicketCommentRequest request) {
        var actor = currentUserProvider.getCurrentUser();
        var result = addTicketCommentUseCase.execute(new AddTicketCommentCommand(new TicketId(id), request.content(), actor));
        return ResponseEntity.created(URI.create("/api/tickets/" + id + "/comments/" + result.commentId().value()))
                .body(AddTicketCommentResponse.from(result));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change ticket status")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeTicketStatusRequest request) {
        var actor = currentUserProvider.getCurrentUser();

        changeTicketStatusUseCase.execute(new ChangeTicketStatusCommand(
                new TicketId(id),
                request.status(),
                actor
        ));

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assignment/me")
    @Operation(summary = "Assign ticket to the authenticated agent/admin")
    public ResponseEntity<Void> assignToMe(@PathVariable UUID id) {
        var actor = currentUserProvider.getCurrentUser();
        assignTicketToMeUseCase.execute(new AssignTicketToMeCommand(new TicketId(id), actor));
        return ResponseEntity.noContent().build();
    }
}
