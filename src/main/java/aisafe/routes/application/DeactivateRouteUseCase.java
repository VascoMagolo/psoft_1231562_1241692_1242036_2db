package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteStatus;
import aisafe.shared.domain.ConcurrencyException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@UseCase
public class DeactivateRouteUseCase {

    private final RouteRepository routeRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    public DeactivateRouteUseCase(RouteRepository routeRepository, RouteHistoryRepository routeHistoryRepository) {
        this.routeRepository = routeRepository;
        this.routeHistoryRepository = routeHistoryRepository;
    }

    public Route execute(String origin, String destination, Long clientVersion) {
        Route route = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                .orElseThrow(() -> new RouteNotFoundException(origin + "-" + destination));

        if (clientVersion != null) {
            Long currentVersion = routeRepository.findVersionFor(new IataCode(origin), new IataCode(destination));
            if (!Objects.equals(currentVersion, clientVersion)) {
                throw new ConcurrencyException("Route version mismatch. Please fetch the latest version and retry.");
            }
        }

        String changedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        route.setStatus(RouteStatus.INACTIVE);
        routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(origin, destination, "Route deactivated", changedBy));
        return route;
    }
}
