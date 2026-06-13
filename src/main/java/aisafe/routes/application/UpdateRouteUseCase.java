package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.UpdateRouteRequest;
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
 * Use case responsible for updating the details of an existing route.
 */
@UseCase
@RequiredArgsConstructor
public class UpdateRouteUseCase {

    private final RouteRepository routeRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    /**
     * Updates the route details and records the change in the history.
     *
     * @param origin        the IATA code of the origin airport
     * @param destination   the IATA code of the destination airport
     * @param request       the data to update
     * @param clientVersion the version for optimistic locking
     * @return the updated route
     */
    public Route execute(String origin, String destination, UpdateRouteRequest request, Long clientVersion) {
        Route route = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                .orElseThrow(() -> new RouteNotFoundException(origin + "-" + destination));

        if (!Objects.equals(route.getVersion(), clientVersion)) {
            throw new ConcurrencyException("Route version mismatch. Please fetch the latest version and retry.");
        }

        route.updateRoute(
                request.estimatedFlightTime(),
                request.minimumRange(),
                request.minimumCapacity()
        );

        if (request.active() != null) {
            route.setStatus(request.active() ? RouteStatus.ACTIVE : RouteStatus.INACTIVE);
        }

        String changedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        Route updatedRoute = routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(origin, destination, "Route details updated", changedBy));
        return updatedRoute;
    }
}
