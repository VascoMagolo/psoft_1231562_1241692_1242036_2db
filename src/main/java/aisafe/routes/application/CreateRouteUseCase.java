package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.DuplicateResourceException;

/**
 * Use case responsible for creating a new route.
 */
@UseCase
public class CreateRouteUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    public CreateRouteUseCase(RouteRepository routeRepository, AirportRepository airportRepository,
                              RouteHistoryRepository routeHistoryRepository) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
        this.routeHistoryRepository = routeHistoryRepository;
    }

    /**
     * Validates and persists a new route based on the provided request.
     *
     * @param request the data required to create the route
     * @return the created route response
     */
    public RouteResponse execute(CreateRouteRequest request) {
        String originCode = request.originIataCode().trim().toUpperCase();
        String destinationCode = request.destinationIataCode().trim().toUpperCase();

        aisafe.airports.domain.Airport originAirport = airportRepository.findByIataCode(new IataCode(originCode))
                .orElseThrow(() -> new AirportNotFoundException(originCode));
        if (originAirport.getStatus() != aisafe.airports.domain.AirportStatus.OPERATIONAL) {
            throw new aisafe.routes.domain.InvalidRouteException("Origin airport is not operational: " + originCode);
        }

        aisafe.airports.domain.Airport destinationAirport = airportRepository.findByIataCode(new IataCode(destinationCode))
                .orElseThrow(() -> new AirportNotFoundException(destinationCode));
        if (destinationAirport.getStatus() != aisafe.airports.domain.AirportStatus.OPERATIONAL) {
            throw new aisafe.routes.domain.InvalidRouteException("Destination airport is not operational: " + destinationCode);
        }
        if (routeRepository.existsByOriginAndDestination(new IataCode(originCode), new IataCode(destinationCode))) {
            throw new DuplicateResourceException("Route already exists between origin and destination.");
        }

        Route route = new Route(
                originCode,
                destinationCode,
                request.estimatedFlightTime(),
                request.minimumRange(),
                request.minimumCapacity()
        );

        routeRepository.save(route);
        routeHistoryRepository.save(new RouteHistory(originCode, destinationCode, "Route created", request.createdBy()));
        return RouteResponse.from(route);
    }
}
