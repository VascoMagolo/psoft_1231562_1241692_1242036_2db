package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteNotFoundException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ViewRouteDetailsUseCase {

    private final RouteRepository routeRepository;

    public Route execute(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));
    }
}