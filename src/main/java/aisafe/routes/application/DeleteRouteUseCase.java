package aisafe.routes.application;

import aisafe.shared.application.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class DeleteRouteUseCase {
    private final RouteRepository routeRepository;

    public void execute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));
        routeRepository.delete(route);
    }
}
