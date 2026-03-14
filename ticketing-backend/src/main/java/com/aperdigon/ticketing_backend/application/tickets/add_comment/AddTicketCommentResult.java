package com.aperdigon.ticketing_backend.application.tickets.add_comment;

import com.aperdigon.ticketing_backend.domain.ticket.CommentId;
import com.aperdigon.ticketing_backend.domain.ticket.TicketId;
import com.aperdigon.ticketing_backend.domain.user.UserId;

import java.time.Instant;

public record AddTicketCommentResult(
        CommentId commentId,
        TicketId ticketId,
        UserId authorUserId,
        String content,
        Instant createdAt
) {
}
