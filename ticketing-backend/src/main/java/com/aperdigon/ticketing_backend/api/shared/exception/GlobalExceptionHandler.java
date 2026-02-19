package com.aperdigon.ticketing_backend.api.shared.exception;

import com.aperdigon.ticketing_backend.application.shared.exception.ForbiddenException;
import com.aperdigon.ticketing_backend.application.shared.exception.NotFoundException;
import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketAlreadyResolved;
import com.aperdigon.ticketing_backend.domain.ticket.exceptions.TicketInvalidStatusTransition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ApiError> badRequest(InvalidArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler({TicketInvalidStatusTransition.class, TicketAlreadyResolved.class})
    public ResponseEntity<ApiError> conflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("CONFLICT", ex.getMessage()));
    }
}
