package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.model.valueObject.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ListRoutesFromAirportUseCase {

    private final RouteRepository routeRepository;

    public List<Route> execute(String iataCode) {
        return routeRepository.findByOrigin(new IataCode(iataCode));
    }
}