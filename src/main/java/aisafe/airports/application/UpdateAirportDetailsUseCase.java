package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.application.dtos.UpdateAirportDetailsRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.airports.domain.Contact;
import aisafe.airports.domain.Gate;
import aisafe.airports.domain.Service;
import aisafe.airports.domain.Terminal;
import aisafe.shared.domain.ConcurrencyException;

import java.util.List;
import java.util.Objects;

/**
 * Use case for updating the details of an existing airport
 */
@UseCase
public class UpdateAirportDetailsUseCase {
    private final AirportRepository airportRepository;

    public UpdateAirportDetailsUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    /**
     * Updates the details of an existing airport based on the provided IATA code and request data.
     * @param iataCode the IATA code of the airport to update
     * @param request  the new details to update for the airport.
     * @param clientVersion the version from the client's If-Match header
     * @return a DTO containing the updated details of the airport after the update is applied
     */
    public AirportResponse execute(String iataCode, UpdateAirportDetailsRequest request, Long clientVersion) {
        IataCode code = new IataCode(iataCode);
        Airport airport = airportRepository.findByIataCode(code)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));

        if (clientVersion != null) {
            Long current = airportRepository.findVersionFor(code);
            if (!Objects.equals(current, clientVersion)) {
                throw new ConcurrencyException("Airport version mismatch. Please fetch the latest version and retry.");
            }
        }

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

        airportRepository.save(airport);
        Long newVersion = airportRepository.findVersionFor(code);
        return AirportResponse.from(airport, newVersion);
    }
}
