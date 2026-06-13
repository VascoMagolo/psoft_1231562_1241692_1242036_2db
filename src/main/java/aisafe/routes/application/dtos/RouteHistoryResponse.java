package aisafe.routes.application.dtos;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a historical entry of changes made to a route.
 */
public record RouteHistoryResponse(
        String originCode,
        String destinationCode,
        String changeDescription,
        LocalDateTime changedAt,
        String changedBy
) {}
