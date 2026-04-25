package com.aperdigon.ticketing_backend.infrastructure.persistence.adapter;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.application.tickets.dashboard.AgentTicketCount;
import com.aperdigon.ticketing_backend.application.tickets.list.TicketQueueScope;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketStatus;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CommentJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper.TicketMapper;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketSpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class JpaTicketRepository implements TicketRepository {

    private final TicketSpringDataRepository ticketSpringRepo;
    private final UserSpringDataRepository userSpringRepo;
    private final CategorySpringDataRepository categorySpringRepo;

    public JpaTicketRepository(
            TicketSpringDataRepository ticketSpringRepo,
            UserSpringDataRepository userSpringRepo,
            CategorySpringDataRepository categorySpringRepo
    ) {
        this.ticketSpringRepo = ticketSpringRepo;
        this.userSpringRepo = userSpringRepo;
        this.categorySpringRepo = categorySpringRepo;
    }

    @Override
    @Transactional
    public Ticket save(Ticket ticket) {
        var createdBy = userSpringRepo.getReferenceById(ticket.createdBy().value());
        var category = categorySpringRepo.getReferenceById(ticket.categoryId().value());
        var assignedTo = ticket.assignedTo() == null ? null : userSpringRepo.getReferenceById(ticket.assignedTo().value());

        TicketJpaEntity entity = ticketSpringRepo.findById(ticket.id().value())
                .orElseGet(() -> new TicketJpaEntity(
                        ticket.id().value(),
                        ticket.title(),
                        ticket.description(),
                        ticket.status(),
                        ticket.priority(),
                        ticket.createdAt(),
                        ticket.updatedAt(),
                        createdBy,
                        category,
                        assignedTo
                ));

        entity.setTitle(ticket.title());
        entity.setDescription(ticket.description());
        entity.setStatus(ticket.status());
        entity.setPriority(ticket.priority());
        entity.setUpdatedAt(ticket.updatedAt());
        entity.setCreatedAt(ticket.createdAt());
        entity.setCreatedBy(createdBy);
        entity.setCategory(category);
        entity.setAssignedTo(assignedTo);

        entity.getComments().clear();
        for (var c : ticket.comments()) {
            var author = userSpringRepo.getReferenceById(c.authorId().value());
            entity.getComments().add(new CommentJpaEntity(
                    c.id().value(),
                    entity,
                    author,
                    c.content(),
                    c.createdAt()
            ));
        }

        TicketJpaEntity saved = ticketSpringRepo.save(entity);
        return ticketSpringRepo.findWithDetailsById(saved.getId())
                .map(TicketMapper::toDomain)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(TicketId id) {
        return ticketSpringRepo.findWithDetailsById(id.value()).map(TicketMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findMyTickets(UserId createdBy, TicketStatus status, String q, Pageable pageable) {
        Specification<TicketJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("createdBy").get("id"), createdBy.value()));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (q != null && !q.isBlank()) {
                String normalized = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), normalized));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketSpringRepo.findAll(spec, pageable).map(TicketMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> findAgentTickets(UserId actorId, TicketQueueScope scope, TicketStatus status, String q, Pageable pageable) {
        Specification<TicketJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (scope == TicketQueueScope.UNASSIGNED) {
                predicates.add(cb.isNull(root.get("assignedTo")));
            } else if (scope == TicketQueueScope.MINE) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), actorId.value()));
            } else if (scope == TicketQueueScope.OTHERS) {
                predicates.add(cb.isNotNull(root.get("assignedTo")));
                predicates.add(cb.notEqual(root.get("assignedTo").get("id"), actorId.value()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (q != null && !q.isBlank()) {
                String normalized = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), normalized));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketSpringRepo.findAll(spec, pageable).map(TicketMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnassigned() {
        return ticketSpringRepo.countByAssignedToIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignedTo(UserId assigneeId) {
        return ticketSpringRepo.countByAssignedTo_Id(assigneeId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(TicketStatus status) {
        return ticketSpringRepo.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentTicketCount> countAssignedByAssigneeRoles(Set<UserRole> roles) {
        return ticketSpringRepo.countAssignedByAssigneeRoles(roles).stream()
                .map(row -> new AgentTicketCount(new UserId(row.getAssigneeId()), row.getAssigneeDisplayName(), row.getTicketCount()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentTicketCount> countByStatusGroupedByAssigneeRoles(TicketStatus status, Set<UserRole> roles) {
        return ticketSpringRepo.countByStatusGroupedByAssigneeRoles(status, roles).stream()
                .map(row -> new AgentTicketCount(new UserId(row.getAssigneeId()), row.getAssigneeDisplayName(), row.getTicketCount()))
                .toList();
    }
}
