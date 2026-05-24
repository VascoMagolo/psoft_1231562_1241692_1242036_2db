package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.routes.application.dtos.UpdateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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
     * @param id      the unique identifier of the route
     * @param request the data to update
     * @return the updated route
     */
    @Transactional
    public Route execute(Long id, UpdateRouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));

        route.updateRoute(
                request.estimatedFlightTime(),
                request.minimumRange(),
                request.minimumCapacity()
        );

        if (request.active() != null) {
            route.setActive(request.active());
        }

        Route updatedRoute = routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(updatedRoute, "Route details updated", "system"));
        return updatedRoute;
    }
}
