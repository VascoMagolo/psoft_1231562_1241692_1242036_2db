package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.PaginatedResult;
import java.util.List;

/**
 * Use case responsible for searching routes based on origin and destination criteria.
 */
@UseCase(readOnly = true)
public class SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    public SearchRoutesUseCase(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Retrieves a paginated result of route responses matching the provided origin and destination.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     * @param pageNumber  the zero-based page number
     * @param pageSize    the number of results per page
     * @return a paginated result of route responses matching the search criteria
     */
    public PaginatedResult<RouteResponse> execute(String origin, String destination, int pageNumber, int pageSize) {
        PaginatedResult<Route> result;
        if (origin != null && destination != null) {
            result = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination), pageNumber, pageSize);
        } else if (origin != null) {
            result = routeRepository.findByOrigin(new IataCode(origin), pageNumber, pageSize);
        } else if (destination != null) {
            result = routeRepository.findByDestination(new IataCode(destination), pageNumber, pageSize);
        } else {
            result = routeRepository.findAll(pageNumber, pageSize);
        }

        List<RouteResponse> mapped = result.data().stream()
                .map(r -> RouteResponse.from(r, routeRepository.findVersionFor(r.getOrigin(), r.getDestination())))
                .toList();

        return new PaginatedResult<>(mapped, result.totalElements());
    }
}
