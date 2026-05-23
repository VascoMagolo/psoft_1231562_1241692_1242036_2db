package aisafe.airports.application;

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

/**
 * Use case for registering a new airport in the system.
 */
@UseCase
public class RegisterAirportUseCase {
    private final AirportRepository airportRepository;

    public RegisterAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    /**
     * Registers a new airport based on the provided request data.
     * @param request the details of the airport to register.
     * @return a DTO containing the details of the newly registered airport.
     */
    public AirportResponse execute(RegisterAirportRequest request) {
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
