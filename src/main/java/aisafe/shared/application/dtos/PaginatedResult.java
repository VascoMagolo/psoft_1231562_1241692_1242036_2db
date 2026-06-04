package aisafe.shared.application.dtos;

import java.util.List;

public record PaginatedResult<T>(
        List<T> data,
        long totalElements
) {}