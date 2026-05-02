package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.model.entities.Route;
import aisafe.routes.domain.RouteRepository;

import java.util.List;

@UseCase
public class ViewAirportRoutesUseCase {
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public ViewAirportRoutesUseCase(AirportRepository airportRepository, RouteRepository routeRepository) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    public List<Route> execute(String iataCode) {
        if (!airportRepository.existsByIataCodeCode(iataCode)) {
            throw new AirportNotFoundException(iataCode);
        }
        return routeRepository.findByOriginAirportIataCodeCodeOrDestinationAirportIataCodeCode(iataCode, iataCode);
    }
}
