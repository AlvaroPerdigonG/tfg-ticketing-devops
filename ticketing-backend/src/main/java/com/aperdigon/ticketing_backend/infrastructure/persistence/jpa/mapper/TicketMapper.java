package com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.mapper;


import com.aperdigon.ticketing_backend.domain.category.Category;
import com.aperdigon.ticketing_backend.domain.category.CategoryId;
import com.aperdigon.ticketing_backend.domain.ticket.*;
import com.aperdigon.ticketing_backend.domain.user.User;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.CommentJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.TicketJpaEntity;
import com.aperdigon.ticketing_backend.infrastructure.persistence.jpa.entity.UserJpaEntity;

import java.util.List;
import java.util.UUID;

public final class TicketMapper {
    private TicketMapper() {}

    // JPA -> Domain
    public static Ticket toDomain(TicketJpaEntity e) {
        var createdBy = UserMapper.toDomain(e.getCreatedBy());
        var category = CategoryMapper.toDomain(e.getCategory());

        var assignedToId = e.getAssignedTo() == null
                ? null
                : new UserId(e.getAssignedTo().getId());

        List<Comment> comments = e.getComments().stream()
                .map(TicketMapper::toDomainComment)
                .toList();

        return Ticket.rehydrate(
                new TicketId(e.getId()),
                e.getTitle(),
                e.getDescription(),
                category.id(),
                createdBy.id(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                assignedToId,
                comments
        );
    }


    private static Comment toDomainComment(CommentJpaEntity c) {
        return new Comment(
                new CommentId(c.getId()),
                c.getContent(),
                new UserId(c.getAuthor().getId()),
                c.getCreatedAt()
        );
    }

    public static UUID uuid(TicketId id) { return id.value(); }
}
