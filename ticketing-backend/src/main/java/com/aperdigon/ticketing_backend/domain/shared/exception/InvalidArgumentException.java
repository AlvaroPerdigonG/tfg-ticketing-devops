package com.aperdigon.ticketing_backend.domain.shared.exception;

public final class InvalidArgumentException extends DomainException {
    public InvalidArgumentException(String message) {
        super(message);
    }
}
