package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;

@UseCase
public class DeleteRouteUseCase {
    private final RouteRepository routeRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    public DeleteRouteUseCase(RouteRepository routeRepository, RouteHistoryRepository routeHistoryRepository) {
        this.routeRepository = routeRepository;
        this.routeHistoryRepository = routeHistoryRepository;
    }

    public void execute(String origin, String destination) {
        Route route = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                .orElseThrow(() -> new RouteNotFoundException(origin + "-" + destination));
        routeHistoryRepository.deleteAllByRoute(origin, destination);
        routeRepository.delete(route);
    }
}
