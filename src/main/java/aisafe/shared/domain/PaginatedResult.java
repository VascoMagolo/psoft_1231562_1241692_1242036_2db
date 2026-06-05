package aisafe.shared.domain;

import java.util.List;

public record PaginatedResult<T>(
        List<T> data,
        long totalElements
) {}