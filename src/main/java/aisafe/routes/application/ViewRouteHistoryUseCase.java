package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ViewRouteHistoryUseCase {

    private final RouteHistoryRepository historyRepository;

    public List<RouteHistory> execute(Long routeId) {
        return historyRepository.findAllByRouteId(routeId);
    }
}