package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.model.valueObject.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    public List<Route> execute(String origin, String destination) {
        if (origin != null && destination != null) {
            return routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination));
        } else if (origin != null) {
            return routeRepository.findByOrigin(new IataCode(origin));
        }
        return routeRepository.findAll();
    }
}