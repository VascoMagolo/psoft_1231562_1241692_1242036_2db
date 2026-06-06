package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import aisafe.shared.domain.ConcurrencyException;
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
     * Deactivates a route by its ID and records the action in the history.
     *
     * @param id the unique identifier of the route to be deactivated
     * @return the deactivated route
     */
    public Route execute(Long id, Long clientVersion) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));

        if (!Objects.equals(route.getVersion(), clientVersion)) {
            throw new ConcurrencyException("Route version mismatch. Please fetch the latest version and retry.");
        }

        String changedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        route.deactivate();
        Route deactivatedRoute = routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(deactivatedRoute.getId(), "Route deactivated", changedBy));
        return deactivatedRoute;
    }
}
