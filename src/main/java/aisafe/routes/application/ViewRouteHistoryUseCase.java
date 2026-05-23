package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * Use case responsible for retrieving the change history of a specific route.
 */
@UseCase
@RequiredArgsConstructor
public class ViewRouteHistoryUseCase {

    private final RouteHistoryRepository historyRepository;
    private final RouteRepository routeRepository;

    /**
     * Retrieves all history entries for the given route ID.
     *
     * @param routeId the unique identifier of the route
     * @return a list of history entries for the specified route
     */
    public List<RouteHistory> execute(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new RouteNotFoundException(routeId.toString());
        }

        return historyRepository.findAllByRouteId(routeId);
    }
}
