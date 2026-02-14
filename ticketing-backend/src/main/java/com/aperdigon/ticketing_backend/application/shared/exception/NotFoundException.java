package com.aperdigon.ticketing_backend.application.shared.exception;

public final class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
