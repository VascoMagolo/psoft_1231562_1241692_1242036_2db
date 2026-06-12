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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * Use case responsible for deactivating an existing route.
 */
@UseCase
@RequiredArgsConstructor
public class DesactivateRouteUseCase {

    private final RouteRepository routeRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    /**
     * Deactivates a route by its origin and destination and records the action in the history.
     *
     * @param origin        the IATA code of the origin airport
     * @param destination   the IATA code of the destination airport
     * @param clientVersion the version for optimistic locking
     * @return the deactivated route
     */
    public Route execute(String origin, String destination, Long clientVersion) {
        Route route = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                .orElseThrow(() -> new RouteNotFoundException(origin + "-" + destination));

        if (!Objects.equals(route.getVersion(), clientVersion)) {
            throw new ConcurrencyException("Route version mismatch. Please fetch the latest version and retry.");
        }

        String changedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        route.setStatus(RouteStatus.INACTIVE);
        Route deactivatedRoute = routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(origin, destination, "Route deactivated", changedBy));
        return deactivatedRoute;
    }
}
