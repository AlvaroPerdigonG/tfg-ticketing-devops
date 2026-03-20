package com.aperdigon.ticketing_backend.api.tickets.comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddTicketCommentRequest(
        @NotBlank @Size(max = 2000) String content
) {
}
