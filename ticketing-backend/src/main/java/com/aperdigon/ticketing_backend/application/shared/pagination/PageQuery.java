package com.aperdigon.ticketing_backend.application.shared.pagination;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

public record PageQuery(
        int page,
        int size,
        String sortBy,
        PageDirection direction
) {
    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        sortBy = Guard.notBlank(sortBy, "sortBy");
        direction = Guard.notNull(direction, "direction");
    }

    public static PageQuery of(int page, int size, String sortBy, PageDirection direction) {
        return new PageQuery(page, size, sortBy, direction);
    }
}
