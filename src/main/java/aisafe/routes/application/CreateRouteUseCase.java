package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;

/**
 * Use case responsible for creating a new route.
 */
@UseCase
@RequiredArgsConstructor
public class CreateRouteUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    /**
     * Validates and persists a new route based on the provided request.
     *
     * @param request the data required to create the route
     * @return the created route
     */
    public Route execute(CreateRouteRequest request) {
        String originCode = request.originIataCode().trim().toUpperCase();
        String destinationCode = request.destinationIataCode().trim().toUpperCase();

        if (!airportRepository.existsByIataCodeCode(originCode)) {
            throw new AirportNotFoundException(originCode);
        }
        if (!airportRepository.existsByIataCodeCode(destinationCode)) {
            throw new AirportNotFoundException(destinationCode);
        }
        if (routeRepository.existsByOriginAndDestination(new IataCode(originCode), new IataCode(destinationCode))) {
            throw new IllegalArgumentException("Route already exists between origin and destination.");
        }

        Route route = new Route(
                originCode,
                destinationCode,
                request.estimatedFlightTime(),
                request.minimumRange(),
                request.minimumCapacity()
        );

        return routeRepository.save(route);
    }
}
