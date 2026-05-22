package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class DesactivateRouteUseCase {

    private final RouteRepository routeRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    @Transactional
    public Route execute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));

        route.deactivate();
        Route deactivatedRoute = routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(deactivatedRoute, "Route deactivated", "system"));
        return deactivatedRoute;
    }
}
