package com.aperdigon.ticketing_backend.application.tickets.detail;

import com.aperdigon.ticketing_backend.application.ports.TicketEventRepository;
import com.aperdigon.ticketing_backend.application.ports.UserRepository;
import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.application.tickets.get.GetTicketUseCase;
import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public final class GetTicketDetailUseCase {

    private final GetTicketUseCase getTicketUseCase;
    private final TicketEventRepository ticketEventRepository;
    private final UserRepository userRepository;

    public GetTicketDetailUseCase(
            GetTicketUseCase getTicketUseCase,
            TicketEventRepository ticketEventRepository,
            UserRepository userRepository
    ) {
        this.getTicketUseCase = Guard.notNull(getTicketUseCase, "getTicketUseCase");
        this.ticketEventRepository = Guard.notNull(ticketEventRepository, "ticketEventRepository");
        this.userRepository = Guard.notNull(userRepository, "userRepository");
    }

    public GetTicketDetailResult execute(TicketId ticketId, CurrentUser actor) {
        Guard.notNull(ticketId, "ticketId");
        Guard.notNull(actor, "actor");

        var ticket = getTicketUseCase.execute(ticketId, actor);
        var events = ticketEventRepository.findByTicketId(ticketId);

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

        return new GetTicketDetailResult(ticket, events, userNamesById);
    }
}
