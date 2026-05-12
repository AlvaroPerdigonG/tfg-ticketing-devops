package com.aperdigon.ticketing_backend.application.shared.pagination;

import com.aperdigon.ticketing_backend.domain.shared.validation.Guard;

import java.util.List;
import java.util.function.Function;

public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
    public PagedResult {
        content = List.copyOf(Guard.notNull(content, "content"));
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must be greater than or equal to 0");
        }
    }

    public <R> PagedResult<R> map(Function<T, R> mapper) {
        Guard.notNull(mapper, "mapper");
        return new PagedResult<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
