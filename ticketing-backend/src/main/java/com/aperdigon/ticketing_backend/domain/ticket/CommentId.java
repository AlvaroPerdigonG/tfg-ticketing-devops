package com.aperdigon.ticketing_backend.domain.ticket;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

import java.util.UUID;

public record CommentId(UUID value) {
    public CommentId {
        Guard.notNull(value, "commentId");
    }

    public static CommentId of(UUID value) {
        return new CommentId(value);
    }
}
