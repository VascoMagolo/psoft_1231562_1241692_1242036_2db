package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * Use case responsible for searching routes based on origin and destination criteria.
 */
@UseCase
@RequiredArgsConstructor
public class SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    /**
     * Retrieves routes matching the provided origin and destination.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     * @return a list of routes matching the search criteria
     */
    public List<Route> execute(String origin, String destination) {
        if (origin != null && destination != null) {
            return routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination));
        } else if (origin != null) {
            return routeRepository.findByOrigin(new IataCode(origin));
        } else if (destination != null) {
            return routeRepository.findByDestination(new IataCode(destination));
        }
        return routeRepository.findAll();
    }
}
