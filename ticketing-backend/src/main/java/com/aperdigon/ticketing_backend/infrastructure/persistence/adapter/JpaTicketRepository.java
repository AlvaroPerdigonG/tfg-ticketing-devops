package com.aperdigon.ticketing_backend.infrastructure.persistence.adapter;

import com.aperdigon.ticketing_backend.application.ports.TicketRepository;
import com.aperdigon.ticketing_backend.domain.ticket.Ticket;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CommentJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper.TicketMapper;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.CategorySpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.TicketSpringDataRepository;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.repository.UserSpringDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        // Cargamos referencias necesarias (createdBy + category + assignedTo) desde DB
        var createdBy = userSpringRepo.getReferenceById(ticket.createdBy().value());
        var category = categorySpringRepo.getReferenceById(ticket.categoryId().value());
        var assignedTo = ticket.assignedTo() == null ? null : userSpringRepo.getReferenceById(ticket.assignedTo().value());

        // Si existe, actualizamos sobre la entidad existente para mantener relaciones y orphanRemoval
        TicketJpaEntity entity = ticketSpringRepo.findById(ticket.id().value())
                .orElseGet(() -> new TicketJpaEntity(
                        ticket.id().value(),
                        ticket.title(),
                        ticket.description(),
                        ticket.status(),
                        ticket.createdAt(),
                        ticket.updatedAt(),
                        createdBy,
                        category,
                        assignedTo
                ));

        entity.setTitle(ticket.title());
        entity.setDescription(ticket.description());
        entity.setStatus(ticket.status());
        entity.setUpdatedAt(ticket.updatedAt());
        entity.setCreatedAt(ticket.createdAt());
        entity.setCreatedBy(createdBy);
        entity.setCategory(category);
        entity.setAssignedTo(assignedTo);

        // Sin UC5 aún no necesitas persistir comments, pero lo dejamos preparado:
        // sincronizamos comments por id (simple: borrar y recrear)
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
        // leer con details para mapear consistente
        return ticketSpringRepo.findWithDetailsById(saved.getId())
                .map(TicketMapper::toDomain)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(TicketId id) {
        return ticketSpringRepo.findWithDetailsById(id.value()).map(TicketMapper::toDomain);
    }
}
