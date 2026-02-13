package com.aperdigon.ticketing_backend.domain.shared.validation;

import com.aperdigon.ticketing_backend.domain.shared.exception.InvalidArgumentException;

public final class Guard {
    private Guard() {}

    public static String notBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static <T> T notNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidArgumentException(fieldName + " must not be null");
        }
        return value;
    }
}
