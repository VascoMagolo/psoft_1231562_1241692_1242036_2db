package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.RouteRepository;

import java.util.List;

/**
 * Use case for viewing all routes associated with a specific airport
 */
@UseCase
public class ViewAirportRoutesUseCase {
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public ViewAirportRoutesUseCase(AirportRepository airportRepository, RouteRepository routeRepository) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Retrieves a list of all routes that either originate from or are destined for the specified airport.
     * @param iataCode the IATA code of the airport for which to retrieve routes
     * @return a list of routes associated with the specified airport
     */
    public List<RouteResponse> execute(String iataCode) {
        if (!airportRepository.existsByIataCodeCode(iataCode)) {
            throw new AirportNotFoundException(iataCode);
        }
        IataCode code = new IataCode(iataCode);
        return routeRepository.findByOriginOrDestination(code, code).stream()
                .map(r -> new RouteResponse(
                        r.getId(),
                        r.getOrigin().getCode(),
                        r.getDestination().getCode(),
                        r.getEstimatedFlightTime(),
                        r.getMinimumRange(),
                        r.getMinimumCapacity(),
                        r.isActive()
                ))
                .toList();
    }
}
