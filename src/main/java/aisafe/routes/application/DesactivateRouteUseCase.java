package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class DesactivateRouteUseCase {

    private final RouteRepository routeRepository;

    @Transactional
    public Route execute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));

        route.setActive(false); // Método que definimos na entidade Route
        return routeRepository.save(route);
    }
}