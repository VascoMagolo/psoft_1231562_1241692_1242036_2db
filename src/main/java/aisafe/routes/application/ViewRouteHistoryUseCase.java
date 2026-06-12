package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ViewRouteHistoryUseCase {

    private final RouteHistoryRepository historyRepository;
    private final RouteRepository routeRepository;

    /**
     * Retrieves all history entries for the route identified by origin and destination.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     * @return a list of history entries for the specified route
     */
    public List<RouteHistory> execute(String origin, String destination) {
        if (!routeRepository.existsByOriginAndDestination(new IataCode(origin), new IataCode(destination))) {
            throw new RouteNotFoundException(origin + "-" + destination);
        }
        return historyRepository.findAllByRoute(origin, destination);
    }
}
