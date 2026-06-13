package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.ResourceInUseException;

@UseCase
public class DeleteAirportUseCase {
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public DeleteAirportUseCase(AirportRepository airportRepository, RouteRepository routeRepository) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    public void execute(String iataCode) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
        IataCode code = new IataCode(iataCode);
        if (!routeRepository.findByOriginOrDestination(code, code).isEmpty()) {
            throw new ResourceInUseException("Airport " + iataCode + " cannot be deleted because it is referenced by existing routes.");
        }
        airportRepository.delete(airport);
    }
}
