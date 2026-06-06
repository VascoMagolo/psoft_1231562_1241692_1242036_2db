package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.PaginatedResult;
import lombok.RequiredArgsConstructor;

/**
 * Use case responsible for searching routes based on origin and destination criteria.
 */
@UseCase
@RequiredArgsConstructor
public class SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    /**
     * Retrieves a paginated result of routes matching the provided origin and destination.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     * @param pageNumber  the zero-based page number
     * @param pageSize    the number of results per page
     * @return a paginated result of routes matching the search criteria
     */
    public PaginatedResult<Route> execute(String origin, String destination, int pageNumber, int pageSize) {
        if (origin != null && destination != null) {
            return routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination), pageNumber, pageSize);
        } else if (origin != null) {
            return routeRepository.findByOrigin(new IataCode(origin), pageNumber, pageSize);
        } else if (destination != null) {
            return routeRepository.findByDestination(new IataCode(destination), pageNumber, pageSize);
        }
        return routeRepository.findAll(pageNumber, pageSize);
    }
}
