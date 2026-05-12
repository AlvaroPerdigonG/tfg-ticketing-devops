package com.aperdigon.ticketing_backend.api.shared.pagination;

import com.aperdigon.ticketing_backend.application.shared.pagination.PagedResult;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long total
) {
    public static <T> PageResponse<T> from(PagedResult<T> page) {
        return new PageResponse<>(
                page.content(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }
}
