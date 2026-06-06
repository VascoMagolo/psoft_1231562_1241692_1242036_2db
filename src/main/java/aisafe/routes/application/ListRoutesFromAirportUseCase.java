package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.PaginatedResult;
import lombok.RequiredArgsConstructor;

/**
 * Use case responsible for listing all routes originating from a specific airport.
 */
@UseCase
@RequiredArgsConstructor
public class ListRoutesFromAirportUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    /**
     * Retrieves a paginated result of routes associated with the provided airport IATA code.
     *
     * @param iataCode   the IATA code of the origin airport
     * @param pageNumber the zero-based page number
     * @param pageSize   the number of results per page
     * @return a paginated result of routes originating from the specified airport
     */
    public PaginatedResult<Route> execute(String iataCode, int pageNumber, int pageSize) {
        String originCode = iataCode.trim().toUpperCase();

        if (!airportRepository.existsByIataCodeCode(originCode)) {
            throw new AirportNotFoundException(originCode);
        }

        return routeRepository.findByOrigin(new IataCode(originCode), pageNumber, pageSize);
    }
}
