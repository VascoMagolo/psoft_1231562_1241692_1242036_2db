package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.RouteRepository;

import java.util.List;

@UseCase(readOnly = true)
public class ViewAirportRoutesUseCase {
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public ViewAirportRoutesUseCase(AirportRepository airportRepository, RouteRepository routeRepository) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    public List<RouteResponse> execute(String iataCode) {
        IataCode code = new IataCode(iataCode);
        if (!airportRepository.existsByIataCode(code)) {
            throw new AirportNotFoundException(iataCode);
        }
        return routeRepository.listSummariesForAirport(code).stream()
                .map(s -> new RouteResponse(
                        s.origin().getCode(),
                        s.destination().getCode(),
                        s.estimatedFlightTime(),
                        s.minimumRange(),
                        s.minimumCapacity(),
                        s.status(),
                        s.version()
                ))
                .toList();
    }
}
