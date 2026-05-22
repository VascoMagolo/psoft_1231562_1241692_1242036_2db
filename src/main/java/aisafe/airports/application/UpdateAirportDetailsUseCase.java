package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.application.dtos.UpdateAirportDetailsRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Contact;
import aisafe.airports.domain.Gate;
import aisafe.airports.domain.Service;
import aisafe.airports.domain.Terminal;

import java.util.List;

@UseCase
public class UpdateAirportDetailsUseCase {
    private final AirportRepository airportRepository;

    public UpdateAirportDetailsUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public AirportResponse execute(String iataCode, UpdateAirportDetailsRequest request) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));

        List<Contact> contacts = request.contacts() == null ? null :
                request.contacts().stream()
                        .map(c -> new Contact(c.type(), c.value(), c.description()))
                        .toList();

        List<Service> services = request.services() == null ? null :
                request.services().stream().map(Service::new).toList();

        List<Terminal> terminals = request.terminals() == null ? null :
                request.terminals().stream().map(Terminal::new).toList();

        List<Gate> gates = request.gates() == null ? null :
                request.gates().stream().map(Gate::new).toList();

        airport.updateDetails(request.operationalHours(), contacts, request.imagePath(), services, terminals, gates);

        return AirportResponse.from(airportRepository.save(airport));
    }
}
