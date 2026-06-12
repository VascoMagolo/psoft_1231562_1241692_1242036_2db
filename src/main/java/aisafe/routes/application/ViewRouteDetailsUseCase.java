package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case responsible for retrieving the details of a specific route.
 */
@UseCase
@RequiredArgsConstructor
public class ViewRouteDetailsUseCase {

    private final RouteRepository routeRepository;

    /**
     * Finds a route by its origin and destination or throws an exception if not found.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     * @return the requested route
     */
    public Route execute(String origin, String destination) {
        return routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                .orElseThrow(() -> new RouteNotFoundException(origin + "-" + destination));
    }
}
