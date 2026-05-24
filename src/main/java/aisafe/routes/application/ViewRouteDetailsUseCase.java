package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Use case responsible for retrieving the details of a specific route.
 */
@UseCase
@RequiredArgsConstructor
public class ViewRouteDetailsUseCase {

    private final RouteRepository routeRepository;

    /**
     * Finds a route by its ID or throws an exception if not found.
     *
     * @param id the unique identifier of the route
     * @return the requested route
     */
    public Route execute(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));
    }
}