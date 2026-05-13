package com.aperdigon.ticketing_backend.test_support;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.shared.pagination.PageDirection;
import com.aperdigon.ticketing_backend.application.shared.pagination.PageQuery;
import com.aperdigon.ticketing_backend.application.shared.pagination.PagedResult;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.AgentTicketCount;
import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InMemoryTicketRepository implements TicketRepository {
    private final Map<TicketId, Ticket> store = new HashMap<>();

    @Override
    public Ticket save(Ticket ticket) {
        store.put(ticket.id(), ticket);
        return ticket;
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public PagedResult<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, PageQuery pageQuery) {
        List<Ticket> filtered = sortedTickets()
                .filter(ticket -> ticket.createdBy().equals(createdBy))
                .filter(ticket -> status == null || ticket.status() == status)
                .filter(ticket -> matchesQuery(ticket, q))
                .toList();

        return toPage(filtered, pageQuery);
    }

    @Override
    public PagedResult<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, PageQuery pageQuery) {
        List<Ticket> filtered = sortedTickets()
                .filter(ticket -> matchesScope(ticket, actorId, scope))
                .filter(ticket -> status == null || ticket.status() == status)
                .filter(ticket -> matchesQuery(ticket, q))
                .toList();

        return toPage(filtered, pageQuery);
    }

    @Override
    public long countUnassigned() {
        return store.values().stream()
                .filter(ticket -> ticket.assignedTo() == null)
                .count();
    }

    @Override
    public long countAssignedTo(UserId assigneeId) {
        return store.values().stream()
                .filter(ticket -> assigneeId.equals(ticket.assignedTo()))
                .count();
    }

    @Override
    public long countByStatus(TicketStatus status) {
        return store.values().stream()
                .filter(ticket -> ticket.status() == status)
                .count();
    }

    @Override
    public List<AgentTicketCount> countAssignedByAssigneeRoles(Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return store.values().stream()
                .filter(ticket -> ticket.assignedTo() != null)
                .collect(Collectors.groupingBy(Ticket::assignedTo, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new AgentTicketCount(entry.getKey(), entry.getKey().value().toString(), entry.getValue()))
                .sorted(Comparator.comparingLong(AgentTicketCount::count).reversed())
                .toList();
    }

    @Override
    public List<AgentTicketCount> countByStatusGroupedByAssigneeRoles(TicketStatus status, Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return store.values().stream()
                .filter(ticket -> ticket.assignedTo() != null)
                .filter(ticket -> ticket.status() == status)
                .collect(Collectors.groupingBy(Ticket::assignedTo, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new AgentTicketCount(entry.getKey(), entry.getKey().value().toString(), entry.getValue()))
                .sorted(Comparator.comparingLong(AgentTicketCount::count).reversed())
                .toList();
    }

    private Stream<Ticket> sortedTickets() {
        return store.values().stream()
                .sorted(Comparator.comparing(Ticket::createdAt).thenComparing(ticket -> ticket.id().value()));
    }

    private boolean matchesScope(Ticket ticket, UserId actorId, TicketQueueScope scope) {
        TicketQueueScope effectiveScope = scope == null ? TicketQueueScope.ALL : scope;
        return switch (effectiveScope) {
            case UNASSIGNED -> ticket.assignedTo() == null;
            case MINE -> actorId.equals(ticket.assignedTo());
            case OTHERS -> ticket.assignedTo() != null && !actorId.equals(ticket.assignedTo());
            case ALL -> true;
        };
    }

    private boolean matchesQuery(Ticket ticket, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }

        String normalizedQuery = q.trim().toLowerCase();
        return ticket.title().toLowerCase().contains(normalizedQuery)
                || ticket.description().toLowerCase().contains(normalizedQuery);
    }

    private PagedResult<Ticket> toPage(List<Ticket> filtered, PageQuery pageQuery) {
        PageQuery effectivePageQuery = pageQuery == null
                ? PageQuery.of(0, Math.max(filtered.size(), 1), "createdAt", PageDirection.ASC)
                : pageQuery;

        List<Ticket> sorted = sort(filtered, effectivePageQuery);
        int start = effectivePageQuery.page() * effectivePageQuery.size();
        if (start >= sorted.size()) {
            return new PagedResult<>(List.of(), effectivePageQuery.page(), effectivePageQuery.size(), sorted.size());
        }

        int end = Math.min(start + effectivePageQuery.size(), sorted.size());
        return new PagedResult<>(sorted.subList(start, end), effectivePageQuery.page(), effectivePageQuery.size(), sorted.size());
    }

    private List<Ticket> sort(List<Ticket> tickets, PageQuery pageQuery) {
        Comparator<Ticket> comparator = switch (pageQuery.sortBy()) {
            case "updatedAt" -> Comparator.comparing(Ticket::updatedAt);
            case "createdAt" -> Comparator.comparing(Ticket::createdAt);
            case "title" -> Comparator.comparing(Ticket::title, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Ticket::createdAt);
        };

        comparator = comparator.thenComparing(ticket -> ticket.id().value());
        if (pageQuery.direction() == PageDirection.DESC) {
            comparator = comparator.reversed();
        }
        return tickets.stream().sorted(comparator).toList();
    }
}
