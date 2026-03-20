package com.aperdigon.ticketing_backend.api.tickets.comments;

import com.aperdigon.ticketing_backend.application.tickets.add_comment.AddTicketCommentResult;

import java.time.Instant;
import java.util.UUID;

public record AddTicketCommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorUserId,
        String content,
        Instant createdAt
) {
    public static AddTicketCommentResponse from(AddTicketCommentResult result) {
        return new AddTicketCommentResponse(
                result.commentId().value(),
                result.ticketId().value(),
                result.authorUserId().value(),
                result.content(),
                result.createdAt()
        );
    }
}
