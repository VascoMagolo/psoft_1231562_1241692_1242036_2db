package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.DuplicateResourceException;
import org.springframework.security.core.context.SecurityContextHolder;

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
        String createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        routeHistoryRepository.save(new RouteHistory(originCode, destinationCode, "Route created", createdBy));
        return route;
    }
}
