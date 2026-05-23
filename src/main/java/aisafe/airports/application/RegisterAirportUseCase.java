package aisafe.airports.application;

import aisafe.DuplicateResourceException;
import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.application.dtos.RegisterAirportRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Gate;
import aisafe.airports.domain.Runway;
import aisafe.airports.domain.Service;
import aisafe.airports.domain.Terminal;

import java.util.List;

@UseCase
public class RegisterAirportUseCase {
    private final AirportRepository airportRepository;

    public RegisterAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public AirportResponse execute(RegisterAirportRequest request) {
        if (airportRepository.existsByIataCodeCode(request.iataCode())) {
            throw new DuplicateResourceException("Airport with IATA code '" + request.iataCode() + "' already exists.");
        }

        List<Runway> runways = request.runways().stream()
                .map(r -> new Runway(r.name(), r.length(), r.orientation()))
                .toList();

        Airport airport = new Airport(
                request.iataCode(),
                request.name(),
                request.city(),
                request.country(),
                request.region(),
                request.timezone(),
                request.latitude(),
                request.longitude(),
                runways
        );

        List<Service> services = request.services() == null ? null :
                request.services().stream().map(Service::new).toList();
        List<Terminal> terminals = request.terminals() == null ? null :
                request.terminals().stream().map(Terminal::new).toList();
        List<Gate> gates = request.gates() == null ? null :
                request.gates().stream().map(Gate::new).toList();

        airport.updateDetails(request.operationalHours(), null, request.imagePath(), services, terminals, gates);

        return AirportResponse.from(airportRepository.save(airport));
    }
}
