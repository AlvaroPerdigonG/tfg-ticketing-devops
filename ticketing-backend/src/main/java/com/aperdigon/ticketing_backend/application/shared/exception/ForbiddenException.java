package com.aperdigon.ticketing_backend.application.shared.exception;

public final class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
