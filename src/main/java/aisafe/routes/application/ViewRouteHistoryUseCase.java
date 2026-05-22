package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ViewRouteHistoryUseCase {

    private final RouteHistoryRepository historyRepository;
    private final RouteRepository routeRepository;

    public List<RouteHistory> execute(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new RouteNotFoundException(routeId.toString());
        }

        return historyRepository.findAllByRouteId(routeId);
    }
}
