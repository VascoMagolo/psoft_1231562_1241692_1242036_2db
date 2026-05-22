package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.model.valueObject.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ListRoutesFromAirportUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    public List<Route> execute(String iataCode) {
        IataCode origin = new IataCode(iataCode);
        String originCode = iataCode.trim().toUpperCase();

        if (!airportRepository.existsByIataCodeCode(originCode)) {
            throw new AirportNotFoundException(originCode);
        }

        return routeRepository.findByOrigin(origin);
    }
}
