package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.RegisterAirportRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.model.valueObject.*;

import java.util.List;

@UseCase
public class RegisterAirportUseCase {
    private final AirportRepository airportRepository;

    public RegisterAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Airport execute(RegisterAirportRequest request) {
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

        return airportRepository.save(airport);
    }
}
