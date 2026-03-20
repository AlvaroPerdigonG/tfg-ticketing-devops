package com.aperdigon.ticketing_backend.api.tickets.detail;

import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketPriority;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEvent;
import com.aperdigon.ticketing_backend.domain.ticket_event.TicketEventType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record TicketDetailResponse(
        UUID id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Instant createdAt,
        Instant updatedAt,
        UUID createdByUserId,
        String createdByDisplayName,
        UUID assignedToUserId,
        String assignedToDisplayName,
        UUID categoryId,
        List<TimelineEntryResponse> timeline,
        List<TicketStatus> availableTransitions
) {
    public static TicketDetailResponse from(Ticket ticket, List<TicketEvent> events, Function<UUID, String> userDisplayNameResolver) {
        List<TimelineEntryResponse> entries = new ArrayList<>();

        entries.add(new TimelineEntryResponse(
                ticket.id().value(),
                "MESSAGE",
                ticket.createdAt(),
                ticket.createdBy().value(),
                userDisplayNameResolver.apply(ticket.createdBy().value()),
                ticket.description(),
                null,
                Map.of()
        ));

        for (var c : ticket.comments()) {
            entries.add(new TimelineEntryResponse(
                    c.id().value(),
                    "MESSAGE",
                    c.createdAt(),
                    c.authorId().value(),
                    userDisplayNameResolver.apply(c.authorId().value()),
                    c.content(),
                    null,
                    Map.of()
            ));
        }

        for (var event : events) {
            if (event.type() == TicketEventType.COMMENT_ADDED) {
                continue;
            }
            entries.add(new TimelineEntryResponse(
                    event.id(),
                    "EVENT",
                    event.createdAt(),
                    event.actorUserId() == null ? null : event.actorUserId().value(),
                    event.actorUserId() == null ? null : userDisplayNameResolver.apply(event.actorUserId().value()),
                    null,
                    event.type(),
                    event.payload()
            ));
        }

        entries.sort(
                Comparator.comparing(TimelineEntryResponse::createdAt)
                        .thenComparing(TimelineEntryResponse::kind)
                        .thenComparing(entry -> entry.eventType() == TicketEventType.TICKET_CREATED ? 0 : 1)
        );

        return new TicketDetailResponse(
                ticket.id().value(),
                ticket.title(),
                ticket.description(),
                ticket.status(),
                ticket.priority(),
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.createdBy().value(),
                userDisplayNameResolver.apply(ticket.createdBy().value()),
                ticket.assignedTo() == null ? null : ticket.assignedTo().value(),
                ticket.assignedTo() == null ? null : userDisplayNameResolver.apply(ticket.assignedTo().value()),
                ticket.categoryId().value(),
                entries,
                availableTransitions(ticket.status())
        );
    }

    private static List<TicketStatus> availableTransitions(TicketStatus status) {
        return switch (status) {
            case OPEN -> List.of(TicketStatus.IN_PROGRESS);
            case IN_PROGRESS -> List.of(TicketStatus.ON_HOLD, TicketStatus.RESOLVED);
            case ON_HOLD -> List.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
            case RESOLVED -> List.of();
        };
    }

    public record TimelineEntryResponse(
            UUID id,
            String kind,
            Instant createdAt,
            UUID actorUserId,
            String actorDisplayName,
            String content,
            TicketEventType eventType,
            Map<String, String> payload
    ) {
    }
}
