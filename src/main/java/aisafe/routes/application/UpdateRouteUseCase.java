package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.dtos.UpdateRouteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class UpdateRouteUseCase {

    private final RouteRepository routeRepository;

    @Transactional
    public Route execute(Long id, UpdateRouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));

        route.updateRoute(
                request.getEstimatedFlightTime(),
                request.getMinimumRange(),
                request.getMinimumCapacity()
        );

        if (request.getActive() != null) {
            route.setActive(request.getActive());
        }

        return routeRepository.save(route);
    }
}