package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Use case responsible for searching routes based on origin and destination criteria.
 */
@UseCase
@RequiredArgsConstructor
public class SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    /**
     * Retrieves a page of routes matching the provided origin and destination.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     * @param pageable    the pagination information
     * @return a page of routes matching the search criteria
     */
    public Page<Route> execute(String origin, String destination, Pageable pageable) {
        if (origin != null && destination != null) {
            return routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination), pageable);
        } else if (origin != null) {
            return routeRepository.findByOrigin(new IataCode(origin), pageable);
        } else if (destination != null) {
            return routeRepository.findByDestination(new IataCode(destination), pageable);
        }
        return routeRepository.findAll(pageable);
    }
}