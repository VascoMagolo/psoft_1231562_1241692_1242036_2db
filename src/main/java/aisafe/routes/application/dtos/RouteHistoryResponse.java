package aisafe.routes.dtos;

import java.time.LocalDateTime;

public record RouteHistoryResponse(
        Long id,
        Long routeId,
        String changeDescription,
        LocalDateTime changedAt,
        String changedBy
) {}